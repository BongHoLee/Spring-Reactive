package com.reactive.practice;

import java.net.URI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootApplication
public class ReactivePracticeApplication {
	private URI baseUri = UriComponentsBuilder.newInstance().scheme("http")
			.host("worldtimeapi.org")
			.port(80)
			.path("/api/timezone/Asia/Seoul")
			.build()
			.encode()
			.toUri();

	public static void main(String[] args) {
		SpringApplication.run(ReactivePracticeApplication.class, args);
	}


	@Bean
	public RestTemplateBuilder restTemplate() {
		return new RestTemplateBuilder();
	}
}
