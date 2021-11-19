package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SqsHealthProbe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqsHealthProbeTest {

    static CreateQueueResponse queue;
    static final String queueName = "integration-test-sqs-health-probe";

    @BeforeAll
    static void setUpQueue() {
        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.KMS_MASTER_KEY_ID, UUID.randomUUID().toString());

        queue = SqsClient.create().createQueue(CreateQueueRequest.builder().queueName(queueName).attributes(attributes).build());
    }

    @Test
    void shouldReturnUnhealthyIfCannotQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertFalse(sqsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", queueName,"non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertTrue(sqsHealthProbe.isHealthy());
    }

    @AfterAll
    static void tearDownQueue() {
        SqsClient.create().deleteQueue(DeleteQueueRequest.builder().queueUrl(queue.queueUrl()).build());
    }
}
