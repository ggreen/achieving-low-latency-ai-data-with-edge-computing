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