package io.cloudNativeData.spring.rabbit.streams;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Consumer;
import com.rabbitmq.client.amqp.ConsumerBuilder;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import io.cloudNativeData.spring.rabbit.streams.domain.SpringIoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
@Slf4j
public class ConsumerConfig {

    //TODO: Remove application properties settings

    //Zen's SQL = session = 'rabbit' AND year = 2026
    // Arul's SQL = session IN ('postgres','dataflow')
    // Cora's SQL = session IN ('gemfire','modulith')
    // Vlad's SQL = session IN ('valkey','kafka')
    private final static String sqlFilter = """
             session = 'rabbit' AND year = 2026
            """;

    @Value("${stream.name:events.super.streams.filtering-0}")
    private String streamName0;
    @Value("${stream.name:events.super.streams.filtering-1}")
    private String streamName1;


    @Bean
    Environment amqpEnvironment(@Value("${spring.rabbitmq.username}")String username, @Value("${spring.rabbitmq.password}") String password) {
        return new AmqpEnvironmentBuilder()
                .connectionSettings()
                .username(username)
                .password(password)
                .environmentBuilder()
                .build();
    }

    @Bean
    Connection streamConnection( Environment environment) {
        return environment.connectionBuilder()
                .name("consumer-" + streamName1)
                .build();
    }


    @Bean
    Consumer consumer0(Connection connection,
                       Converter<byte[], SpringIoEvent> messageConverter) {
        return constructConsumer(streamName0, connection, messageConverter);

    }

    @Bean
    Consumer consumer1(Connection connection,
                       Converter<byte[], SpringIoEvent> messageConverter) {
        return constructConsumer(streamName1, connection, messageConverter);

    }


    Consumer constructConsumer(String stream,
                               Connection connection,
                               Converter<byte[], SpringIoEvent> messageConverter) {

        log.info("input consumed with SQL '{}' from input stream {}", sqlFilter, stream);

        return connection.consumerBuilder()
                .queue(stream)
                .stream()
                .offset(ConsumerBuilder.StreamOffsetSpecification.FIRST)
                .filter()
                .sql(sqlFilter)
                .stream()
                .builder().messageHandler((ctx, inputMessage) -> {
                    try {
                        //Processing input message
                        var event = messageConverter.convert(inputMessage.body());
                        log.info("Received: {}", event);

                        //Acknowledge Message acceptance
                        ctx.accept();
                    } catch (Exception e) {
                        log.error("Error:{}", String.valueOf(e));
                        throw e;
                    }
                })
                .build();
    }
}
