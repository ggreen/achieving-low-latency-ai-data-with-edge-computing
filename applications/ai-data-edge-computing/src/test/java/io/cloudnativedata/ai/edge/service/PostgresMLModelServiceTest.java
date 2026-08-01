package io.cloudnativedata.ai.edge.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

class PostgresMLModelServiceTest {

    private JdbcTemplate jdbcTemplate;
    private PostgresMLModelService subject;
    private String projectName = "sepsis_risk_predictor";

    @BeforeEach
    void setUp() {
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
    void loadXGBoostModel() {

        var actual = subject.loadXGBoostModel(projectName);

        assertNotNull(actual);

    }
}