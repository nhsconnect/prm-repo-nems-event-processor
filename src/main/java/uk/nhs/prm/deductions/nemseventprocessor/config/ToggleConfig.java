package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToggleConfig {

    @Value("${toggle.canProcessReregistrations}")
    private boolean canProcessReregistrations;

    public boolean canProcessReregistrations() {
        return canProcessReregistrations;
    }
}
