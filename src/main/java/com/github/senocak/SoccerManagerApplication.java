package com.github.senocak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class SoccerManagerApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplicationBuilder(SoccerManagerApplication.class)
				.bannerMode(Banner.Mode.CONSOLE)
				.logStartupInfo(true)
				.build();
		app.run(args);
	}
}
