package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    List<Pipeline> findByStatusNotOrderByUpdatedAtDesc(String status);
    List<Pipeline> findByNameContainingIgnoreCase(String name);
    List<Pipeline> findByFavoriteTrue();
}
