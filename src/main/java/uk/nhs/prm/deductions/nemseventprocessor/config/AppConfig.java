package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    @Value("${environment}")
    @SuppressWarnings("unused")
    private String environment;

    @Value("${metric.health.value}")
    @SuppressWarnings("unused")
    private Double metricHealthValue;

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
