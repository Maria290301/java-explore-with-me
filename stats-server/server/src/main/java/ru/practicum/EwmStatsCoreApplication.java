package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class EwmStatsCoreApplication {
    public static void main(String[] args) {
        log.info("Starting stats server...");
        SpringApplication.run(EwmStatsCoreApplication.class, args);
        log.info("Stats server started successfully.");
    }
}
