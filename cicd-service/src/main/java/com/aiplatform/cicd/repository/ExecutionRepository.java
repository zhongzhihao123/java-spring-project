package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {
    List<Execution> findByPipelineIdOrderByCreatedAtDesc(Long pipelineId);
    List<Execution> findByStatus(String status);
    @Query("SELECT e FROM Execution e ORDER BY e.createdAt DESC")
    List<Execution> findAllOrderByCreatedAtDesc();
    @Query("SELECT COALESCE(MAX(e.buildNo), 0) FROM Execution e WHERE e.pipelineId = :pid")
    Integer findMaxBuildNo(@Param("pid") Long pipelineId);
}
