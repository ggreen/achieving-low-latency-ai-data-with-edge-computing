package io.cloudnativedata.ai.edge.sepsis.service;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudnativedata.ai.edge.sepsis.entity.SepsisVitalHistoryEntity;
import io.cloudnativedata.ai.edge.sepsis.repository.SepsisVitalHistoryRepository;
import nyla.solutions.core.data.Copier;
import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SepsisServiceTest {

    private SepsisService subject;
    private final SepsisVitalHistory vitalHistory = JavaBeanGeneratorCreator
            .of(SepsisVitalHistory.class).create();

    @Mock
    private SepsisVitalHistoryRepository repository;

    @Mock
    private Converter<SepsisVitalHistory, SepsisVitalHistoryEntity> converter;

    @Mock
    private SepsisVitalHistoryEntity entity;


    @BeforeEach
    void setUp() {
        subject = new SepsisService(repository,converter);
    }

    @Test
    void saveHistory() {
        when(converter.convert(any())).thenReturn(entity);

        subject.saveHistory(vitalHistory);


//        nyla.solutions.core.patterns.conversion.JavaBeanConverter
        verify(repository).save(any(SepsisVitalHistoryEntity.class));
    }
}