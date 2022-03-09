package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.springframework.context.annotation.Bean;

public class StubbedNemsEventServiceConfig {

    private static StubbedNemsEventService stubbedNemsEventService = new StubbedNemsEventService();

    public static void throwOnProcessNextMessage() {
        stubbedNemsEventService.throwOnProcessEvent();
    }

    @Bean
    public static NemsEventHandler stubbedNemsEventService() {
        return stubbedNemsEventService;
    }

    public static void waitUntilProcessedMessage() {
        stubbedNemsEventService.waitUntilProcessed();
    }
}
