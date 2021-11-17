package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthMetricPublisherTest {

    @Test
    public void shouldSetHealthMetricToZeroForUnhealthyIfConnectionIsUnhealthy() {
        MetricPublisher metricPublisher = Mockito.mock(MetricPublisher.class);
        List<HealthProbe> probe = new ArrayList<>();
        SqsHealthProbe sqsHealthProbe = Mockito.mock(SqsHealthProbe.class);
        SnsHealthProbe snsHealthProbe = Mockito.mock(SnsHealthProbe.class);
        probe.add(sqsHealthProbe);
        probe.add(snsHealthProbe);
        when(sqsHealthProbe.isHealthy()).thenReturn(false);
        when(snsHealthProbe.isHealthy()).thenReturn(false);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher,probe);
        healthPublisher.publishHealthStatus();

        verify(metricPublisher,times(2)).publishMetric("Health", 0.0);
    }

    @Test
    public void shouldSetHealthMetricToOneIfConnectionIsHealthy() {
        MetricPublisher metricPublisher = Mockito.mock(MetricPublisher.class);
        List<HealthProbe> probe = new ArrayList<>();
        SqsHealthProbe sqsHealthProbe = Mockito.mock(SqsHealthProbe.class);
        SnsHealthProbe snsHealthProbe = Mockito.mock(SnsHealthProbe.class);
        probe.add(sqsHealthProbe);
        probe.add(snsHealthProbe);
        when(sqsHealthProbe.isHealthy()).thenReturn(true);
        when(snsHealthProbe.isHealthy()).thenReturn(true);

        HealthMetricPublisher healthPublisher = new HealthMetricPublisher(metricPublisher, probe);
        healthPublisher.publishHealthStatus();
        verify(metricPublisher,times(2)).publishMetric("Health", 1.0);
    }

}
