package io.cloudNativeData.ai.assistance.services.search;

import org.springframework.ai.document.Document;

import java.util.List;

public interface SimilaritiesService {
    List<Document> findSimilarities(String question);
}
