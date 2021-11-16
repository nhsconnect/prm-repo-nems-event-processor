package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

@Component
@Slf4j
public class SqsHealthProbe {
    private final AppConfig config;

    public SqsHealthProbe(AppConfig config) {
        this.config = config;
    }

    public boolean isHealthy() {
        try {
            SqsClient sqsClient = SqsClient.create();
            String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName()).build()).queueUrl();
            sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(queueUrl).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SQS queue: " + queueName(), exception);
            return false;
        }
    }

    private String queueName() {
        return config.incomingQueueName();
    }
}
