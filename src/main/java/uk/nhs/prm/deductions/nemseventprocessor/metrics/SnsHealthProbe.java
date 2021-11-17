package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesRequest;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

@Slf4j
@Component
public class SnsHealthProbe implements HealthProbe{
    private final AppConfig config;

    @Value("${aws.unhandledEventsSnsTopicArn}")
    String unhandledArn;

    public SnsHealthProbe(AppConfig config) {
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        try {
            SnsClient snsClient = SnsClient.create();
            snsClient.getTopicAttributes(GetTopicAttributesRequest.builder().topicArn(unhandledArn).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SNS topic: " + unhandledArn, exception);
            return false;
        }
    }

}
