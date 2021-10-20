package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {
    private final String environment;
    private final Double metricHealthValue;

    public AppConfig(@Value("${environment}")String environment, @Value("${metric.health.value}") Double metricHealthValue) {
        this.environment = environment;
        this.metricHealthValue = metricHealthValue;
    }

    public String environment() {
        return environment;
    }

    public Double metricHealthValue() {
        return metricHealthValue;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}
