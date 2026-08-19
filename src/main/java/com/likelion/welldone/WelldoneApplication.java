package com.likelion.welldone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WelldoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(WelldoneApplication.class, args);
	}

}