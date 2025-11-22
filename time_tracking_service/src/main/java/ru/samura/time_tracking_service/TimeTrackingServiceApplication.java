package ru.samura.time_tracking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TimeTrackingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeTrackingServiceApplication.class, args);
	}

}
