package io.cloudNativeData.ai.assistance.repository;

import io.cloudNativeData.ai.assistance.domain.QaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticCacheRepositoryTest {

    private final static String prompt = "Meaning of life";
    @Mock
    private VectorStore vectorStore;

    @Mock
    private QaRepository qaRepository;

    private SemanticCacheRepository subject;

    @BeforeEach
    void setUp() {
        subject = new SemanticCacheRepository(vectorStore, 0.9,qaRepository);
    }

    @Test
    void given_saved_response_when_findAnswer_then_return() {
        String docId = "docId";
        String answer = "To love";
        var expected = Optional.of(answer);
        var doc = Document.builder().text(prompt)
                .id(docId)
                .build();
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(qaRepository.findById(anyString())).thenReturn(
                Optional.of(QaContext.builder()
                .id(docId)
                .answer(answer).build()));

        var actual = subject.findAnswerByPrompt("What is the meaning of life?");

        assertThat(actual).isEqualTo(expected);
        var captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        assertThat(captor.getValue().getTopK()).isEqualTo(1);
        assertThat(captor.getValue().getSimilarityThreshold()).isEqualTo(0.9);
    }

    @Test
    void given_no_similar_prompt_when_findAnswer_then_null() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        assertThat(subject.findAnswerByPrompt(prompt)).isEmpty();
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void when_saveAnswer_then_add_document_repository() {

        var qa = QaContext.builder().id("hellow")
                .answer("world").build();

        subject.saveAnswer(qa.id(),qa.answer());

        verify(qaRepository).save(any());

    }
}
