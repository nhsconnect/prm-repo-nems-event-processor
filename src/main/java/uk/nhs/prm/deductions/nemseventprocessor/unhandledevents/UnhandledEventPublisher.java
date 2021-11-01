package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UnhandledEventPublisher {
    private final String unhandledEventsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public UnhandledEventPublisher(MessagePublisher messagePublisher, @Value("${aws.unhandledEventsSnsTopicArn}") String unhandledEventsSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.unhandledEventsSnsTopicArn = unhandledEventsSnsTopicArn;
    }

    public void sendMessage(String message) {
        messagePublisher.sendMessage(this.unhandledEventsSnsTopicArn, message);
    }
}
