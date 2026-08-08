package io.cloudnativedata.ai.edge.service;

import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class PostgresMLModelService {

    private final JdbcTemplate jdbcTemplate;

    public PostgresMLModelService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Extracts model raw byte arrays from pgml.files and loads an XGBoost Booster.
     */
    public Booster loadXGBoostModel(String projectName) throws IOException, XGBoostError {
        String sql = """
            SELECT f.data 
            FROM pgml.files f
            JOIN pgml.models m ON f.model_id = m.id
            JOIN pgml.projects p ON m.project_id = p.id
            WHERE p.name = ?
            ORDER BY m.created_at DESC, f.part ASC
            limit 1;
        """;

        // Fetch binary parts
        List<byte[]> modelChunks = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBytes("data"), projectName);

        if (modelChunks.isEmpty()) {
            throw new IllegalStateException("No model found in PostgresML for project: " + projectName);
        }


        // Load into XGBoost Booster
        var json = modelChunks.getFirst();
        log.info("Loading XGBoost model for json: " + new String(json));


        return XGBoost.loadModel(json);

    }
}