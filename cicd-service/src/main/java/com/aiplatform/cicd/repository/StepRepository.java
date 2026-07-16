package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findByStageIdOrderByStepOrderAsc(Long stageId);
}
