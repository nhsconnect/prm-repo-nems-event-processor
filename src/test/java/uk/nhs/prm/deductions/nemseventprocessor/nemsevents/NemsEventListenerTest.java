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

import javax.jms.JMSException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
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
    @SuppressFBWarnings
    void shouldNotAcknowledgeOrProcessMessageIfProcessingFails() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        doThrow(new JMSException("error")).when(message).getText();

        nemsEventListener.onMessage(message);
        verifyNoInteractions(nemsEventService);
        verifyNoMoreInteractions(message);
    }

    @Test
    void shouldAddTraceIdToLoggingContextWhenReceivesMessage() throws JMSException {
        TestLogAppender testLogAppender = addTestLogAppender();

        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        nemsEventListener.onMessage(message);
        ILoggingEvent lastLoggedEvent = testLogAppender.getLastLoggedEvent();
        assertNotNull(lastLoggedEvent);
        assertTrue(lastLoggedEvent.getMDCPropertyMap().containsKey("traceId"));
        assertEquals(32, lastLoggedEvent.getMDCPropertyMap().get("traceId").length());
    }

    @NotNull
    private TestLogAppender addTestLogAppender() {
        TestLogAppender testLogAppender = new TestLogAppender();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(testLogAppender);

        testLogAppender.start();
        return testLogAppender;
    }
}

class TestLogAppender extends AppenderBase<ILoggingEvent> {
    ArrayList<ILoggingEvent> loggingEvents = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        loggingEvents.add(eventObject);
    }

    ILoggingEvent getLastLoggedEvent() {
        if (loggingEvents.isEmpty()) return null;
        return loggingEvents.get(loggingEvents.size() - 1);
    }
}
