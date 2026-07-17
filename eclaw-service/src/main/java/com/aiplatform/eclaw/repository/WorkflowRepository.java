package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}
