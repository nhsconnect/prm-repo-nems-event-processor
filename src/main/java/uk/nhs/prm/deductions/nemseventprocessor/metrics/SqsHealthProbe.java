package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.stereotype.Component;

@Component
public class SqsHealthProbe {
    public boolean youHealthyYeah() {
        return true;
    }
}
