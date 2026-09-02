package io.cloudNativeData.ai.edge.healthcare.domain.sepsis;

public record SepsisInferenceFeatures(
        float heartRate,
        float systolicBp,
        float diastolicBp,
        float meanArterialPressure,
        float respiratoryRate,
        float spo2,
        float temperature,
        float wbcCount,
        float lactate
){

}