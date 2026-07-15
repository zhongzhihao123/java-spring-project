package com.aiplatform.business.repository;

import com.aiplatform.business.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
