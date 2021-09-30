package uk.nhs.prm.deductions.nemseventprocessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class NemsEventProcessorApplication {

    public static void main(String[] args) {
        log.info("IN MAIN");
        SpringApplication.run(NemsEventProcessorApplication.class, args);
    }

}
