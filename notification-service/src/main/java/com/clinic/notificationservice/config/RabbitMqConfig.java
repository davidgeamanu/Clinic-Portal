package com.clinic.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the RabbitMQ message converter to use Jackson JSON
 */
@Configuration
public class RabbitMqConfig {

    /**
     * Dead-letter queue: poison messages land here after the listener retry
     * policy is exhausted, so they can be inspected instead of lost.
     */
    @Bean
    public Queue deadLetterQueue(@Value("${app.messaging.dead-letter-queue}") String deadLetterQueueName) {
        return new Queue(deadLetterQueueName, true);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
