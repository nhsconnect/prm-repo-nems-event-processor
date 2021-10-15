package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import javax.jms.JMSException;

import static org.mockito.BDDMockito.willThrow;
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
    void shouldLogAnErrorIfMessageProcessingFails() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        // TODO: Simulate JMSException instead of RuntimeException
        doThrow(new RuntimeException("error")).when(nemsEventService).processNemsEvent(any());
        nemsEventListener.onMessage(message);
        // TODO: Add missing logging assertion
        verify(nemsEventService).processNemsEvent(payload);
    }
}
