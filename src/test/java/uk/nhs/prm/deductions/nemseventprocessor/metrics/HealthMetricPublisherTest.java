package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthMetricPublisherTest {

    @Test
    public void shouldSetHealthMetricToZeroForUnhealthyIfSqsConnectionIsUnhealthy() {
        MetricPublisher metricPublisher = Mockito.mock(MetricPublisher.class);

        SqsHealthProbe sqsHealthProbe = Mockito.mock(SqsHealthProbe.class);
        when(sqsHealthProbe.youHealthyYeah()).thenReturn(false);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher, sqsHealthProbe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher).publishMetric("Health", 0.0);
    }

    @Test
    public void shouldSetHealthMetricToOneIfSqsConnectionIsHealthy() {
        MetricPublisher metricPublisher = Mockito.mock(MetricPublisher.class);

        SqsHealthProbe sqsHealthProbe = Mockito.mock(SqsHealthProbe.class);
        when(sqsHealthProbe.youHealthyYeah()).thenReturn(true);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher, sqsHealthProbe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher).publishMetric("Health", 1.0);
    }

}
