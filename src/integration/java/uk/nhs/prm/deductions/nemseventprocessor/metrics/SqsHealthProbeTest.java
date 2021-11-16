package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqsHealthProbeTest {

    @Test
    void shouldReturnUnhealthyIfCannotQuerySqsQueue() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue");
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertFalse(sqsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySqsQueue() {
        String queueName = "sqs-health-probe-queue";
        SqsClient.create().createQueue(CreateQueueRequest.builder().queueName(queueName).build());

        AppConfig config = new AppConfig("int-test", queueName);
        SqsHealthProbe sqsHealthProbe = new SqsHealthProbe(config);

        assertTrue(sqsHealthProbe.isHealthy());
    }
}
