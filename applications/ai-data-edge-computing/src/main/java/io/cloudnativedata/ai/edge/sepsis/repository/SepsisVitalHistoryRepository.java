package io.cloudnativedata.ai.edge.sepsis.repository;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudnativedata.ai.edge.sepsis.entity.SepsisVitalHistoryEntity;
import org.springframework.data.repository.CrudRepository;

public interface SepsisVitalHistoryRepository  extends CrudRepository<SepsisVitalHistoryEntity, String> {
}
