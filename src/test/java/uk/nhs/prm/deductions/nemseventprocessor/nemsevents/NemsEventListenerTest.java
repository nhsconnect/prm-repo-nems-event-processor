package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.nhs.prm.deductions.nemseventprocessor.config.Tracer;

import javax.jms.JMSException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NemsEventListenerTest {

    @Mock
    private NemsEventService nemsEventService;

    @Mock
    private Tracer tracer;

    @InjectMocks
    private NemsEventListener nemsEventListener;

    @Test
    void shouldCallNemsEventServiceWithReceivedMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        nemsEventListener.onMessage(message);
        verify(nemsEventService).processNemsEvent(payload);
        verify(message).acknowledge();
    }

    @Test
    @SuppressFBWarnings
    void shouldNotAcknowledgeOrProcessMessageIfJMSReceivingFails() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        doThrow(new JMSException("error")).when(message).getText();

        nemsEventListener.onMessage(message);
        verifyNoInteractions(nemsEventService);
        verifyNoMoreInteractions(message);
    }

    @Test
    @SuppressFBWarnings
    void shouldNotAcknowledgeTheMessageIfProcessingFailsForAnyUncheckedException() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        doThrow(new RuntimeException("unchecked")).when(nemsEventService).processNemsEvent(anyString());

        nemsEventListener.onMessage(message);
        verify(message, never()).acknowledge();
    }

}
