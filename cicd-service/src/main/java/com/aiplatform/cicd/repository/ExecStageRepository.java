package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.ExecStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExecStageRepository extends JpaRepository<ExecStage, Long> {
    List<ExecStage> findByExecIdOrderByIdAsc(Long execId);
}
