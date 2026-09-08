package com.dima.analytics_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlqConsumer {
    @KafkaListener(
            topics = "link-clicks-dead-letter",
            groupId = "analytics-service-dlq",
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void consume(
            byte[] message,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.warn("Received message in dead letter queue. Error: {}. Message: {}",
                new String(message),
                errorMessage
        );
    }
}
