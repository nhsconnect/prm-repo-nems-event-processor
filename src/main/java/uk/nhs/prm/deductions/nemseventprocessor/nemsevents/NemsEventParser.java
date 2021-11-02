package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.springframework.stereotype.Service;

@Service
public class NemsEventParser {
    public NemsEventMessage parse(String messageBody) {
        if (messageBody.contains("generalPractitioner")) {
            return NemsEventMessage.nonDeduction();
        }
        return NemsEventMessage.deduction();
    }
}
