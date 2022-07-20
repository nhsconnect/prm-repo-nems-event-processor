package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.audit.AuditService;
import uk.nhs.prm.deductions.nemseventprocessor.config.ToggleConfig;
import uk.nhs.prm.deductions.nemseventprocessor.dlq.DeadLetterQueuePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.reregistration.ReRegistrationEvent;
import uk.nhs.prm.deductions.nemseventprocessor.reregistration.ReRegistrationEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.suspensions.SuspensionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.deductions.nemseventprocessor.audit.AuditMessageStatus.NO_ACTION_NON_SUSPENSION;

@ExtendWith(MockitoExtension.class)
class NemsEventServiceTest {

    @Mock
    private UnhandledEventPublisher unhandledEventPublisher;
    @Mock
    private SuspensionsEventPublisher suspensionsEventPublisher;
    @Mock
    private ReRegistrationEventPublisher reRegistrationEventPublisher;
    @Mock
    private DeadLetterQueuePublisher deadLetterQueuePublisher;
    @Mock
    private NemsEventParser nemsEventParser;
    @Mock
    private AuditService auditService;
    @Mock
    private ToggleConfig toggleConfig;

    @InjectMocks
    private NemsEventService nemsEventService;

    @Test
    void shouldPublishNonSuspensionsToTheUnhandledQueue() {
        String nemsMessageId = "1234567";
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.nonSuspension(nemsMessageId));
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        NonSuspendedMessage expectedMessage = new NonSuspendedMessage(nemsMessageId, NO_ACTION_NON_SUSPENSION);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(expectedMessage, "Non-suspension");
    }

    @Test
    void shouldPublishToSuspensionsTopicWhenMessageIsSuspension() {
        var nemsEventMessage = NemsEventMessage.suspension("111", "2023-01-01", "B12345", "123456");
        when(nemsEventParser.parse(anyString())).thenReturn(nemsEventMessage);
        var message = "a suspension";
        var suspendedMessage = new SuspendedMessage(nemsEventMessage);

        nemsEventService.processNemsEvent(message);

        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        verify(suspensionsEventPublisher).sendMessage(suspendedMessage);
    }

    @Test
    void shouldPublishToReRegistrationTopicWhenMessageIsReRegistrationAndToggleIsTrue() {
        when(toggleConfig.canProcessReregistrations()).thenReturn(true);
        NemsEventMessage nemsEventMessage = NemsEventMessage.reRegistration("111", "2023-01-01", "B12345", "123456");
        when(nemsEventParser.parse(anyString())).thenReturn(nemsEventMessage);
        String message = "a suspension";
        nemsEventService.processNemsEvent(message);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        var reRegistrationEvent = new ReRegistrationEvent(nemsEventMessage);
        verify(reRegistrationEventPublisher).sendMessage(reRegistrationEvent);
    }

    @Test
    void shouldPublishToUnhandledTopicForReRegistrationWhenMessageIsReRegistrationAndToggleIsFalse() {
        String nemsMessageId = "1234567";
        when(toggleConfig.canProcessReregistrations()).thenReturn(false);
        NemsEventMessage nemsEventMessage = NemsEventMessage.reRegistration("111", "2023-01-01", "B12345", nemsMessageId);
        when(nemsEventParser.parse(anyString())).thenReturn(nemsEventMessage);
        String message = "a suspension";
        nemsEventService.processNemsEvent(message);
        verify(auditService).extractNemsMessageIdAndPublishAuditMessage(message);
        verifyNoInteractions(reRegistrationEventPublisher);

        NonSuspendedMessage expectedMessage = new NonSuspendedMessage(nemsMessageId, NO_ACTION_NON_SUSPENSION);
        verify(unhandledEventPublisher).sendMessage(expectedMessage, "Non-suspension");
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
    void shouldNotPublishToUnhandledTopicWhenMessageIsReRegistration() {
        when(toggleConfig.canProcessReregistrations()).thenReturn(true);
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.reRegistration("222", "2022-10-21", "A34564", "123456"));
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
