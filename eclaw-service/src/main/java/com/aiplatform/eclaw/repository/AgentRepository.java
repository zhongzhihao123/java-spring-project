package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    List<Agent> findByStatus(String status);
}
