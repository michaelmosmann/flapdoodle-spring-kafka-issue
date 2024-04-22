package de.darkatra.flapdoodlespringkafkaissue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final TestRepository testRepository;

    public KafkaConsumer(final TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @KafkaListener(topics = "testTopic", containerFactory = "kafkaListenerContainerFactory")
    public void listenGroupFoo(@Payload final String message) {

        System.out.println("Received Message: " + message);

        final TestModel testModel = new TestModel();
        testModel.setId(message);

        testRepository.save(testModel);
    }
}
