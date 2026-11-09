package io.cloudNativeData.ai.assistance.repository;

import io.cloudNativeData.ai.assistance.domain.QaContext;
import io.cloudNativeData.ai.assistance.domain.QuestionAnswer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Semantic cache: prompts are embedded and stored in the vector store along with their answer.
 * A lookup returns the answer of the most similar cached prompt that meets the similarity threshold.
 */
@Repository
@Slf4j
public class SemanticCacheRepository {

    static final String TYPE_KEY = "type";
    static final String TYPE_VALUE = "semantic-cache";
    static final String ANSWER_KEY = "answer";

    private final VectorStore vectorStore;
    private final double similarityThreshold;
    private final QaRepository qaRepository;

    public SemanticCacheRepository(VectorStore vectorStore,
                                   @Value("${app.ai.semantic-cache.similarity-threshold:0.9}") double similarityThreshold,
                                   QaRepository qaRepository) {
        this.vectorStore = vectorStore;
        this.similarityThreshold = similarityThreshold;
        this.qaRepository = qaRepository;
    }

    /**
     * @return the answer of the most similar cached prompt, or null on a cache miss
     */
    public Optional<String> findAnswerByPrompt(String prompt) {
        if (prompt == null || prompt.isBlank())
            return Optional.empty();

        var request = SearchRequest.builder()
                .query(prompt)
                .topK(1)
                .similarityThreshold(similarityThreshold)
//                .filterExpression(new FilterExpressionBuilder().eq(TYPE_KEY, TYPE_VALUE).build()) Not supported in Valkey
                .build();

        List<Document> matches = vectorStore.similaritySearch(request);

        log.info("Found matches: {}", matches);

        if (matches == null || matches.isEmpty())
            return Optional.empty();

        var key = matches.getFirst().getId();
        log.info("Looking for key: {}", key);

        var cachedResponse = qaRepository.findById(key);

        log.info("cachedResponse {}", cachedResponse);
        return cachedResponse.map(QaContext::answer);

    }

    /**
     * Cache an answer for a prompt in the vector databases
     * And in the key/value store.
     *
     */
    public void saveAnswer(String prompt, String answer) {

        var context = "Given question:"+prompt+" then answer:"+answer;

        log.info("Context is {}", context);

        var doc = Document.builder()
                .text(context)
                .build();

        log.info("Docs is {}", doc);
        vectorStore.add(List.of(
                doc
                ));

        qaRepository.save(QaContext.builder().id(doc.getId())
                        .answer(answer).build());

    }
}
