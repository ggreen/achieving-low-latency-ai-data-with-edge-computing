package io.cloudNativeData.ai.assistance.repository;

import io.cloudNativeData.ai.assistance.domain.QaContext;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QaRepository  extends KeyValueRepository<QaContext, String> {
}
