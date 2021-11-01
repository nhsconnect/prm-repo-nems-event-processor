package uk.nhs.prm.deductions.nemseventprocessor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MessagePublisher {
    private final SnsClient snsClient;

    public MessagePublisher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendMessage(String topicArn, String message) {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(MDC.get("traceId"))
                .build());

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .messageAttributes(messageAttributes)
                .topicArn(topicArn)
                .build();

        log.debug("Sending message to {}", topicArn);
        PublishResponse result = snsClient.publish(request);
        log.debug("PUBLISHED: message to {} topic. Message id: {}", topicArn, result.messageId());
    }
}
