package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.EclawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EclawLogRepository extends JpaRepository<EclawLog, Long> {
    List<EclawLog> findByAgentIdOrderByCreatedAtDesc(Long agentId);
    List<EclawLog> findByWorkflowIdOrderByCreatedAtDesc(Long workflowId);
    long countByStatus(String status);
}
