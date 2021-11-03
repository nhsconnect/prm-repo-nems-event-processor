package uk.nhs.prm.deductions.nemseventprocessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import uk.nhs.prm.deductions.nemseventprocessor.config.Tracer;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessagePublisher {
    private final SnsClient snsClient;
    private final Tracer tracer;

    public void sendMessage(String topicArn, String message) {
        Map<String, MessageAttributeValue> messageAttributes = createMessageAttributes();

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .messageAttributes(messageAttributes)
                .topicArn(topicArn)
                .build();

        log.debug("Sending message to {}", topicArn);
        PublishResponse result = snsClient.publish(request);
        log.info("PUBLISHED: message to {} topic. Message id: {}", topicArn, result.messageId());
    }

    private Map<String, MessageAttributeValue> createMessageAttributes() {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(tracer.getTraceId())
                .build());
        return messageAttributes;
    }
}
