package io.cloudNativeData.ai.edge;

import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import io.cloudNativeData.ai.edge.entity.SepsisVitalHistoryEntity;
import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudNativeData.ai.edge.repository.SepsisVitalHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbitmq.client.AmqpConnectionFactory;
import org.springframework.amqp.rabbitmq.client.RabbitAmqpAdmin;
import org.springframework.amqp.rabbitmq.client.SingleAmqpConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Slf4j
public class ConsumerConfig {

    public static final String QUEUE_NAME = "sepsis.vitals.queue";

    private SepsisVitalHistoryRepository sepsisVitalHistoryRepository;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    // 1. Declare the RabbitMQ Queue
    @Bean
    public Queue sepsisVitalsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    // 2. Declare the Spring Integration Input Channel
    @Bean
    public MessageChannel sepsisVitalsInputChannel(SepsisVitalHistoryRepository sepsisVitalHistoryRepository) {

        this.sepsisVitalHistoryRepository = sepsisVitalHistoryRepository;
        return new DirectChannel();
    }

    @Bean
    Environment env()
    {
        return new AmqpEnvironmentBuilder().connectionSettings()
                .username(rabbitUser)
                .password(rabbitPassword)
                .environmentBuilder()
                .build();
    }

    @Bean
    public RabbitAmqpAdmin amqpAdmin(AmqpConnectionFactory connectionFactory) {

        Queue streamQueue = QueueBuilder.durable(QUEUE_NAME)
                .stream() // Configures "x-queue-type": "stream"
                .build();

        var admin = new RabbitAmqpAdmin(connectionFactory);
        admin.declareQueue(streamQueue);

        return admin;
    }

    @Bean
    AmqpConnectionFactory connectionFactory(Environment env){
        return new SingleAmqpConnectionFactory(env);
    }



    @Bean
    public IntegrationFlow sepsisVitalsFlow(
            ConnectionFactory connectionFactory,
            JsonMapper jsonMapper,
            SepsisVitalHistoryRepository sepsisVitalHistoryRepository) {

        return IntegrationFlow
                .from(Amqp.inboundAdapter(connectionFactory, QUEUE_NAME))
                .handle(byte[].class, (payload, headers) -> {

                    var sepsisVitalsPayload = jsonMapper.readValue(payload, SepsisVitalHistory.class);

                    log.info("Received sepsisVitalsPayload: {}", sepsisVitalsPayload);

                    sepsisVitalHistoryRepository.save(
                            SepsisVitalHistoryEntity.builder()
                                    .id(sepsisVitalsPayload.getId())
                                    .sepsisVitalHistory(sepsisVitalsPayload)
                                    .build()
                    );
                    return null; // Return null as this is the end of the pipeline
                })
                .get();
    }
}
