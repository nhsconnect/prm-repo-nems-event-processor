package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(MDC.get("traceId"))
                        .build());

        PublishRequest request = PublishRequest.builder()
            .message(message)
            .messageAttributes(messageAttributes)
            .topicArn(unhandledEventsSnsTopicArn)
            .build();

        log.info("Send message to {}", unhandledEventsSnsTopicArn);
        PublishResponse result = snsClient.publish(request);
        log.info("PUBLISHED: message to unhandled events topic. Message id: {}", result.messageId());
    }
}
