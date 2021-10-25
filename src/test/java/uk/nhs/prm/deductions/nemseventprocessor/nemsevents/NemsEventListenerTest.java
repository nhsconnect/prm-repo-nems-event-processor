package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NemsEventListenerTest {

    @Mock
    private NemsEventService nemsEventService;

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
    @CheckReturnValue
    void shouldLogAnErrorIfMessageProcessingFails() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        doThrow(new JMSException("error")).when(message).getText();

        nemsEventListener.onMessage(message);
        verifyNoInteractions(nemsEventService);
        verifyNoMoreInteractions(message);
    }
}
