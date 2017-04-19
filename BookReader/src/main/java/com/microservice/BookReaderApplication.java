package com.microservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
////import org.springframework.boot.context.embedded.ErrorPage;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpStatus;

@SpringBootApplication
@EnableDiscoveryClient
public class BookReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookReaderApplication.class, args);
	}
	@Autowired
	void setEnviroment(Environment env) {
		System.out.println("myname from env: "
				+ env.getProperty("myname"));
	}
//	@Bean
//	public EmbeddedServletContainerCustomizer containerCustomizer() {
//
//		return (container -> {
//			ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
//			ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
//			ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
//
//			container.addErrorPages(error401Page, error404Page, error500Page);
//		});
//	}
}
