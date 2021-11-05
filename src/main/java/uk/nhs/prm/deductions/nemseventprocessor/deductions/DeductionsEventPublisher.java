package uk.nhs.prm.deductions.nemseventprocessor.deductions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

@Component
public class DeductionsEventPublisher {
    private final String deductionsSnsTopicArn;
    private final MessagePublisher messagePublisher;

    public DeductionsEventPublisher(MessagePublisher messagePublisher, @Value("${aws.deductionsSnsTopicArn}") String deductionsSnsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.deductionsSnsTopicArn = deductionsSnsTopicArn;
    }

    public void sendMessage(String message) {
        messagePublisher.sendMessage(this.deductionsSnsTopicArn, message);
    }
}
