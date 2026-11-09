package io.cloudNativeData.ai.assistance.services;

import io.cloudNativeData.ai.assistance.repository.SemanticCacheRepository;
import io.cloudNativeData.ai.assistance.services.model.AiAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiAgentServiceTest {

    private AiAgentService subject;
    private String prompt = "";
    private String context = "";
    @Mock
    private SemanticCacheRepository repository;

    @Mock
    private AiAnswerService aiAnswerService;

    @BeforeEach
    void setUp() {
        subject = new AiAgentService(aiAnswerService,repository);
    }

    @Test
    void saveContext() {

        subject.saveContext(prompt,context);

        verify(repository).saveAnswer(anyString(),anyString());

    }
}