package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.HealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SuspensionsSnsHealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.UnhandledSnsHealthProbe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuspensionsSnsHealthProbeTest {
    @Test
    void shouldReturnUnhealthyIfCannotQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        HealthProbe suspensionsSnsHealthProbe = new SuspensionsSnsHealthProbe(config);

        assertFalse(suspensionsSnsHealthProbe.isHealthy());
    }


    @Test
    void shouldReturnHealthyIfCanQuerySnsTopic() {
        CreateTopicResponse topic = setUpTopic();

        AppConfig config = new AppConfig("int-test", "non-existent-queue", "unhandled-non-existent", topic.topicArn());
        HealthProbe suspensionsSnsHealthProbe = new SuspensionsSnsHealthProbe(config);

        assertTrue(suspensionsSnsHealthProbe.isHealthy());

        tearDown(topic);
    }

    private CreateTopicResponse setUpTopic() {
        String snsTopicName = "suspensions-topic-health-probe";
        CreateTopicResponse topic = SnsClient.create().createTopic(CreateTopicRequest.builder().name(snsTopicName).build());
        return topic;
    }

    private void tearDown(CreateTopicResponse topic) {
        SnsClient.create().deleteTopic(DeleteTopicRequest.builder().topicArn(topic.topicArn()).build());
    }

}