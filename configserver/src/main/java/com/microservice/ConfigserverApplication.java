package com.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@EnableConfigServer
@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class ConfigserverApplication {
	@RequestMapping("/")
	public String home() {
		return "config server!";
	}

	public static void main(String[] args) {
		SpringApplication.run(ConfigserverApplication.class, args);
	}
}
