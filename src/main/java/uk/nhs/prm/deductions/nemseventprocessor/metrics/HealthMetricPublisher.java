package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HealthMetricPublisher {

    private static final int SECONDS = 1000;
    private static final int MINUTE_INTERVAL = 60 * SECONDS;
    private final AppConfig config;
    private final MetricPublisher metricPublisher;

    @Autowired
    public HealthMetricPublisher(AppConfig config, MetricPublisher metricPublisher) {
        this.metricPublisher = metricPublisher;
        this.config = config;
    }

    @Scheduled(fixedRate = MINUTE_INTERVAL)
    public void publishHealthyStatus() {
        Double healthValue = config.metricHealthValue();
        String metricName = "Health";
        metricPublisher.publishMetric(healthValue, metricName);
    }
}
