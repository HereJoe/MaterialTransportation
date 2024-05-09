package com.adl.genius;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.adl.genius.mapper")
@SpringBootApplication
public class GeniusChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeniusChatApplication.class, args);
	}

}
