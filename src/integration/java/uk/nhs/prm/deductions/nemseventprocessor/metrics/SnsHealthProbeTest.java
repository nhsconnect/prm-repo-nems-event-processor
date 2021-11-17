package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;

import static org.junit.jupiter.api.Assertions.*;

class SnsHealthProbeTest {


    @Test
    void shouldReturnUnhealthyIfCannotQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic");
        SnsHealthProbe snsHealthProbe = new SnsHealthProbe(config);

        assertFalse(snsHealthProbe.isHealthy());
    }


    @Test
    void shouldReturnHealthyIfCanQuerySnsTopic() {

        String snsTopicName = "sns-topic-health-probe";
        CreateTopicResponse topic = SnsClient.create().createTopic(CreateTopicRequest.builder().name(snsTopicName).build());

        AppConfig config = new AppConfig("int-test", "non-existent-queue",topic.topicArn());
        SnsHealthProbe snsHealthProbe = new SnsHealthProbe(config);

        assertTrue(snsHealthProbe.isHealthy());
    }

}