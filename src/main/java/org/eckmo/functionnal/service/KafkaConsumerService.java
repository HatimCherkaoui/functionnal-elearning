package org.eckmo.functionnal.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class KafkaConsumerService {

    /** Keeps last 100 received messages in-memory (for tests + monitoring). */
    @Getter
    private final List<String> receivedMessages = new ArrayList<>();

    @KafkaListener(
        topics = {
            KafkaProducerService.TOPIC_COURSE_PUBLISHED,
            KafkaProducerService.TOPIC_COURSE_UPDATED,
            KafkaProducerService.TOPIC_USER_REGISTERED,
            KafkaProducerService.TOPIC_ENROLLMENT
        },
        groupId = "${spring.kafka.consumer.group-id:elearning-group}"
    )
    public void onMessage(String message) {
        log.info("📨 Kafka received: {}", message);
        synchronized (receivedMessages) {
            receivedMessages.add(message);
            if (receivedMessages.size() > 100) receivedMessages.remove(0);
        }
    }

    public void onCourseNotification(String message) {
        onMessage(message);
    }
}
