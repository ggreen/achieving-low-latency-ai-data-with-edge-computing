package io.cloudNativeData.ai.edge;

import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import io.cloudNativeData.ai.edge.entity.SepsisVitalHistoryEntity;
import io.cloudNativeData.ai.edge.healthcare.domain.sepsis.SepsisVitalHistory;
import io.cloudNativeData.ai.edge.repository.SepsisVitalHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbitmq.client.AmqpConnectionFactory;
import org.springframework.amqp.rabbitmq.client.RabbitAmqpAdmin;
import org.springframework.amqp.rabbitmq.client.SingleAmqpConnectionFactory;
import org.springframework.amqp.rabbitmq.client.listener.RabbitAmqpListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
@Slf4j
public class ConsumerConfig {

    public static final String QUEUE_NAME = "sepsis.vitals.queue";
    public static final String INPUT_CHANNEL = "sepsisVitalsInputChannel";

    private SepsisVitalHistoryRepository sepsisVitalHistoryRepository;

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
        return new AmqpEnvironmentBuilder().build();
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


    // 3. Inbound Channel Adapter: Consumes from RabbitMQ and passes to Channel
    @Bean
    public AmqpInboundChannelAdapter inboundAdapter(AmqpConnectionFactory connectionFactory,
                                                    MessageChannel sepsisVitalsInputChannel) {

        org.springframework.amqp.core.MessageListenerContainer messageListenerContainer = new RabbitAmqpListenerContainer(connectionFactory);
        messageListenerContainer.setQueueNames(QUEUE_NAME);


        var adapter = new AmqpInboundChannelAdapter(messageListenerContainer);

        adapter.setOutputChannel(sepsisVitalsInputChannel);

        return adapter;
    }


    // 4. Service Activator: Listens on the channel and saves payload to Repository
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void processAndSaveVitals(SepsisVitalHistory vitalHistory) {
        sepsisVitalHistoryRepository.save(SepsisVitalHistoryEntity.builder().id(vitalHistory.getId())
                .sepsisVitalHistory(vitalHistory).build());
    }
}
