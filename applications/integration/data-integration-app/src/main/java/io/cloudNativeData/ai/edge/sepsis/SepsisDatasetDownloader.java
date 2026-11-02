package io.cloudNativeData.ai.edge.sepsis;

import io.cloudNativeData.ai.edge.ConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Daily downloads the next batch of patient files from the free PhysioNet Challenge 2019
 * "Early Prediction of Sepsis from Clinical Data" dataset, parses them into SepsisVitalHistory
 * and publishes each record to the sepsis.vitals.queue RabbitMQ queue.
 * <p>
 * The dataset is published as one file per patient (p000001.psv, p000002.psv, ...), so each run
 * continues from the last patient processed (tracked in a cursor file) until the set is exhausted.
 */
@Component
@Slf4j
public class SepsisDatasetDownloader {

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;
    private final SepsisPsvParser parser = new SepsisPsvParser();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String baseUrl;
    private final int patientsPerRun;
    private final int maxPatientNumber;
    private final Path cursorFile;

    public SepsisDatasetDownloader(
            RabbitTemplate rabbitTemplate,
            JsonMapper jsonMapper,
            @Value("${sepsis.dataset.base-url}") String baseUrl,
            @Value("${sepsis.dataset.patients-per-run}") int patientsPerRun,
            @Value("${sepsis.dataset.max-patient-number}") int maxPatientNumber,
            @Value("${sepsis.dataset.cursor-file}") String cursorFile) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.patientsPerRun = patientsPerRun;
        this.maxPatientNumber = maxPatientNumber;
        this.cursorFile = Path.of(cursorFile);
    }

    @Scheduled(cron = "${sepsis.dataset.cron}")
    public void downloadAndPublish() {
        int next = readCursor() + 1;
        int last = Math.min(next + patientsPerRun - 1, maxPatientNumber);
        if (next > maxPatientNumber) {
            log.info("Sepsis dataset fully loaded (last patient {})", maxPatientNumber);
            return;
        }

        int records = 0;
        int lastCompleted = next - 1;
        for (int n = next; n <= last; n++) {
            var patientId = String.format("p%06d", n);
            try {
                for (var vitals : parser.parse(patientId, download(patientId))) {
                    send(vitals);
                    records++;
                }
                lastCompleted = n;
            } catch (Exception e) {
                log.error("Stopping run at patient {}; will resume there next run", patientId, e);
                break;
            }
        }

        writeCursor(lastCompleted);
        log.info("Published {} sepsis vital records for patients {}..{}", records, next, lastCompleted);
    }

    private String download(String patientId) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + patientId + ".psv"))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200)
            throw new IOException("Download of " + request.uri() + " failed with HTTP " + response.statusCode());
        return response.body();
    }

    private void send(Object vitals) {
        var properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        rabbitTemplate.send("", ConsumerConfig.QUEUE_NAME,
                MessageBuilder.withBody(jsonMapper.writeValueAsBytes(vitals))
                        .andProperties(properties)
                        .build());
    }

    private int readCursor() {
        try {
            return Files.exists(cursorFile) ? Integer.parseInt(Files.readString(cursorFile).trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            log.warn("Unable to read cursor file {}; starting from the first patient", cursorFile, e);
            return 0;
        }
    }

    private void writeCursor(int patientNumber) {
        try {
            Files.writeString(cursorFile, Integer.toString(patientNumber));
        } catch (IOException e) {
            log.error("Unable to write cursor file {}", cursorFile, e);
        }
    }
}
