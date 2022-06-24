package com.debijenkorf.service.debijenkorfservice;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class DebijenkorfServiceApplication {
	@Bean
	@LoadBalanced
	@Qualifier("restTemplateExternal")
	public RestTemplate restTemplateExternal() {
		return new RestTemplate();
	}

	@Bean
	@Qualifier("restTemplateNormal")
	public RestTemplate restTemplateNormal() {
		return new RestTemplate();
	}
	public static void main(String[] args) {
		SpringApplication.run(DebijenkorfServiceApplication.class, args);
	}

}
