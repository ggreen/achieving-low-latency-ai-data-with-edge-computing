```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

```sql
-- Schema for image-based ulcer detection using embeddings
CREATE TABLE foot_ulcer_images (
    image_id SERIAL PRIMARY KEY,
    image_url TEXT,
    embedding vector(512), -- Vector embeddings generated via CLIP/ResNet
    has_ulcer INT          -- 1 for Ulcer present, 0 for Healthy
);

-- Train model on visual vector features
SELECT * FROM pgml.train(
    project_name => 'Foot Ulcer Image Classification',
    task => 'classification',
    relation_name => 'foot_ulcer_images',
    y_column_name => 'has_ulcer',
    algorithm => 'lightgbm'
);
```

```sql
-- Populate using Hugging Face CLIP embeddings directly inside PostgreSQL
INSERT INTO foot_ulcer_images (image_url, embedding, has_ulcer)
VALUES
    -- Ulcer cases (Positive)
    (
        'https://example.com/medical_dataset/dfu_plantar_01.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Plantar foot ulceration with localized inflammation and tissue defect'),
        1
    ),
    (
        'https://example.com/medical_dataset/dfu_calcaneus_02.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Deep diabetic ulcer on the heel area with surrounding erythema'),
        1
    ),
    (
        'https://example.com/medical_dataset/dfu_forefoot_03.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Grade 2 foot ulcer on metatarsal head area'),
        1
    ),

    -- Healthy/Non-ulcer cases (Negative)
    (
        'https://example.com/medical_dataset/healthy_dorsal_01.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Healthy adult foot with intact skin and no lesions'),
        0
    ),
    (
        'https://example.com/medical_dataset/healthy_plantar_02.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Plantar surface of healthy diabetic foot without ulceration'),
        0
    ),
    (
        'https://example.com/medical_dataset/healthy_heel_03.jpg',
        pgml.embed('openai/clip-vit-base-patch32', 'Normal foot skin integrity with minor callus but no ulcer'),
        0
    );
```