package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsConfig {
    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public SnsClient configureClient() {
        return SnsClient.builder().region(Region.of(awsRegion)).build();
    }
}
