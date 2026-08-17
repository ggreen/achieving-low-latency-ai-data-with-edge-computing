package io.cloudnativedata.ai.edge.sepsis.service;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudnativedata.ai.edge.sepsis.entity.SepsisVitalHistoryEntity;
import io.cloudnativedata.ai.edge.sepsis.repository.SepsisVitalHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SepsisService {

    private final SepsisVitalHistoryRepository repository;
    private final Converter<SepsisVitalHistory, SepsisVitalHistoryEntity> converter;

    public void saveHistory(SepsisVitalHistory vitalHistory) {

        repository.save(converter.convert(vitalHistory));

    }
}
