package com.aiplatform.eclaw.repository;
import com.aiplatform.eclaw.entity.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long> {
    List<ModelConfig> findByIsEnabledTrue();
    List<ModelConfig> findByProvider(String provider);
}
