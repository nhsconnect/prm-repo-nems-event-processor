package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.deductions.DeductionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NemsEventServiceTest {

    @Mock
    private UnhandledEventPublisher unhandledEventPublisher;
    @Mock
    private DeductionsEventPublisher deductionsEventPublisher;
    @Mock
    private NemsEventParser nemsEventParser;

    @InjectMocks
    private NemsEventService nemsEventService;

    @Test
    void shouldPublishNonDeductionsToTheUnhandledQueue() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.nonDeduction());
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(unhandledNemsEvent, "Non-deduction");
    }

    @Test
    void shouldPublishToDeductionsTopicWhenMessageIsDeduction() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.deduction("111", "2023-01-01", "B12345"));
        nemsEventService.processNemsEvent("a deduction");
        verify(deductionsEventPublisher).sendMessage("{\"lastUpdated\":\"2023-01-01\",\"previousOdsCode\":\"B12345\",\"eventType\":\"DEDUCTION\",\"nhsNumber\":\"111\"}");
    }

    @Test
    void shouldNotPublishToUnhandledTopicWhenMessageIsDeduction() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.deduction("222", "2022-10-21", "A34564"));
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
