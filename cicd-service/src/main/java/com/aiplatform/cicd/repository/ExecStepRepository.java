package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.ExecStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExecStepRepository extends JpaRepository<ExecStep, Long> {
    List<ExecStep> findByExecStageIdOrderByIdAsc(Long execStageId);
}
