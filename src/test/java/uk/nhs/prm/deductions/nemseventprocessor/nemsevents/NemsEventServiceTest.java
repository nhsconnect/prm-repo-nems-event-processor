package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private NemsEventParser nemsEventParser;

    @InjectMocks
    private NemsEventService nemsEventService;

    @Test
    void shouldPublishNonSuspensionsToTheUnhandledQueue() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.nonSuspension());
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(unhandledNemsEvent, "Non-suspension");
    }

    @Test
    void shouldPublishToSuspensionsTopicWhenMessageIsSuspension() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.suspension("111", "2023-01-01", "B12345"));
        nemsEventService.processNemsEvent("a suspension");
        verify(suspensionsEventPublisher).sendMessage("{\"lastUpdated\":\"2023-01-01\",\"previousOdsCode\":\"B12345\",\"eventType\":\"SUSPENSION\",\"nhsNumber\":\"111\"}");
    }

    @Test
    void shouldNotPublishToUnhandledTopicWhenMessageIsSuspension() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.suspension("222", "2022-10-21", "A34564"));
        nemsEventService.processNemsEvent("not sent to unhandled");
        verify(unhandledEventPublisher, times(0)).sendMessage(anyString(), anyString());
    }

    @Test
    void shouldPublishEventsToUnhandledTopicIfFailsToParse() {
        when(nemsEventParser.parse(anyString())).thenThrow(new NemsEventParseException("failed-to-parse"));
        String incomingMessage = "will throw a parse exception";
        nemsEventService.processNemsEvent(incomingMessage);
        verify(unhandledEventPublisher, times(1)).sendMessage(incomingMessage, "NemsEventParseException: failed-to-parse");
    }
}
