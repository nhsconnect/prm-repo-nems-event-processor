package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;
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
        String parsedMessageAsJson = new Gson().toJson(parsedMessage.exposeSensitiveData());
        if (parsedMessage.isDeduction()) {
            deductionEventPublisher.sendMessage(parsedMessageAsJson);
        } else {
            unhandledEventPublisher.sendMessage(message);
        }
    }
}
