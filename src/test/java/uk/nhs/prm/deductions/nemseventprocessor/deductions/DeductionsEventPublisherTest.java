package uk.nhs.prm.deductions.nemseventprocessor.deductions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeductionsEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String deductionsTopicArn = "deductionsTopicArn";

    private DeductionsEventPublisher deductionsEventPublisher;

    @BeforeEach
    void setUp() {
        deductionsEventPublisher = new DeductionsEventPublisher(messagePublisher, deductionsTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        deductionsEventPublisher.sendMessage("message");
        verify(messagePublisher).sendMessage(deductionsTopicArn, "message");
    }
}