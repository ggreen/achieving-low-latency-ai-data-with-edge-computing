package io.cloudnativedata.ai.edge.service;

import lombok.SneakyThrows;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

class PostgresMLModelServiceTest {

    private JdbcTemplate jdbcTemplate;
    private PostgresMLModelService subject;

    @BeforeEach
    void setUp() {
        String projectName = "sepsis_risk_predictor";

        this.jdbcTemplate = new JdbcTemplate(DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url("jdbc:postgresql://localhost:5433/postgresml")
                .username("postgresml")
//                .password("")
                .driverClassName("org.postgresql.Driver")
                .build());

        subject = new PostgresMLModelService(jdbcTemplate);
    }

    @SneakyThrows
    @Test
    void sepsis() {

        String projectName = "sepsis_risk_predictor";
        var model = subject.loadXGBoostModel(projectName);

        assertNotNull(model);

        float[] singlePatientFeatures = new float[]{
                0.0f,   // bed_id
                118.0f, // heart_rate (bpm)
                88.0f,  // systolic_bp (mmHg)
                55.0f,  // diastolic_bp (mmHg)
                66.0f,  // mean_arterial_pressure
                28.0f,  // respiratory_rate
                92.0f,  // spo2 (%)
                39.1f,  // temperature (°C)
                18.2f,  // wbc_count
                4.8f    // lactate (mmol/L)
        };

        int numPatients = 1;
        int numFeatures = singlePatientFeatures.length; // 10

        // 4. Wrap single patient sample in DMatrix (1 row, 10 columns)
        DMatrix dMatrix = new DMatrix(singlePatientFeatures, numPatients, numFeatures, Float.NaN);

        // 5. Predict probability
        float[][] predictions = model.predict(dMatrix);
        float sepsisProbability = predictions[0][0];

        // 6. Output Single Patient Evaluation
        System.out.println("\n--- Single Patient Sepsis Evaluation ---");
        System.out.printf("Sepsis Probability: %.2f%%%n", sepsisProbability * 100);
        System.out.println("Status: " + (sepsisProbability > 0.5 ? "⚠️ SEPSIS ALERT" : "✅ NORMAL"));

    }

    @SneakyThrows
    @Test
    void chronicKidneyDisease() {

        String projectName = "CKD Detection Classifier";
        var model = subject.loadXGBoostModel(projectName);

        float[] patientMetrics = new float[] {
                55.0f,  // age: 55 years
                80.0f,  // blood_pressure: 80 mmHg
                1.012f, // specific_gravity: 1.012
                2.0f,   // albumin: 2 (scale 0-5)
                1.0f,   // sugar: 1
                210.0f, // blood_glucose_random: 210 mg/dl
                65.0f,  // blood_urea: 65 mg/dl
                2.4f,   // serum_creatinine: 2.4 mg/dl (elevated)
                132.0f, // sodium: 132 mEq/L
                4.8f,   // potassium: 4.8 mEq/L
                10.2f,  // hemoglobin: 10.2 gms (low)
                31.0f,  // packed_cell_volume: 31%
                8200.0f,// white_blood_cell_count: 8200
                1.0f,   // hypertension: 1 (TRUE)
                1.0f,   // diabetes_mellitus: 1 (TRUE)
                0.0f,   // coronary_artery_disease: 0 (FALSE)
                1.0f,   // pedal_edema: 1 (TRUE)
                0.0f    // anemia: 0 (FALSE)
        };

        // 3. Convert array to a 1-row, 18-column DMatrix
        int rows = 1;
        int cols = 18;
        DMatrix singlePatientData = new DMatrix(patientMetrics, rows, cols, Float.NaN);

        // 4. Run prediction
        float[][] predictions = model.predict(singlePatientData);

        // 5. Interpret output (binary classification score/probability)
        float ckdProbability = predictions[0][0];
        boolean hasCKD = ckdProbability >= 0.5f;

        System.out.printf("CKD Risk Probability: %.2f%%\n", ckdProbability * 100);
        System.out.println("Predicted Diagnosis: " + (hasCKD ? "CKD Positive (1)" : "CKD Negative (0)"));

    }

}