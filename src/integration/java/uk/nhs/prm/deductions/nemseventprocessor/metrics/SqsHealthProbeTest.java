package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SqsClient sqsClient;
    static CreateQueueResponse queue;
    static final String queueName = "integration-test-sqs-health-probe";

    @BeforeEach
    void setUpQueue() {
        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.KMS_MASTER_KEY_ID, UUID.randomUUID().toString());

        queue = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).attributes(attributes).build());
    }

    @Test
    void shouldReturnUnhealthyIfCannotQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config, sqsClient);

        assertFalse(sqsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", queueName,"non-existent-sns-topic", "suspension-non-existent");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config, sqsClient);

        assertTrue(sqsHealthProbe.isHealthy());
    }

     @AfterEach
     void tearDownQueue() {
        sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queue.queueUrl()).build());
    }
}
