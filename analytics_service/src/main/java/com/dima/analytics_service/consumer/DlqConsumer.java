package com.dima.analytics_service.consumer;

import com.dima.analytics_service.entity.DeadLetterEvent;
import com.dima.analytics_service.repository.DeadLetterEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlqConsumer {
    private final DeadLetterEventRepository deadLetterEventRepository;

    public DlqConsumer(DeadLetterEventRepository deadLetterEventRepository) {
        this.deadLetterEventRepository = deadLetterEventRepository;
    }

    @KafkaListener(
            topics = "link-clicks-dead-letter",
            groupId = "analytics-service-dlq",
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void consume(
            byte[] message,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        DeadLetterEvent event = DeadLetterEvent.builder()
                .payload(new String(message))
                .errorMessage(errorMessage)
                .build();

        deadLetterEventRepository.save(event);

        log.warn("Received message in dead letter queue. Error: {}. Message: {}",
                new String(message),
                errorMessage
        );
    }
}
