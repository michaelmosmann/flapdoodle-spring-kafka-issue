package de.darkatra.flapdoodlespringkafkaissue;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("it")
@EmbeddedKafka(topics = {"testTopic"}, controlledShutdown = true)
class FlapdoodleSpringKafkaIssueApplicationTests {

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private TestRepository testRepository;

    private KafkaTemplate<String, String> testProducer;

    @BeforeEach
    void setUp() {

        testRepository.deleteAll();

        final Map<String, Object> producerProps = KafkaTestUtils.producerProps(broker);
        producerProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        testProducer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }

    @Test
    void shouldConsumeMessageAndPersistToDatabase() {

        testProducer.send("testTopic", "key", "hello world!");

        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> !testRepository.findAll().isEmpty());

        final List<TestModel> testModels = testRepository.findAll();
        assertThat(testModels).hasSize(1);
        assertThat(testModels.get(0).getId()).isEqualTo("hello world!");
    }
}
