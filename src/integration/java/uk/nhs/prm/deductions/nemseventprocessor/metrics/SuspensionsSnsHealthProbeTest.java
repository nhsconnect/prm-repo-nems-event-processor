package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.HealthProbe;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.healthprobes.SuspensionsSnsHealthProbe;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuspensionsSnsHealthProbeTest {

    @Autowired
    private SnsClient snsClient;
    static CreateTopicResponse topic;

    @BeforeEach
    void setUpTopic() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("KmsMasterKeyId", "aws/sns");

        String snsTopicName = "integration-test-suspensions-health-probe";
        topic = snsClient.createTopic(CreateTopicRequest.builder().name(snsTopicName).attributes(attributes).build());
    }

    @Test
    void shouldReturnUnhealthyIfCannotQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "non-existent-sns-topic", "suspension-non-existent");
        HealthProbe suspensionsSnsHealthProbe = new SuspensionsSnsHealthProbe(config, snsClient);

        assertFalse(suspensionsSnsHealthProbe.isHealthy());
    }

    @Test
    void shouldReturnHealthyIfCanQuerySnsTopic() {
        AppConfig config = new AppConfig("int-test", "non-existent-queue", "unhandled-non-existent", topic.topicArn());
        HealthProbe suspensionsSnsHealthProbe = new SuspensionsSnsHealthProbe(config, snsClient);

        assertTrue(suspensionsSnsHealthProbe.isHealthy());
    }

    @AfterEach
    void tearDownTopic() {
        snsClient.deleteTopic(DeleteTopicRequest.builder().topicArn(topic.topicArn()).build());
    }
}