package io.cloudnativedata.ai.edge.sepsis.service;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisInferenceFeatures;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import nyla.solutions.core.patterns.conversion.Converter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepsisDetectionService {

    private final Converter<SepsisInferenceFeatures,float[]> converter;
    private final Supplier<Booster> modelSupplier;
    private final static int numPatients =1;

    @SneakyThrows
    public float determineProbability(@NonNull SepsisInferenceFeatures features) {

        // Convert record to float array
        var featureArray = converter.convert(features);
        int numFeatures = featureArray.length;

        DMatrix dMatrix = null;
        try {
            // Wrap features in a DMatrix (1 row, N columns)
            dMatrix = new DMatrix(featureArray, numPatients, numFeatures, Float.NaN);

            // Execute inference
            Booster model = modelSupplier.get();
            float[][] predictions = model.predict(dMatrix);

            // Return calculated probability (value between 0.0 and 1.0)
            return predictions[0][0];

        }
        finally {
            // Always dispose native C++ memory allocated by DMatrix
            if (dMatrix != null) {
                dMatrix.dispose();
            }
        }
    }
}
