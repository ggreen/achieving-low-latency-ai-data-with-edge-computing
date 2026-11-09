package io.cloudNativeData.ai.assistance.services.model;

import io.cloudNativeData.ai.assistance.domain.QuestionAnswer;

/**
 * Answer question using an AI Model
 * @author gregory green
 */
public interface AiAnswerService {
    QuestionAnswer answer(String prompt);
}
