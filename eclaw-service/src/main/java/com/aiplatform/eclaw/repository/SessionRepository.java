package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByAgentIdOrderByCreatedAtDesc(Long agentId);
    List<Session> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
