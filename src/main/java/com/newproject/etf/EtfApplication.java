package com.newproject.etf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // ✅ 캐시 기능 활성화
public class EtfApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtfApplication.class, args);
	}

}
