package com.UNI_FYP.AI_Based_Road_Construction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class  AiBasedRoadConstructionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBasedRoadConstructionApplication.class, args);
	}

}
