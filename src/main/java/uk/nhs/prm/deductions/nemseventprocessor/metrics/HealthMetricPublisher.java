package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
                .timestamp(awsCompatibleNow())
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

    // why? here's why: https://forums.aws.amazon.com/thread.jspa?threadID=328321
    private Instant awsCompatibleNow() {
        return Instant.parse(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT));
    }
}
