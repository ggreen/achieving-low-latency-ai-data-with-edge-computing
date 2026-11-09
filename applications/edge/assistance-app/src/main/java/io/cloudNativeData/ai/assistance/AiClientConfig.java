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
                Answer the user's question based strictly on the provided context.
                If the context does not contain enough information to answer the question,
                set 'answered' to false and specify the missing information in 'reasonIfNotAnswered'.
                Do not attempt to fabricate an answer.
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
