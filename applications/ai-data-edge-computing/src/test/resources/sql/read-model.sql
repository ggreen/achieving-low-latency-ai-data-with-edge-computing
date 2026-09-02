SELECT f.data
FROM pgml.files f
         JOIN pgml.models m ON f.model_id = m.id
         JOIN pgml.projects p ON m.project_id = p.id
WHERE p.name = 'sepsis_risk_predictor'
ORDER BY m.created_at DESC, f.part ASC;