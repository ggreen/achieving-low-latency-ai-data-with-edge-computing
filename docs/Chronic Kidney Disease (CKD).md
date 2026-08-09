
```prompt

Build SQL to train a model using postgresml to train a model to detect Chronic Kidney and a example data/table
```

```text
CREATE TABLE ckd_patient_data (
    patient_id SERIAL PRIMARY KEY,
    age INT,
    blood_pressure INT,              -- mmHg
    specific_gravity NUMERIC(4,3),   -- e.g., 1.005, 1.020
    albumin INT,                     -- 0 to 5 scale
    sugar INT,                       -- 0 to 5 scale
    blood_glucose_random INT,        -- mgs/dl
    blood_urea INT,                  -- mgs/dl
    serum_creatinine NUMERIC(4,1),   -- mgs/dl
    sodium NUMERIC(5,1),             -- mEq/L
    potassium NUMERIC(3,1),          -- mEq/L
    hemoglobin NUMERIC(4,1),         -- gms
    packed_cell_volume INT,          -- %
    white_blood_cell_count INT,      -- cells/cumm
    hypertension BOOLEAN,            -- TRUE / FALSE
    diabetes_mellitus BOOLEAN,       -- TRUE / FALSE
    coronary_artery_disease BOOLEAN, -- TRUE / FALSE
    pedal_edema BOOLEAN,             -- TRUE / FALSE
    anemia BOOLEAN,                  -- TRUE / FALSE
    has_ckd INT                      -- Target variable: 1 = Has CKD, 0 = No CKD
);
```


```sql
INSERT INTO ckd_patient_data (
    age, blood_pressure, specific_gravity, albumin, sugar,
    blood_glucose_random, blood_urea, serum_creatinine, sodium, potassium,
    hemoglobin, packed_cell_volume, white_blood_cell_count,
    hypertension, diabetes_mellitus, coronary_artery_disease, pedal_edema, anemia, has_ckd
) VALUES
-- =================================================================
-- CKD POSITIVE CASES (has_ckd = 1) - 25 Entries
-- =================================================================
(48, 80, 1.020, 1, 0, 121,  36, 1.2, 137.0, 4.4, 15.4, 44,  7800, FALSE, TRUE,  FALSE, FALSE, FALSE, 1),
(68, 70, 1.010, 4, 0, 106, 215, 9.6, 128.0, 4.3,  9.4, 28,  9800, TRUE,  TRUE,  FALSE, TRUE,  TRUE,  1),
(62, 80, 1.010, 2, 3, 423,  53, 1.8, 135.0, 4.9,  9.6, 31,  7500, TRUE,  TRUE,  TRUE,  FALSE, TRUE,  1),
(59, 70, 1.010, 3, 0, 204,  38, 2.1, 130.0, 3.8, 10.0, 32,  6700, TRUE,  FALSE, FALSE, TRUE,  FALSE, 1),
(65, 90, 1.015, 2, 1, 165,  72, 3.4, 132.0, 5.1, 10.2, 33,  9200, TRUE,  TRUE,  FALSE, TRUE,  TRUE,  1),
(71, 80, 1.010, 3, 2, 210, 110, 5.2, 129.0, 5.4,  8.8, 26,  8400, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(53, 70, 1.015, 1, 0, 135,  48, 1.9, 136.0, 4.6, 11.5, 36,  7100, TRUE,  FALSE, FALSE, FALSE, FALSE, 1),
(60, 90, 1.010, 4, 0, 180, 142, 6.8, 126.0, 5.2,  9.1, 27, 11200, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(57, 80, 1.015, 2, 0, 118,  56, 2.3, 133.0, 4.8, 10.8, 34,  6900, TRUE,  TRUE,  FALSE, FALSE, FALSE, 1),
(66, 70, 1.012, 3, 1, 195,  88, 4.1, 131.0, 5.0,  9.5, 30,  8800, TRUE,  TRUE,  FALSE, TRUE,  TRUE,  1),
(50, 80, 1.015, 1, 0, 128,  42, 1.6, 138.0, 4.3, 12.1, 37,  7600, FALSE, TRUE,  FALSE, FALSE, FALSE, 1),
(74, 90, 1.010, 4, 3, 290, 165, 7.5, 125.0, 5.6,  8.2, 24, 10500, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(58, 80, 1.012, 2, 0, 145,  64, 2.7, 134.0, 4.7, 10.4, 32,  8100, TRUE,  FALSE, FALSE, TRUE,  FALSE, 1),
(63, 70, 1.015, 2, 1, 155,  58, 2.2, 135.0, 4.5, 11.0, 35,  7400, TRUE,  TRUE,  FALSE, FALSE, FALSE, 1),
(69, 80, 1.010, 3, 0, 172,  95, 4.8, 128.0, 5.3,  9.0, 28,  9600, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(56, 90, 1.015, 1, 0, 130,  50, 1.7, 137.0, 4.4, 11.8, 36,  6800, TRUE,  FALSE, FALSE, FALSE, FALSE, 1),
(72, 80, 1.010, 4, 2, 240, 180, 8.2, 124.0, 5.8,  7.9, 23, 11800, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(61, 70, 1.012, 2, 0, 160,  68, 2.9, 133.0, 4.9, 10.1, 31,  8300, TRUE,  TRUE,  FALSE, TRUE,  FALSE, 1),
(54, 80, 1.015, 2, 1, 148,  46, 1.8, 136.0, 4.6, 11.2, 35,  7700, TRUE,  FALSE, FALSE, FALSE, FALSE, 1),
(67, 90, 1.010, 3, 1, 205, 125, 5.9, 127.0, 5.1,  8.7, 26,  9900, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(49, 70, 1.020, 1, 0, 115,  39, 1.3, 139.0, 4.2, 13.0, 40,  7300, FALSE, TRUE,  FALSE, FALSE, FALSE, 1),
(75, 80, 1.010, 4, 2, 265, 195, 8.9, 123.0, 6.0,  7.5, 22, 12200, TRUE,  TRUE,  TRUE,  TRUE,  TRUE,  1),
(55, 80, 1.015, 2, 0, 138,  54, 2.0, 135.0, 4.7, 10.6, 33,  7900, TRUE,  TRUE,  FALSE, FALSE, FALSE, 1),
(64, 90, 1.012, 3, 0, 185, 102, 4.5, 130.0, 5.2,  9.3, 29,  9100, TRUE,  TRUE,  FALSE, TRUE,  TRUE,  1),
(52, 70, 1.015, 1, 0, 122,  41, 1.5, 138.0, 4.3, 12.4, 38,  7200, FALSE, FALSE, FALSE, FALSE, FALSE, 1),

-- =================================================================
-- NON-CKD CONTROL CASES (has_ckd = 0) - 25 Entries
-- =================================================================
(40, 80, 1.025, 0, 0, 140,  10, 1.2, 135.0, 5.0, 15.0, 48, 10400, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(23, 80, 1.020, 0, 0,  99,  46, 0.5, 145.0, 4.1, 16.2, 52,  9800, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(34, 70, 1.025, 0, 0, 121,  16, 0.7, 138.0, 4.2, 15.8, 46,  6900, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(45, 80, 1.020, 0, 0,  82,  49, 1.0, 150.0, 4.7, 14.2, 43,  5600, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(29, 70, 1.025, 0, 0,  92,  22, 0.6, 142.0, 4.0, 16.0, 50,  8200, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(38, 80, 1.020, 0, 0, 108,  31, 0.9, 140.0, 4.3, 14.9, 45,  7500, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(42, 70, 1.025, 0, 0, 115,  18, 0.8, 144.0, 4.1, 15.3, 47,  6800, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(26, 80, 1.020, 0, 0,  88,  25, 0.7, 146.0, 3.9, 16.5, 51,  9100, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(31, 70, 1.025, 0, 0,  95,  20, 0.6, 141.0, 4.2, 15.6, 48,  7800, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(47, 80, 1.020, 0, 0, 125,  34, 1.1, 139.0, 4.5, 14.1, 42,  6300, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(33, 70, 1.025, 0, 0, 102,  23, 0.8, 143.0, 4.0, 15.9, 49,  8500, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(22, 80, 1.020, 0, 0,  90,  19, 0.5, 147.0, 3.8, 16.8, 53, 10100, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(36, 70, 1.025, 0, 0, 110,  27, 0.9, 140.0, 4.3, 15.1, 46,  7300, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(44, 80, 1.020, 0, 0, 118,  32, 1.0, 138.0, 4.4, 14.4, 44,  6600, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(28, 70, 1.025, 0, 0,  86,  21, 0.6, 145.0, 4.1, 16.1, 50,  8900, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(39, 80, 1.020, 0, 0, 105,  29, 0.8, 142.0, 4.2, 14.8, 45,  7200, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(41, 70, 1.025, 0, 0, 112,  24, 0.9, 141.0, 4.0, 15.4, 47,  7900, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(25, 80, 1.020, 0, 0,  94,  17, 0.6, 148.0, 3.9, 16.4, 52,  9400, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(35, 70, 1.025, 0, 0, 101,  26, 0.7, 143.0, 4.1, 15.7, 48,  8100, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(46, 80, 1.020, 0, 0, 130,  35, 1.1, 137.0, 4.6, 13.9, 41,  6100, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(30, 70, 1.025, 0, 0,  97,  22, 0.7, 144.0, 4.0, 16.0, 49,  8600, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(24, 80, 1.020, 0, 0,  91,  18, 0.5, 146.0, 3.8, 16.6, 52,  9700, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(37, 70, 1.025, 0, 0, 106,  28, 0.8, 141.0, 4.2, 15.2, 46,  7600, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(43, 80, 1.020, 0, 0, 116,  30, 1.0, 139.0, 4.3, 14.6, 44,  6900, FALSE, FALSE, FALSE, FALSE, FALSE, 0),
(27, 70, 1.025, 0, 0,  89,  20, 0.6, 145.0, 4.0, 16.3, 51,  8800, FALSE, FALSE, FALSE, FALSE, FALSE, 0);
```


Train

```sql
SELECT * FROM pgml.train(
    project_name => 'CKD Detection Classifier',
    task => 'classification',
    relation_name => 'ckd_patient_data',
    y_column_name => 'has_ckd',
    algorithm => 'xgboost'
);
```

Predict


```sql
-- Predict CKD for a single new patient feature vector
SELECT pgml.predict(
    'CKD Detection Classifier',
    ARRAY[
        55,    -- age
        80,    -- blood_pressure
        1.012, -- specific_gravity
        2,     -- albumin
        1,     -- sugar
        210,   -- blood_glucose_random
        65,    -- blood_urea
        2.4,   -- serum_creatinine
        132.0, -- sodium
        4.8,   -- potassium
        10.2,  -- hemoglobin
        31,    -- packed_cell_volume
        8200,  -- white_blood_cell_count
        1,     -- hypertension (TRUE=1)
        1,     -- diabetes_mellitus (TRUE=1)
        0,     -- coronary_artery_disease (FALSE=0)
        1,     -- pedal_edema (TRUE=1)
        0      -- anemia (FALSE=0)
    ]
) AS ckd_prediction;
```


```text
Build a Java XGBoost example that using a module to predict CKD based on 1 patient's metrics
```


```text
You are kidney expert, determine if the following patient has Chronic Kidney Disease.
Reply only with yes or no.

 age: 55.0f,  
               blood_pressure_mmHg: 80.0f,   
                specific_gravity: 1.012f, 
                albumin(0-5) : 2.0f,  
                sugar: 1.0f,  
                blood_glucose_random_mg/dl: 210.0f,  
                blood_urea_mg/dl: 65.0f,  
                serum_creatinine (elevated): 2.4f,    
                sodium_mEq/L:  132.0f ,  
                potassium_mEq/L: 4.8f,    
                hemoglobin_(low): 10.2f,     
                packed_cell_volume: 31.0f,  
                white_blood_cell_count: 8200.0f,
                hypertension_bool: 1.0f,   
                diabetes_mellitus_bool: 1.0f,   
                coronary_artery_disease_bool: 0.0f,   
                pedal_edema_bool: 1.0f,   
                anemia: 0.0f 
```




```json
{
  "age": 55.0,
  "blood_pressure_mm_hg": 80.0,
  "specific_gravity": 1.012,
  "albumin_scale_0_5": 2.0,
  "sugar_scale_0_5": 1.0,
  "blood_glucose_random_mg_dl": 210.0,
  "blood_urea_mg_dl": 65.0,
  "serum_creatinine_mg_dl": 2.4,
  "sodium_m_eq_l": 132.0,
  "potassium_m_eq_l": 4.8,
  "hemoglobin_gms": 10.2,
  "packed_cell_volume_percent": 31.0,
  "white_blood_cell_count": 8200.0,
  "hypertension": 1.0,
  "diabetes_mellitus": 1.0,
  "coronary_artery_disease": 0.0,
  "pedal_edema": 1.0,
  "
```