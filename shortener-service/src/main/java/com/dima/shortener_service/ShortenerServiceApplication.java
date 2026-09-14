package com.dima.shortener_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShortenerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortenerServiceApplication.class, args);
	}

}
