package com.aiplatform.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局 JWT 鉴权过滤器
 *
 * 白名单路径直接放行，其余路径检查 Authorization Header。
 * 解析后的 userId / username / role 写入请求头透传至下游服务。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** 白名单路径（无需 JWT） */
    private static final List<String> WHITE_LIST = List.of(
            "/api/users/login", "/api/users/register",
            "/api/admin/users/forgot-password",
            "/api/nlp", "/api/recommend", "/api/cv", "/api/mlops",
            "/api/dbadmin", "/api/cicd", "/api/oa", "/api/eclaw", "/api/health", "/api/info",
            "/api/docs", "/v3/api-docs"
    );

    @Value("${jwt.secret:ai-platform-jwt-secret-key-min-256-bits-long-2024}")
    private String jwtSecret;

    @Value("${jwt.enabled:false}")
    private boolean jwtEnabled;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!jwtEnabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            exchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", claims.getSubject()))
                    .request(r -> r.header("X-User-Name", claims.get("username", String.class)))
                    .request(r -> r.header("X-User-Role", claims.get("role", String.class)))
                    .build();

            return chain.filter(exchange);
        } catch (JwtException e) {
            log.warn("JWT 鉴权失败: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
