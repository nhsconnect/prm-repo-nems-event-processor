package uk.nhs.prm.deductions.nemseventprocessor.unhandledevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnhandledEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String unhandledTopicArn = "unhandledTopicArn";

    private UnhandledEventPublisher unhandledEventPublisher;

    @BeforeEach
    void setUp() {
        unhandledEventPublisher = new UnhandledEventPublisher(messagePublisher, unhandledTopicArn);
    }

    @Test
    void shouldPublishMessageToTheUnhandledTopic() {
        unhandledEventPublisher.sendMessage("message");
        verify(messagePublisher).sendMessage(unhandledTopicArn, "message");
    }
}
