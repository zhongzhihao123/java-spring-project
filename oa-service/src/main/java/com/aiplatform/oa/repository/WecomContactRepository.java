package com.aiplatform.oa.repository;

import com.aiplatform.oa.entity.WecomContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WecomContactRepository extends JpaRepository<WecomContact, Long> {
    
    List<WecomContact> findByIsActiveTrue();
    
    Optional<WecomContact> findByUserId(Long userId);
    
    Optional<WecomContact> findByWecomUserid(String wecomUserid);
    
    List<WecomContact> findByWecomDepartment(String wecomDepartment);
}
