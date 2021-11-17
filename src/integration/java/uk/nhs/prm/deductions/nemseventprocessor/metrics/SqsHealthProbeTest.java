package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqsHealthProbeTest {

    @Test
    void shouldReturnUnhealthyIfCannotQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertFalse(sqsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySqsQueue() {
        CreateQueueResponse queue = setUpQueue("sqs-health-probe-queue");

        AppConfig config = new AppConfig("int-test", "sqs-health-probe-queue","non-existent-sns-topic");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertTrue(sqsHealthProbe.isHealthy());

        tearDown(queue);
    }

    private CreateQueueResponse setUpQueue(String queueName) {
        return SqsClient.create().createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }

    private void tearDown(CreateQueueResponse queue) {
        SqsClient.create().deleteQueue(DeleteQueueRequest.builder().queueUrl(queue.queueUrl()).build());
    }
}
