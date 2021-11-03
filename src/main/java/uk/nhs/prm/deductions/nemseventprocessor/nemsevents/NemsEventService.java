package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.deductions.DeductionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

@Service
@Slf4j
@RequiredArgsConstructor
public class NemsEventService {

    private final UnhandledEventPublisher unhandledEventPublisher;
    private final NemsEventParser parser;
    private final DeductionsEventPublisher deductionEventPublisher;

    public void processNemsEvent(String message) {
        NemsEventMessage parsedMessage = parser.parse(message);
        if(parsedMessage.isDeduction()) {
            deductionEventPublisher.sendMessage(message);
        }
        else {
            unhandledEventPublisher.sendMessage(message);
        }
    }
}
