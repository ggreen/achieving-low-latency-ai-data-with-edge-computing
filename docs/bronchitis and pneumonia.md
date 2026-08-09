Prompt

```prompt
Change example to only trainin a model with audio embedding vector to detect bronchitis and pneumonia
```


```sql
CREATE TABLE respiratory_audio_embeddings (
    id SERIAL PRIMARY KEY,
    patient_id INT,
    audio_file_path TEXT,
    diagnosis TEXT NOT NULL,       -- Target labels: 'pneumonia', 'bronchitis', 'healthy'
    embedding vector(768) NOT NULL -- Audio embedding vector (e.g., 768 dimensions from wav2vec2)
);
```


```sql
INSERT INTO respiratory_audio_embeddings (patient_id, audio_file_path, diagnosis, embedding)
VALUES
(
    101, 
    '/audio/patient_101.wav', 
    'pneumonia',  
    (SELECT array_agg(random())::real[] FROM generate_series(1, 768))::vector
),
(
    102, 
    '/audio/patient_102.wav', 
    'bronchitis', 
    (SELECT array_agg(random())::real[] FROM generate_series(1, 768))::vector
),
(
    103, 
    '/audio/patient_103.wav', 
    'healthy',    
    (SELECT array_agg(random())::real[] FROM generate_series(1, 768))::vector
);
```

```sql
-- Generate 150 rows (~50 per class) to avoid sparse split errors
INSERT INTO respiratory_audio_embeddings (patient_id, audio_file_path, diagnosis, embedding)
SELECT
    100 + g.id AS patient_id,
    '/audio/patient_' || (100 + g.id)::text || '.wav' AS audio_file_path,
    (ARRAY['pneumonia', 'bronchitis', 'healthy'])[(g.id % 3) + 1] AS diagnosis,
    (SELECT array_agg(random())::real[] FROM generate_series(1, 768))::vector AS embedding
FROM generate_series(1, 150) AS g(id);
```

Create view


```sql
CREATE OR REPLACE VIEW respiratory_embedding_training AS
SELECT
    CASE LOWER(TRIM(diagnosis))
        WHEN 'healthy'    THEN 0::
        WHEN 'bronchitis' THEN 1
        WHEN 'pneumonia'  THEN 1
        END AS diagnosis_code,
    embedding::float4[] AS embedding_features
FROM respiratory_audio_embeddings
WHERE diagnosis IS NOT NULL
  AND LOWER(TRIM(diagnosis)) IN ('healthy', 'bronchitis', 'pneumonia');
```




```sql
SELECT * FROM pgml.train(
    project_name => 'Respiratory Disease Vector Classification v5',
    task => 'classification',
    relation_name => 'respiratory_embedding_training',
    y_column_name => 'diagnosis_code',
    algorithm => 'xgboost',
    test_size => 0.0
);
```