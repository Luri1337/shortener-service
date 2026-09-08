package com.dima.shortener_service.producer;

import com.dima.shortener_service.dto.LinkClickedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LinkEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LinkEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceLinkEvent(LinkClickedEvent linkClickedEvent) {
        kafkaTemplate.send("link-clicks", linkClickedEvent);
    }
}
