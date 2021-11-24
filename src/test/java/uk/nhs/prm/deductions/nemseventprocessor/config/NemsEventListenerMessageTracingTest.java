package uk.nhs.prm.deductions.nemseventprocessor.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventListener;
import uk.nhs.prm.deductions.nemseventprocessor.nemsevents.NemsEventService;

import javax.jms.JMSException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NemsEventListenerMessageTracingTest {

    @Mock
    private NemsEventService nemsEventService;

    @Test
    void shouldAddTraceIdToLoggingContextWhenReceivesMessage() throws JMSException {
        var nemsEventListener = new NemsEventListener(nemsEventService, new Tracer());
        var testLogAppender = addTestLogAppender();

        nemsEventListener.onMessage(new SQSTextMessage("payload"));

        var receivedMessageLogEvent = testLogAppender.findLoggedEvent("RECEIVED");
        assertNotNull(receivedMessageLogEvent);
        assertTrue(receivedMessageLogEvent.getMDCPropertyMap().containsKey("traceId"));
    }

    @NotNull
    private TestLogAppender addTestLogAppender() {
        var testLogAppender = new TestLogAppender();
        var logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
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

    ILoggingEvent findLoggedEvent(String subString) {
        for (ILoggingEvent event: loggingEvents) {
           if (event.getMessage().contains(subString)) {
               return event;
           }
        }
        return null;
    }
}