package com.example.hallyuclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HallyuclassApplication {

	public static void main(String[] args) {
		SpringApplication.run(HallyuclassApplication.class, args);
	}

}
