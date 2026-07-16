package com.aiplatform.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * 数据库管理控制器
 * ==================
 * 为 SQL Data 子应用提供数据库浏览和查询能力
 *
 * 返回格式直接是 JSON 对象（不包 ApiResponse），与前端期望一致。
 *
 * 接口列表：
 *   GET  /api/dbadmin/tables            → 获取所有业务表
 *   GET  /api/dbadmin/tables/{name}     → 获取表结构
 *   GET  /api/dbadmin/tables/{name}/data → 分页查询数据
 *   POST /api/dbadmin/query             → 执行自定义 SQL（仅 SELECT）
 */
@RestController
@RequestMapping("/api/dbadmin")
public class DbAdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── 获取所有业务表 ──
    @GetMapping("/tables")
    public Map<String, Object> listTables() {
        String sql = """
            SELECT TABLE_NAME FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY TABLE_NAME
        """;
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        Map<String, Object> result = new HashMap<>();
        result.put("tables", tables);
        return result;
    }

    // ── 获取表结构 ──
    @GetMapping("/tables/{tableName}")
    public Map<String, Object> tableSchema(@PathVariable String tableName) {
        // 安全检查：只允许查看当前数据库的表
        String checkSql = """
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
        """;
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "表不存在: " + tableName);
        }

        String sql = """
            SELECT COLUMN_NAME AS `Field`,
                   COLUMN_TYPE AS `Type`,
                   IS_NULLABLE AS `Null`,
                   COLUMN_KEY AS `Key`,
                   COLUMN_DEFAULT AS `Default`,
                   EXTRA AS `Extra`
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
        """;
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);

        Map<String, Object> result = new HashMap<>();
        result.put("table", tableName);
        result.put("columns", columns);
        return result;
    }

    // ── 分页查询表数据 ──
    @GetMapping("/tables/{tableName}/data")
    public Map<String, Object> tableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        // 安全检查
        String checkSql = """
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
        """;
        Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);
        if (exists == null || exists == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "表不存在: " + tableName);
        }

        // 限制 pageSize
        pageSize = Math.min(pageSize, 500);
        int offset = (page - 1) * pageSize;

        // 总数
        String countSql = "SELECT COUNT(*) FROM `" + tableName + "`";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);

        // 列名
        String colSql = """
            SELECT COLUMN_NAME FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
        """;
        List<String> columns = jdbcTemplate.queryForList(colSql, String.class, tableName);

        // 数据
        String dataSql = "SELECT * FROM `" + tableName + "` LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, pageSize, offset);

        Map<String, Object> result = new HashMap<>();
        result.put("table", tableName);
        result.put("columns", columns);
        result.put("rows", rows);
        result.put("total", total != null ? total : 0);
        result.put("page", page);
        result.put("page_size", pageSize);
        return result;
    }

    // ── 执行自定义 SQL 查询 ──
    @PostMapping("/query")
    public Map<String, Object> runQuery(@RequestBody Map<String, Object> body) {
        String sql = (String) body.get("sql");
        if (sql == null || sql.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SQL 语句不能为空");
        }

        // 安全检查：只允许查询语句
        String upperSql = sql.strip().toUpperCase();
        List<String> allowed = List.of("SELECT", "EXPLAIN", "DESC", "DESCRIBE", "SHOW");
        boolean isAllowed = allowed.stream().anyMatch(upperSql::startsWith);
        if (!isAllowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只允许执行查询语句 (SELECT/EXPLAIN/DESC/SHOW)");
        }

        // 限制返回行数
        int limit = 500;
        Object limitObj = body.get("limit");
        if (limitObj instanceof Number) {
            limit = Math.min(((Number) limitObj).intValue(), 5000);
        }

        try {
            // 如果 SQL 没有 LIMIT，自动加上
            String execSql = sql.trim();
            if (!upperSql.contains("LIMIT") && upperSql.startsWith("SELECT")) {
                execSql = execSql + " LIMIT " + limit;
            }

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(execSql);
            List<String> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                columns = new ArrayList<>(rows.get(0).keySet());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("columns", columns);
            result.put("rows", rows);
            result.put("row_count", rows.size());
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SQL 执行错误: " + e.getMessage());
        }
    }
}
