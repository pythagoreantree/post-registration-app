package ru.post.PostRegistrationApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PostRegistrationAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostRegistrationAppApplication.class, args);
	}

}
