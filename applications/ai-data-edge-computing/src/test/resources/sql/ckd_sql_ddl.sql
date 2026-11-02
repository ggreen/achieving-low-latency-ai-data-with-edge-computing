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