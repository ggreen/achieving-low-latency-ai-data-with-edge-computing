package io.cloudNativeData.ai.edge.repository;

import io.cloudNativeData.ai.edge.entity.SepsisVitalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  SepsisVitalHistoryRepository extends JpaRepository<SepsisVitalHistoryEntity,String> {


}
