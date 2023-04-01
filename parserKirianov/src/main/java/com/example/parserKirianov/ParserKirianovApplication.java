package com.example.parserKirianov;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParserKirianovApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParserKirianovApplication.class, args);
		WebDriverManager.chromedriver().setup();
	}

}
