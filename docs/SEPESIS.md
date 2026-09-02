
brew install libomp


```text
Bottle libomp (22.1.8)                                                                                                                         Downloaded  590.0KB/590.0
```


```text
There is the probability that that person will these vitals has sepsis. Reply only with a answer of yes or no

               heart_rate (bpm): 118.0f, 
                systolic_bp_mmHg: 88.0f,  
                diastolic_bp_mmHg: 55.0f, 
                mean_arterial_pressure: 66.0f,  
                respiratory_rate: 28.0f,  
                spo2 (%): 92.0f,  /
                temperature (°C): 39.1f,  
                wbc_count: 18.2f, 
                lactate (mmol/L): 4.8f 
```

````text
SELECT count(f.data) 
            FROM pgml.files f
            JOIN pgml.models m ON f.model_id = m.id
            JOIN pgml.projects p ON m.project_id = p.id
            WHERE p.name = 'sepsis_risk_predictor'
            limit 1;

````

```sql
CREATE TABLE sepsis_vitals_history (

    -- Vital Signs (Numeric Features)
                                       heart_rate NUMERIC(5,2),         -- bpm (e.g., 85.00)
                                       systolic_bp NUMERIC(5,2),        -- mmHg (e.g., 120.00)
                                       diastolic_bp NUMERIC(5,2),       -- mmHg (e.g., 80.00)
                                       mean_arterial_pressure NUMERIC(5,2), -- MAP = (2*DBP + SBP) / 3
                                       respiratory_rate NUMERIC(5,2),   -- breaths/min
                                       spo2 NUMERIC(5,2),               -- Oxygen Saturation % (e.g., 98.50)
                                       temperature NUMERIC(4,2),        -- Celsius (e.g., 38.50)

    -- Key Laboratory Readings (Used in qSOFA / SIRS criteria for sepsis)
                                       wbc_count NUMERIC(6,2),          -- White Blood Cell count (x10^3/µL)
                                       lactate NUMERIC(5,2),            -- Blood lactate (mmol/L) - critical sepsis biomarker

    -- Feature Aggregates / Engineered Features (Pre-calculated for XGBoost)
                                       shock_index NUMERIC(4,2) GENERATED ALWAYS AS (
                                           CASE WHEN systolic_bp > 0 THEN heart_rate / systolic_bp ELSE NULL END
                                           ) STORED,                        -- HR / SBP (Normal: 0.5 - 0.7; > 0.9 indicates shock)

    -- XGBoost Target Label
                                       has_sepsis BOOLEAN NOT NULL DEFAULT FALSE -- 0 = No Sepsis, 1 = Sepsis Positive

    -- Time & Audit Tracking
);


-- Index for PostgresML training set splits
CREATE INDEX idx_sepsis_vitals_target
    ON sepsis_vitals_history (has_sepsis);

```

```sql

INSERT INTO sepsis_vitals_history (
    heart_rate, 
    systolic_bp, 
    diastolic_bp, 
    mean_arterial_pressure, 
    respiratory_rate, 
    spo2, 
    temperature, 
    wbc_count, 
    lactate, 
    has_sepsis
)
SELECT 
    -- Normal baseline + random variance; shift values higher for sepsis cases
    (70 + (random() * 40) + (CASE WHEN random() > 0.85 THEN 25 ELSE 0 END))::NUMERIC(5,2),   -- Heart Rate (bpm)
    (110 - (random() * 30) - (CASE WHEN random() > 0.85 THEN 20 ELSE 0 END))::NUMERIC(5,2),  -- Systolic BP (mmHg)
    (70 - (random() * 20))::NUMERIC(5,2),                                                   -- Diastolic BP
    (80 - (random() * 20))::NUMERIC(5,2),                                                   -- MAP
    (16 + (random() * 8) + (CASE WHEN random() > 0.85 THEN 10 ELSE 0 END))::NUMERIC(5,2),   -- Resp Rate
    (98 - (random() * 4))::NUMERIC(5,2),                                                    -- SpO2 (%)
    (36.5 + (random() * 1.5) + (CASE WHEN random() > 0.85 THEN 1.8 ELSE 0 END))::NUMERIC(4,2),-- Temp (C)
    (8.0 + (random() * 6.0) + (CASE WHEN random() > 0.85 THEN 8.0 ELSE 0 END))::NUMERIC(6,2), -- WBC Count
    (1.0 + (random() * 1.5) + (CASE WHEN random() > 0.85 THEN 3.5 ELSE 0 END))::NUMERIC(5,2), -- Lactate
    (random() > 0.85)
FROM generate_series(1, 1000);

```


```sql
SELECT * FROM  pgml.train(
    project_name => 'sepsis_risk_predictor',
    algorithm => 'xgboost',
    task => 'classification',
    relation_name => 'sepsis_vitals_history',
    y_column_name => 'has_sepsis'
);
```


```sql
SELECT f.data
FROM pgml.files f
JOIN pgml.models m ON f.model_id = m.id
JOIN pgml.projects p ON m.project_id = p.id
WHERE p.name = 'sepsis_risk_predictor'
ORDER BY m.created_at DESC, f.part ASC;
```


```sql
SELECT 
    SUM(octet_length(f.data)) AS total_size_bytes,
    ROUND(SUM(octet_length(f.data)) / 1024.0 / 1024.0, 2) AS total_size_mb,
    pg_size_pretty(SUM(octet_length(f.data))::bigint) AS human_readable_size
FROM pgml.files f
JOIN pgml.models m ON f.model_id = m.id
JOIN pgml.projects p ON m.project_id = p.id
WHERE p.name = 'sepsis_risk_predictor'
  AND m.id = (
      -- Get the latest trained model ID for this project
      SELECT id FROM pgml.models 
      WHERE project_id = p.id 
      ORDER BY created_at DESC 
      LIMIT 1
  );
```



```text
SELECT pgml.dump_model(1);
```