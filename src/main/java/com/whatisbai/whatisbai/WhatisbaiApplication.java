package com.whatisbai.whatisbai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"com.whatisbai.Controllers", "com.whatisbai.Entities", "com.whatisbai.Services"})
@EnableJpaRepositories(basePackages = "com.whatisbai.Repositories")
@EntityScan(basePackages = "com.whatisbai.Entities")
@SpringBootApplication
public class WhatisbaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatisbaiApplication.class, args);
	}

}
