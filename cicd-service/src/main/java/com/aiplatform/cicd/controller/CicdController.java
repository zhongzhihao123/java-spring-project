package com.aiplatform.cicd.controller;

import com.aiplatform.cicd.entity.*;
import com.aiplatform.cicd.service.CicdService;
import com.aiplatform.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/cicd")
public class CicdController {

    @Autowired private CicdService service;

    @GetMapping("/pipelines")
    public ApiResponse<List<Pipeline>> listPipelines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(service.listPipelines(keyword, status));
    }

    @GetMapping("/pipelines/{id}")
    public ApiResponse<Pipeline> getPipeline(@PathVariable Long id) {
        return ApiResponse.success(service.getPipeline(id));
    }

    @PostMapping("/pipelines")
    public ApiResponse<Pipeline> createPipeline(@RequestBody Pipeline p) {
        return ApiResponse.success(service.createPipeline(p));
    }

    @PutMapping("/pipelines/{id}")
    public ApiResponse<Pipeline> updatePipeline(@PathVariable Long id, @RequestBody Pipeline p) {
        return ApiResponse.success(service.updatePipeline(id, p));
    }

    @DeleteMapping("/pipelines/{id}")
    public ApiResponse<Void> deletePipeline(@PathVariable Long id) {
        service.deletePipeline(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/pipelines/{id}/favorite")
    public ApiResponse<Void> toggleFavorite(@PathVariable Long id) {
        service.toggleFavorite(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/pipelines/{id}/execute")
    public ApiResponse<Execution> executePipeline(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        String branch = params != null ? params.get("branch") : null;
        String env = params != null ? params.get("env") : null;
        String remark = params != null ? params.get("remark") : null;
        String user = params != null ? params.getOrDefault("triggerUser", "admin") : "admin";
        return ApiResponse.success(service.executePipeline(id, branch, env, remark, user));
    }

    @PutMapping("/executions/{id}/cancel")
    public ApiResponse<Void> cancelExecution(@PathVariable Long id) {
        service.cancelExecution(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/executions")
    public ApiResponse<Map<String, Object>> listExecutions(
            @RequestParam(required = false) Long pipelineId,
            @RequestParam(required = false) String status) {
        List<Execution> list = service.listExecutions(pipelineId, status);
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", list.size());
        return ApiResponse.success(result);
    }

    @GetMapping("/executions/{id}")
    public ApiResponse<Execution> getExecution(@PathVariable Long id) {
        return ApiResponse.success(service.getExecution(id));
    }

    @GetMapping("/executions/{id}/stages")
    public ApiResponse<List<ExecStage>> getExecStages(@PathVariable Long id) {
        return ApiResponse.success(service.getExecStages(id));
    }

    @GetMapping("/exec-stages/{id}/steps")
    public ApiResponse<List<ExecStep>> getExecSteps(@PathVariable Long id) {
        return ApiResponse.success(service.getExecSteps(id));
    }

    @GetMapping("/executions/{id}/changes")
    public ApiResponse<List<CodeChange>> getCodeChanges(@PathVariable Long id) {
        return ApiResponse.success(service.getCodeChanges(id));
    }

    @GetMapping("/executions/{id}/artifacts")
    public ApiResponse<List<Artifact>> getArtifacts(@PathVariable Long id) {
        return ApiResponse.success(service.getArtifacts(id));
    }
}
