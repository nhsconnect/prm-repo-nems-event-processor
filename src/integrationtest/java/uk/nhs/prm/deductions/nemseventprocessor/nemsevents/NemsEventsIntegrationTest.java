package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;
import static uk.nhs.prm.deductions.nemseventprocessor.nemsevents.AwsTestConfig.UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = AwsTestConfig.class)
class NemsEventsIntegrationTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("aws.region", "eu-west-2");
    }

    @Container
    public static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.10.0"))
                    .withServices(SNS, SQS)
                    .withEnv("AWS_REGION", "eu-west-2")
                    .withEnv("LOCALSTACK_HOSTNAME", "localhost")
                    .withEnv("AWS_DEFAULT_REGION", "eu-west-2")
                    .withEnv("DEFAULT_REGION", "eu-west-2");

    @Test
    void shouldSendNemsEventMessageToUnhandledTopic() {
        String queueUrl = amazonSQSAsync.getQueueUrl(nemsEventQueueName).getQueueUrl();
        String messageBody = "I Have Arrived";
        amazonSQSAsync.sendMessage(queueUrl, messageBody);

        String receiving = amazonSQSAsync.getQueueUrl(UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE).getQueueUrl();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Message> messages = amazonSQSAsync.receiveMessage(receiving).getMessages();
            assertThat(messages).hasSize(1);
            assertTrue(messages.get(0).getBody().contains(messageBody));
        });
    }

}
