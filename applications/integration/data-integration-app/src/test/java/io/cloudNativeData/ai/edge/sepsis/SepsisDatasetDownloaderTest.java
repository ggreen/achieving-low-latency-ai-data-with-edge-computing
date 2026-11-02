package io.cloudNativeData.ai.edge.sepsis;

import com.sun.net.httpserver.HttpServer;
import io.cloudNativeData.ai.edge.ConsumerConfig;
import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SepsisDatasetDownloaderTest {

    private static final String PSV = """
            HR|O2Sat|Temp|SBP|MAP|DBP|Resp|WBC|Lactate|Age|ICULOS|SepsisLabel
            NaN|NaN|NaN|NaN|NaN|NaN|NaN|NaN|NaN|83.14|1|0
            97|95|NaN|98|75.33|NaN|19|NaN|NaN|83.14|2|0
            110|91|38.6|88|65.3|54|24|14.8|3.2|83.14|3|1
            """;

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String baseUrl;
    private RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private Path cursorFile;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // patients 1 and 2 exist; anything else is a 404
        for (var patient : List.of("p000001", "p000002")) {
            server.createContext("/" + patient + ".psv", exchange -> {
                var body = PSV.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
        }
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";

        rabbitTemplate = mock(RabbitTemplate.class);
        cursorFile = tempDir.resolve("cursor.txt");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private SepsisDatasetDownloader downloader(int patientsPerRun, int maxPatientNumber) {
        return new SepsisDatasetDownloader(rabbitTemplate, jsonMapper, baseUrl,
                patientsPerRun, maxPatientNumber, cursorFile.toString());
    }

    @Test
    void downloadAndPublish_sendsParsedRecordsToQueueAndAdvancesCursor() throws IOException {
        downloader(2, 10).downloadAndPublish();

        var captor = ArgumentCaptor.forClass(Message.class);
        // 2 patients x 2 non-empty rows
        verify(rabbitTemplate, times(4)).send(eq(""), eq(ConsumerConfig.QUEUE_NAME), captor.capture());

        var first = captor.getAllValues().get(0);
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, first.getMessageProperties().getContentType());

        var vitals = jsonMapper.readValue(first.getBody(), SepsisVitalHistory.class);
        assertEquals("p000001-2", vitals.getId());
        assertFalse(vitals.getHasSepsis());

        var last = jsonMapper.readValue(captor.getAllValues().get(3).getBody(), SepsisVitalHistory.class);
        assertEquals("p000002-3", last.getId());
        assertTrue(last.getHasSepsis());

        assertEquals("2", Files.readString(cursorFile));
    }

    @Test
    void downloadAndPublish_resumesFromCursor() throws IOException {
        Files.writeString(cursorFile, "1");

        downloader(1, 10).downloadAndPublish();

        var captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate, times(2)).send(eq(""), eq(ConsumerConfig.QUEUE_NAME), captor.capture());
        var vitals = jsonMapper.readValue(captor.getAllValues().get(0).getBody(), SepsisVitalHistory.class);
        assertEquals("p000002-2", vitals.getId());
        assertEquals("2", Files.readString(cursorFile));
    }

    @Test
    void downloadAndPublish_stopsAtFailedDownloadAndKeepsCursorAtLastSuccess() throws IOException {
        // patient 3 returns 404
        downloader(5, 10).downloadAndPublish();

        verify(rabbitTemplate, times(4)).send(any(String.class), any(String.class), any(Message.class));
        assertEquals("2", Files.readString(cursorFile));
    }

    @Test
    void downloadAndPublish_doesNothingWhenDatasetExhausted() throws IOException {
        Files.writeString(cursorFile, "2");

        downloader(5, 2).downloadAndPublish();

        verify(rabbitTemplate, never()).send(any(String.class), any(String.class), any(Message.class));
        assertEquals("2", Files.readString(cursorFile));
    }

    @Test
    void downloadAndPublish_respectsMaxPatientNumber() throws IOException {
        downloader(10, 1).downloadAndPublish();

        verify(rabbitTemplate, times(2)).send(any(String.class), any(String.class), any(Message.class));
        assertEquals("1", Files.readString(cursorFile));
    }
}
