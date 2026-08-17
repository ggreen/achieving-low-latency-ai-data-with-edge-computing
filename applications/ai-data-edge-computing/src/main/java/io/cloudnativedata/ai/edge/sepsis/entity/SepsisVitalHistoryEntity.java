package io.cloudnativedata.ai.edge.sepsis.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table( name = "healthcare.sepsis_vitals_history")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SepsisVitalHistoryEntity {

    @Id
    // Entity Identifiers
    private String bedId;

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
