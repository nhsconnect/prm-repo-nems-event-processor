package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Instant;
import java.util.ArrayList;

public class HealthMetricPublisher {

    private CloudWatchClient cloudWatchClient;

    @Autowired
    public HealthMetricPublisher(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public void publishHealthyStatus() {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(Dimension
                .builder()
                .name("Environment")
                .value("ci")
                .build());

        MetricDatum datum = MetricDatum
                .builder()
                .metricName("Health")
                .value(1.0)
                .timestamp(Instant.now())
                .dimensions(dimensions)
                .build();

        PutMetricDataRequest request =
                PutMetricDataRequest
                        .builder()
                        .namespace("PrmDeductions/NemsEventProcessor")
                        .metricData(datum)
                        .build();

        cloudWatchClient.putMetricData(request);
    }
}
