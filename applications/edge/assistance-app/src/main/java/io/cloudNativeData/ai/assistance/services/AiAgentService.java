package io.cloudNativeData.ai.assistance.services;

import io.cloudNativeData.ai.assistance.repository.SemanticCacheRepository;
import io.cloudNativeData.ai.assistance.services.model.AiAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAgentService implements AssistanceService {

    private final AiAnswerService aiAnswerService;
    private final SemanticCacheRepository repository;

    @Override
    public String answer(String prompt) {
        log.info("Answering prompt {}", prompt);

        var savedResponse = repository.findAnswerByPrompt(prompt);
        if(savedResponse.isPresent()) {
            log.info("Returning answer {}", savedResponse);
            return savedResponse.get();
        }

        log.info("Asking model");
        var aiResponse = aiAnswerService.answer(prompt);
        log.info("Ai response: {}", aiResponse);
        if(aiResponse.answered())
        {
            log.info("Saving answer for prompt {}", prompt);
            repository.saveAnswer(prompt, aiResponse.answer());
            return aiResponse.answer();
        }
        else{
            return aiResponse.reasonIfNotAnswered();
        }
    }

    @Override
    public void saveContext(String prompt, String context) {
        log.info("Saving context for prompt {}", prompt);

        this.repository.saveAnswer(prompt, context);
    }
}
