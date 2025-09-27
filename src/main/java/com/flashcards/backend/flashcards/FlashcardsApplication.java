package com.flashcards.backend.flashcards;

import com.flashcards.backend.flashcards.config.GoogleCredentialsInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@SpringBootApplication
@EnableMongoRepositories
public class FlashcardsApplication {

	public static void main(String[] args) {
		log.info("Starting Flashcards Application...");
		SpringApplication app = new SpringApplication(FlashcardsApplication.class);
		app.addInitializers(new GoogleCredentialsInitializer());
		log.info("Google Credentials Initializer registered");
		app.run(args);
		log.info("Flashcards Application started successfully");
	}

}
