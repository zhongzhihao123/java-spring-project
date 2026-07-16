package com.aiplatform.oa.repository;

import com.aiplatform.oa.entity.Approver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApproverRepository extends JpaRepository<Approver, Long> {
    List<Approver> findByUserId(Long userId);
    Approver findByUserIdAndIsDefaultTrue(Long userId);
}
