package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@Slf4j
public class UnhandledEventPublisher {

    private final SnsClient snsClient;
    private final String unhandledEventsSnsTopicArn;

    public UnhandledEventPublisher(SnsClient snsClient, @Value("${aws.unhandledEventsSnsTopicArn}") String unhandledEventsSnsTopicArn) {
        this.snsClient = snsClient;
        this.unhandledEventsSnsTopicArn = unhandledEventsSnsTopicArn;
    }

    public void sendMessage(String message) {
        PublishRequest request = PublishRequest.builder()
            .message(message)
            .topicArn(unhandledEventsSnsTopicArn)
            .build();

        PublishResponse result = snsClient.publish(request);
        log.info("PUBLISHED: message to unhandled events topic. Message id: {}", result.messageId());
    }
}
