package com.aiplatform.cicd.repository;

import com.aiplatform.cicd.entity.CodeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeChangeRepository extends JpaRepository<CodeChange, Long> {
    List<CodeChange> findByExecId(Long execId);
}
