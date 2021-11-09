package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.suspensions.SuspensionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NemsEventService {

    private final UnhandledEventPublisher unhandledEventPublisher;
    private final NemsEventParser parser;
    private final SuspensionsEventPublisher suspensionsEventPublisher;

    public void processNemsEvent(String message) {
        try {
            NemsEventMessage parsedMessage = parser.parse(message);
            if (parsedMessage.isSuspension()) {
                log.info("SUSPENSION event - sending to suspensions sns topic");
                suspensionsEventPublisher.sendMessage(toJson(parsedMessage.exposeSensitiveData()));
                return;
            }
            log.info("NON-SUSPENSION event - sending to unhandled sns topic");
            unhandledEventPublisher.sendMessage(message, "Non-suspension");
        }
        catch (Exception e) {
            log.info("PROCESSING FAILED - sending to unhandled sns topic");
            unhandledEventPublisher.sendMessage(message, e.getMessage());
        }
    }

    private String toJson(Map<String, String> data) {
        return new Gson().toJson(data);
    }
}
