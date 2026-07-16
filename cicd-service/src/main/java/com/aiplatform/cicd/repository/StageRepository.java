package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findByPipelineIdOrderByStageOrderAsc(Long pipelineId);
}
