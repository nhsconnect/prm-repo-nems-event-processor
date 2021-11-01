package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnhandledEventPublisherTest {

    @Mock
    private SnsClient snsClient;

    private final static String topicArn = "topicArn";

    private UnhandledEventPublisher unhandledEventPublisher;

    @BeforeEach
    void setUp() {
        unhandledEventPublisher = new UnhandledEventPublisher(snsClient, topicArn);
    }

    @Test
    void shouldPublishMessageToSns() {
        String message = "someMessage";
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(MDC.get("traceId"))
                .build());

        PublishRequest expectedRequest = PublishRequest.builder()
            .message(message)
            .topicArn(topicArn).messageAttributes(messageAttributes)
            .build();

        String messageId = "someMessageId";
        PublishResponse publishResponse = PublishResponse.builder().messageId(messageId).build();

        when(snsClient.publish(expectedRequest)).thenReturn(publishResponse);

        unhandledEventPublisher.sendMessage(message);
        verify(snsClient).publish(expectedRequest);
    }
}
