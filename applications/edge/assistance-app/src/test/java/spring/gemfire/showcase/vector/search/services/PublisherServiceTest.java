package spring.gemfire.showcase.vector.search.services;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import spring.gemfire.showcase.vector.search.domain.PromptContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    private PublisherService subject;

    private final PromptContext promptContext = JavaBeanGeneratorCreator.of(PromptContext.class).create();
    @Mock
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        this.subject = new PublisherService(vectorStore);
    }

    @Test
    void givenContextWhenAddContextThenSaveToVectorDatabase() {

        subject.send(promptContext);
        verify(vectorStore).add(any(List.class));
    }
}