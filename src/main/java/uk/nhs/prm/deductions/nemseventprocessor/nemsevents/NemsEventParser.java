package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.springframework.stereotype.Service;

@Service
public class NemsEventParser {
    public NemsEventMessage parse(String messageBody) {
        // stub for now
        return NemsEventMessage.nonDeduction();
    }
}
