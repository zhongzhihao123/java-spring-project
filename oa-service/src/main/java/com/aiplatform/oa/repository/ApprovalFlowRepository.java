package com.aiplatform.oa.repository;

import com.aiplatform.oa.entity.ApprovalFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalFlowRepository extends JpaRepository<ApprovalFlow, Long> {
    List<ApprovalFlow> findByStatus(String status);
}
