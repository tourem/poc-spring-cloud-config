package com.larbotech.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ConfigClientApplication {

	@Value("${foo.bar}")
	private  String foo;

	@Value("${secret}")
	String secret;

	public static void main(String[] args) {
		SpringApplication.run(ConfigClientApplication.class, args);
	}

	@RequestMapping("/")
	public String home(){
		return   String.format("Hello %s! My secret is %s.", foo, secret);
	}
}