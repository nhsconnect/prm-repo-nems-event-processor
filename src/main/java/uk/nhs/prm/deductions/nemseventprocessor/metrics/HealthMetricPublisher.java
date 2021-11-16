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
    public static final String HEALTH_METRIC_NAME = "Health";

    private SqsHealthProbe sqsHealthProbe;
    private final MetricPublisher metricPublisher;

    @Autowired
    public HealthMetricPublisher(MetricPublisher metricPublisher, SqsHealthProbe sqsHealthProbe) {
        this.metricPublisher = metricPublisher;
        this.sqsHealthProbe = sqsHealthProbe;
    }

    @Scheduled(fixedRate = MINUTE_INTERVAL)
    public void publishHealthStatus() {
        if (sqsHealthProbe.isHealthy()) {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 1.0);
        }
        else {
            metricPublisher.publishMetric(HEALTH_METRIC_NAME, 0.0);
        }
    }
}
