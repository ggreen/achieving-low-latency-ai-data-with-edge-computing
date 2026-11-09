package io.cloudNativeData.ai.assistance;

import io.cloudNativeData.ai.assistance.domain.QuestionAnswer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.cloudNativeData.ai.assistance.services.model.AiAnswerService;
import io.cloudNativeData.ai.assistance.services.search.SimilaritiesService;

import java.util.List;

@Configuration
public class AiClientConfig {
    private final String systemPrompt = """
            You are a factual assistant. Answer the user's question accurately.
            
             Use the following JSON schema:
             {
               "answered": boolean, // true if you can answer using facts/common knowledge, false if ambiguous or missing critical information
               "answer": string or null, // The detailed explanation if answered is true, otherwise null
               "reasonIfNotAnswered": string or null // Explanation of what is missing if answered is false, otherwise null
             }
            
             Guidelines:
             1. For standard factual or scientific questions (e.g., basic physics, definitions), assume standard Earth conditions, set 'answered' to true, and provide the answer.
             2. Only set 'answered' to false if the prompt is genuinely unanswerable, paradoxical, or requires private/unprovided data.
                """;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder)
    {
        return chatClientBuilder
                .build();
    }

    @Bean
    QuestionAnswerAdvisor advisor(VectorStore vectorStore){

        SearchRequest searchRequest = SearchRequest
                .builder()
                .similarityThreshold(.75)
                .build();

        return QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(searchRequest).build();
    }

    @Bean
    SimilaritiesService similaritiesService(VectorStore vectorStore)
    {
        return vectorStore::similaritySearch;

    }

    @Bean
    AiAnswerService answerService(ChatClient chatClient, List<Advisor> advisors)
    {
        return prompt -> chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                //.toolCallbacks(tools)
                .advisors(advisors) //use GemFire vectorDB
                .call().entity(QuestionAnswer.class);
    }
}
