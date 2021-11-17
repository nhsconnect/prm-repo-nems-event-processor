package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnsHealthProbeTest {
    @Test
    void shouldReturnUnhealthyIfCannotQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic");
        SnsHealthProbe snsHealthProbe = new SnsHealthProbe(config);

        assertFalse(snsHealthProbe.isHealthy());
    }


    @Test
    void shouldReturnHealthyIfCanQuerySnsTopic() {
        CreateTopicResponse topic = setUpTopic();

        AppConfig config = new AppConfig("int-test", "non-existent-queue",topic.topicArn());
        SnsHealthProbe snsHealthProbe = new SnsHealthProbe(config);

        assertTrue(snsHealthProbe.isHealthy());

        tearDown(topic);
    }

    private CreateTopicResponse setUpTopic() {
        String snsTopicName = "sns-topic-health-probe";
        CreateTopicResponse topic = SnsClient.create().createTopic(CreateTopicRequest.builder().name(snsTopicName).build());
        return topic;
    }

    private void tearDown(CreateTopicResponse topic) {
        SnsClient.create().deleteTopic(DeleteTopicRequest.builder().topicArn(topic.topicArn()).build());
    }

}