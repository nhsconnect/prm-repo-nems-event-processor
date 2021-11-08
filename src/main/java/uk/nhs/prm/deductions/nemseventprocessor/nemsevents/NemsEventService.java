package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.deductions.DeductionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NemsEventService {

    private final UnhandledEventPublisher unhandledEventPublisher;
    private final NemsEventParser parser;
    private final DeductionsEventPublisher deductionEventPublisher;

    public void processNemsEvent(String message) {
        try {
            NemsEventMessage parsedMessage = parser.parse(message);
            if (parsedMessage.isDeduction()) {
                deductionEventPublisher.sendMessage(toJson(parsedMessage.exposeSensitiveData()));
                return;
            }
            unhandledEventPublisher.sendMessage(message, "Non-deduction");
        }
        catch (Exception e) {
            unhandledEventPublisher.sendMessage(message, e.getMessage());
        }
    }

    private String toJson(Map<String, String> data) {
        return new Gson().toJson(data);
    }
}
