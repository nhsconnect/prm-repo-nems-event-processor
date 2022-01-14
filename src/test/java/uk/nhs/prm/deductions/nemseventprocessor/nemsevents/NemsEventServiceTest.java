package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.audit.AuditService;
import uk.nhs.prm.deductions.nemseventprocessor.dlq.DeadLetterQueuePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.suspensions.SuspensionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NemsEventServiceTest {

    @Mock
    private UnhandledEventPublisher unhandledEventPublisher;
    @Mock
    private SuspensionsEventPublisher suspensionsEventPublisher;
    @Mock
    private DeadLetterQueuePublisher deadLetterQueuePublisher;
    @Mock
    private NemsEventParser nemsEventParser;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private NemsEventService nemsEventService;

    @Test
    void shouldPublishNonSuspensionsToTheUnhandledQueue() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.nonSuspension("1234567"));
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(unhandledNemsEvent, "Non-suspension");
    }

    @Test
    void shouldPublishToSuspensionsTopicWhenMessageIsSuspension() {
        NemsEventMessage nemsEventMessage = NemsEventMessage.suspension("111", "2023-01-01", "B12345", "123456");
        when(nemsEventParser.parse(anyString())).thenReturn(nemsEventMessage);
        String message = "a suspension";
        nemsEventService.processNemsEvent(message);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        verify(suspensionsEventPublisher).sendMessage(nemsEventMessage);
    }

    @Test
    void shouldNotPublishToUnhandledTopicWhenMessageIsSuspension() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.suspension("222", "2022-10-21", "A34564", "123456"));
        String message = "not sent to unhandled";
        nemsEventService.processNemsEvent(message);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        verifyNoInteractions(unhandledEventPublisher);
    }

    @Test
    void shouldPublishEventsToUnhandledTopicIfFailsToParse() {
        when(nemsEventParser.parse(anyString())).thenThrow(new NemsEventParseException("failed-to-parse"));
        String message = "will throw a parse exception";
        nemsEventService.processNemsEvent(message);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        verify(deadLetterQueuePublisher, times(1)).sendMessage(message, "NemsEventParseException: failed-to-parse");
    }
}
