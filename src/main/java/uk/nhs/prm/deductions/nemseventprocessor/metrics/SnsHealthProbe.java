package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;

@Slf4j
@Component
public class SnsHealthProbe implements HealthProbe{
    private final AppConfig config;


    public SnsHealthProbe(AppConfig config) {
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        try {
            SnsClient snsClient = SnsClient.create();
            snsClient.getTopicAttributes(GetTopicAttributesRequest.builder().topicArn(config.unhandledEventsSnsTopicArn()).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SNS topic: " + config.unhandledEventsSnsTopicArn(), exception);
            return false;
        }
    }

}
