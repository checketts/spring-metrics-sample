package com.github.checketts.springmetricssample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.metrics.instrument.prometheus.EnablePrometheusScraping;

@SpringBootApplication
@EnablePrometheusScraping
public class SpringMetricsSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMetricsSampleApplication.class, args);
	}
}
