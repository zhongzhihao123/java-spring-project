package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArtifactRepository extends JpaRepository<Artifact, Long> {
    List<Artifact> findByExecId(Long execId);
}
