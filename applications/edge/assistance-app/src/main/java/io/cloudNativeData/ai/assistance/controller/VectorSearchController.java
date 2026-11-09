package io.cloudNativeData.ai.assistance.controller;

import io.cloudNativeData.ai.assistance.services.AssistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.integration.Publisher;
import org.springframework.ai.document.Document;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import io.cloudNativeData.ai.assistance.domain.PromptContext;
import io.cloudNativeData.ai.assistance.services.search.SimilaritiesService;

import java.util.List;


/**
 * * Vector Search Controller for search and evicting caching search results
 * @author Gregory Green
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("vector/search")
@Slf4j
public class VectorSearchController {

    private final SimilaritiesService similaritiesService;
    private final AssistanceService aiAnswerService;

    @PostMapping
    @Cacheable("SearchResults")
    public String answerPrompt(@RequestBody String prompt) {
        log.info("prompt: {}",prompt);
        return aiAnswerService.answer(prompt);
    }

    /**
     * Publish prompt context and evict cache for prompt
     * @param prompt the prompt
     * @param context the context
     */
    @PostMapping("prompt/context")
    public void publishPromptContext(@RequestParam String prompt, @RequestParam String context) {
        aiAnswerService.saveContext(prompt,context);
    }

    @DeleteMapping("prompt")
    @CacheEvict(value = "SearchResults", key = "#prompt")
    public void evictPrompt(@RequestParam String prompt) {
        log.info("Deleted: {} using Spring Cache @CacheEvict",prompt);
    }


    @PostMapping("similarities")
    public List<Document> findSimilarities(@RequestBody String question) {
        return similaritiesService.findSimilarities(question);
    }
}
