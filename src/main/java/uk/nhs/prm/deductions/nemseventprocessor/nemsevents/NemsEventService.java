package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

@Service
@RequiredArgsConstructor
public class NemsEventService {

    private final UnhandledEventPublisher unhandledEventPublisher;

    public void processNemsEvent(String message) {
        unhandledEventPublisher.sendMessage(message);
    }
}
