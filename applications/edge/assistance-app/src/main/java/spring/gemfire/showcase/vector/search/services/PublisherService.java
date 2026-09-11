package spring.gemfire.showcase.vector.search.services;

import lombok.RequiredArgsConstructor;
import nyla.solutions.core.patterns.integration.Publisher;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import spring.gemfire.showcase.vector.search.domain.PromptContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherService implements Publisher<PromptContext> {

    private final VectorStore vectorStore;

    @Override
    public void send(PromptContext payload) {
        vectorStore.add(List.of(
                Document.builder().text("The answer to question:"+payload.promptText()+" is ANSWER:"
                        +payload.context()).build()
        ));
    }
}
