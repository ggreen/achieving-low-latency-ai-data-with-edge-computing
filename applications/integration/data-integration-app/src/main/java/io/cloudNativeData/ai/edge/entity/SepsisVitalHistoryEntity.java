package io.cloudNativeData.ai.edge.entity;

import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SepsisVitalHistoryEntity {
    @Id
    @Column(name = "sepsis_vhe_id")
    private String id;

    @Embedded
    SepsisVitalHistory sepsisVitalHistory;
}
