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
    void shouldCallPublisherWithUnhandledNemsEvents() {
        when(nemsEventParser.parse(anyString())).thenReturn(NemsEventMessage.nonDeduction());
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(unhandledNemsEvent);
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
        verify(unhandledEventPublisher, times(0)).sendMessage(anyString());
    }
}
