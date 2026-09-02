package io.cloudnativedata.ai.edge.sepsis.service;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisInferenceFeatures;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoostError;
import nyla.solutions.core.patterns.conversion.Converter;
import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SepsisDetectionServiceTest {

    private SepsisDetectionService subject;
    private SepsisInferenceFeatures features = JavaBeanGeneratorCreator.of(SepsisInferenceFeatures.class)
            .create();

    @Mock
    private Converter<SepsisInferenceFeatures, float[]> converter;
    @Mock
    private Supplier<Booster> modelSupplier;
    @Mock
    private Booster booster;


    @BeforeEach
    void setUp() {
        subject = new SepsisDetectionService(converter, modelSupplier);
    }

    @Test
    void given_feature_when_inference_then_return_probability() throws XGBoostError {

        float[] floats = {0.3F};
        float expected = 343.33F;
        float[][] returnFloats = {{expected}};

        when(converter.convert(any())).thenReturn(floats);
        when(modelSupplier.get()).thenReturn(booster);
        when(booster.predict(any())).thenReturn(returnFloats);


        var actual = subject.determineProbability(features);

        assertThat(actual).isEqualTo(expected);
    }
}