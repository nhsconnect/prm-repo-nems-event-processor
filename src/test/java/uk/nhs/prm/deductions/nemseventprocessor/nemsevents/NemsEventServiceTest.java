package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NemsEventServiceTest {

    @Mock
    private UnhandledEventPublisher unhandledEventPublisher;

    @InjectMocks
    private NemsEventService nemsEventService;

    @Test
    void shouldCallPublisherWithUnhandledNemsEvents() {
        String unhandledNemsEvent = "unhandledNemsEvent";
        nemsEventService.processNemsEvent(unhandledNemsEvent);
        verify(unhandledEventPublisher).sendMessage(unhandledNemsEvent);
    }
}
