package com.aiplatform.oa.repository;

import com.aiplatform.oa.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    List<Application> findByStatusOrderByCreatedAtDesc(String status);
    List<Application> findAllByOrderByCreatedAtDesc();

    @Query("SELECT a FROM Application a WHERE a.applicantId = :userId AND (:status IS NULL OR a.status = :status) ORDER BY a.createdAt DESC")
    List<Application> findByApplicantIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.applicantId = :userId AND a.status = 'pending'")
    long countPendingByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.days), 0) FROM Application a WHERE a.applicantId = :userId AND a.status = 'approved' AND a.typeName = :typeName AND YEAR(a.startDate) = YEAR(CURRENT_DATE)")
    java.math.BigDecimal sumDaysByUserAndTypeThisYear(@Param("userId") Long userId, @Param("typeName") String typeName);
}
