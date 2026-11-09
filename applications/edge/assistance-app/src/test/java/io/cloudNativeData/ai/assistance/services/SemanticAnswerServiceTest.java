package io.cloudNativeData.ai.assistance.services;

import io.cloudNativeData.ai.assistance.domain.QuestionAnswer;
import io.cloudNativeData.ai.assistance.repository.SemanticCacheRepository;
import io.cloudNativeData.ai.assistance.services.model.AiAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SemanticAnswerServiceTest {

    private AiAgentService subject;

    @Mock
    private SemanticCacheRepository repository;

    @Mock
    private AiAnswerService aiAnswerService;


    @BeforeEach
    void setUp() {
        subject = new AiAgentService(aiAnswerService,repository);
    }

    @Test
    void given_prompt_saved_not_found_findAnswer_then_return_service_response() {

        var expected = "To love";
        when(aiAnswerService.answer(any())).thenReturn(QuestionAnswer.builder().answer(expected).answered(true).build() );
        when(repository.findAnswerByPrompt(any())).thenReturn(Optional.empty());

        var actual = subject.answer("why am I here");

        assertThat(actual).isEqualTo(expected);
        verify(repository).saveAnswer(any(),any());
    }

    @Test
    void given_prompt_when_not_know_findAnswer_then_do_not_save() {

        var expected = "I do not know";
        when(aiAnswerService.answer(any())).thenReturn(QuestionAnswer.builder().answer(null)
                .answered(false).reasonIfNotAnswered(expected).build() );
        when(repository.findAnswerByPrompt(any())).thenReturn(Optional.empty());

        var actual = subject.answer("why am I here");

        assertThat(actual).isEqualTo(expected);
        verify(repository,never()).saveAnswer(any(),any());
    }
}