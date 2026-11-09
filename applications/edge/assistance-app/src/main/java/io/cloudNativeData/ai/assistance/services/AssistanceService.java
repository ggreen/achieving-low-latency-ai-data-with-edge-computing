package io.cloudNativeData.ai.assistance.services;

/**
 * Main serivce to support vit
 */
public interface AssistanceService {

    String answer(String prompt);

    void saveContext(String prompt, String context);
}
