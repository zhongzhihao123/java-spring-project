package com.aiplatform.cicd.service;

import com.aiplatform.cicd.entity.*;
import com.aiplatform.cicd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CicdService {

    @Autowired private PipelineRepository pipelineRepo;
    @Autowired private StageRepository stageRepo;
    @Autowired private StepRepository stepRepo;
    @Autowired private ExecutionRepository execRepo;
    @Autowired private ExecStageRepository execStageRepo;
    @Autowired private ExecStepRepository execStepRepo;
    @Autowired private ArtifactRepository artifactRepo;
    @Autowired private CodeChangeRepository changeRepo;

    // ── 流水线 CRUD ──

    public List<Pipeline> listPipelines(String keyword, String status) {
        if (keyword != null && !keyword.isEmpty()) {
            return pipelineRepo.findByNameContainingIgnoreCase(keyword);
        }
        return pipelineRepo.findByStatusNotOrderByUpdatedAtDesc("archived");
    }

    public Pipeline getPipeline(Long id) {
        return pipelineRepo.findById(id).orElseThrow(() -> new RuntimeException("流水线不存在: " + id));
    }

    public Pipeline createPipeline(Pipeline p) {
        // 处理 stages 关联
        if (p.getStages() != null) {
            for (Stage s : p.getStages()) {
                s.setPipeline(p);
                if (s.getSteps() != null) {
                    for (Step step : s.getSteps()) {
                        step.setStage(s);
                    }
                }
            }
        }
        return pipelineRepo.save(p);
    }

    public Pipeline updatePipeline(Long id, Pipeline updated) {
        Pipeline existing = getPipeline(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setTriggerType(updated.getTriggerType());
        existing.setRepoUrl(updated.getRepoUrl());
        existing.setDefaultBranch(updated.getDefaultBranch());
        existing.setEnvList(updated.getEnvList());
        existing.setNotifyEnabled(updated.getNotifyEnabled());
        existing.setNotifyType(updated.getNotifyType());
        existing.setNotifyTargets(updated.getNotifyTargets());
        existing.setNotifyGroup(updated.getNotifyGroup());
        existing.setNotifyOn(updated.getNotifyOn());

        // 更新阶段和步骤
        existing.getStages().clear();
        if (updated.getStages() != null) {
            for (Stage s : updated.getStages()) {
                s.setPipeline(existing);
                if (s.getSteps() != null) {
                    for (Step step : s.getSteps()) {
                        step.setStage(s);
                    }
                }
                existing.getStages().add(s);
            }
        }
        return pipelineRepo.save(existing);
    }

    public void deletePipeline(Long id) {
        pipelineRepo.deleteById(id);
    }

    public Pipeline toggleFavorite(Long id) {
        Pipeline p = getPipeline(id);
        p.setFavorite(!p.getFavorite());
        return pipelineRepo.save(p);
    }

    // ── 执行 ──

    public Execution executePipeline(Long pipelineId, String branch, String env, String remark, String user) {
        Pipeline p = getPipeline(pipelineId);
        int buildNo = Optional.ofNullable(execRepo.findMaxBuildNo(pipelineId)).orElse(0) + 1;

        Execution exec = new Execution();
        exec.setPipelineId(pipelineId);
        exec.setBuildNo(buildNo);
        exec.setStatus("running");
        exec.setTriggerUser(user);
        exec.setTriggerType("manual");
        exec.setBranch(branch != null ? branch : p.getDefaultBranch());
        exec.setRemark(remark);
        exec.setStartAt(LocalDateTime.now());
        exec = execRepo.save(exec);

        // 更新流水线最近执行
        p.setLastExecAt(exec.getStartAt());
        p.setLastStatus("running");
        pipelineRepo.save(p);

        // 创建执行阶段（模拟）
        List<Stage> stages = stageRepo.findByPipelineIdOrderByStageOrderAsc(pipelineId);
        for (Stage stage : stages) {
            ExecStage es = new ExecStage();
            es.setExecId(exec.getId());
            es.setStageName(stage.getName());
            es.setStatus("pending");
            execStageRepo.save(es);

            List<Step> steps = stepRepo.findByStageIdOrderByStepOrderAsc(stage.getId());
            for (Step step : steps) {
                ExecStep esStep = new ExecStep();
                esStep.setExecStageId(es.getId());
                esStep.setStepName(step.getName());
                esStep.setStatus("pending");
                execStepRepo.save(esStep);
            }
        }

        return exec;
    }

    public void cancelExecution(Long execId) {
        Execution exec = execRepo.findById(execId).orElseThrow();
        exec.setStatus("cancelled");
        exec.setEndAt(LocalDateTime.now());
        exec.setDuration(java.time.Duration.between(exec.getStartAt(), exec.getEndAt()).toMillis());
        execRepo.save(exec);
    }

    // ── 查询 ──

    public List<Execution> listExecutions(Long pipelineId, String status) {
        if (pipelineId != null) return execRepo.findByPipelineIdOrderByCreatedAtDesc(pipelineId);
        return execRepo.findAllOrderByCreatedAtDesc();
    }

    public Execution getExecution(Long id) {
        Execution exec = execRepo.findById(id).orElseThrow();
        // 填充流水线名称
        Pipeline p = pipelineRepo.findById(exec.getPipelineId()).orElse(null);
        if (p != null) exec.setPipelineName(p.getName());
        return exec;
    }

    public List<ExecStage> getExecStages(Long execId) {
        List<ExecStage> stages = execStageRepo.findByExecIdOrderByIdAsc(execId);
        for (ExecStage es : stages) {
            es.setLogText(null); // 列表不返回日志，节省带宽
        }
        return stages;
    }

    public List<ExecStep> getExecSteps(Long execStageId) {
        return execStepRepo.findByExecStageIdOrderByIdAsc(execStageId);
    }

    public List<CodeChange> getCodeChanges(Long execId) {
        return changeRepo.findByExecId(execId);
    }

    public List<Artifact> getArtifacts(Long execId) {
        return artifactRepo.findByExecId(execId);
    }
}
