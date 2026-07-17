package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.McpServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface McpServerRepository extends JpaRepository<McpServer, Long> {
    List<McpServer> findByIsEnabledTrue();
    List<McpServer> findByStatus(String status);
}
