package io.cloudNativeData.ai.edge.healthcare.domain.sepsis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SepsisVitalHistory {

    // Entity Identifiers
    private String id;

    // Vital Signs (Numeric Features)
    private BigDecimal heartRate;

    private BigDecimal systolicBp;

    private BigDecimal diastolicBp;

    private BigDecimal meanArterialPressure;

    private BigDecimal respiratoryRate;

    private BigDecimal spo2;

    private BigDecimal temperature;

    // Key Laboratory Readings
    private BigDecimal wbcCount;

    private BigDecimal lactate;

    // XGBoost Target Label
    private Boolean hasSepsis = false;
}
