package io.dargenn.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    @KafkaListener(topics = "stats-input", groupId = "stats")
    public void process(String json) {
        System.out.println("RECEIVED json: " + json);
    }
}
