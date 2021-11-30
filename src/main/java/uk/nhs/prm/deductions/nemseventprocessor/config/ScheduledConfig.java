package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.nhs.prm.deductions.nemseventprocessor.metrics.HealthMetricPublisher;


@Configuration
@EnableScheduling
@ComponentScan("uk.nhs.prm.deductions.nemseventprocessor.metrics")
public class ScheduledConfig {
}
