package uk.nhs.prm.deductions.nemseventprocessor.suspensions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventMessage;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SuspensionsEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String suspensionsTopicArn = "suspensionsTopicArn";

    private SuspensionsEventPublisher suspensionsEventPublisher;

    @BeforeEach
    void setUp() {
        suspensionsEventPublisher = new SuspensionsEventPublisher(messagePublisher, suspensionsTopicArn);
    }

    @Test
    void shouldPublishMessageToTheSuspensionsTopic() {
        var nemsEventMessage = NemsEventMessage.suspension("111", "2023-01-01", "B12345", "123456");
        suspensionsEventPublisher.sendMessage(nemsEventMessage);
        verify(messagePublisher).sendMessage(suspensionsTopicArn, nemsEventMessage.toJsonString(), "nemsMessageId", "123456");
    }
}
