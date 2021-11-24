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

    @InjectMocks
    private NemsEventListener nemsEventListener;

    @Test
    void shouldAddTraceIdToLoggingContextWhenReceivesMessage() throws JMSException {

        Tracer tracer = new Tracer();
        nemsEventListener = new NemsEventListener(nemsEventService, tracer);
        TestLogAppender testLogAppender = addTestLogAppender();

        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        nemsEventListener.onMessage(message);

        var receivedMessageLogEvent = testLogAppender.hasLoggedEvent("RECEIVED");
        assertTrue(receivedMessageLogEvent);
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

        boolean hasLoggedEvent(String subString) {
            if (loggingEvents.isEmpty()) return false;
            for(ILoggingEvent event:loggingEvents){
               if(event.getMessage().contains(subString)) {return true;}
            }
            return false;
        }
}