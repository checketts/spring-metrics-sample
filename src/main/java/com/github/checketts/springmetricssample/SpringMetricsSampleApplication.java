package com.github.checketts.springmetricssample;

import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.metrics.instrument.Counter;
import org.springframework.metrics.instrument.MeterRegistry;
import org.springframework.metrics.instrument.Timer;
import org.springframework.metrics.instrument.binder.LogbackMetrics;
import org.springframework.metrics.instrument.binder.MetricsTurboFilter;
import org.springframework.metrics.instrument.prometheus.EnablePrometheusScraping;
import org.springframework.metrics.instrument.prometheus.PrometheusMeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnablePrometheusScraping
public class SpringMetricsSampleApplication {

    @Bean
    public MeterRegistry meterRegistry() { return new PrometheusMeterRegistry(); }

    @Bean
    public LogbackMetrics logbackMetrics() {
        return new LogbackMetrics();
    }

    @RestController
    public class MyController {
        List<String> people = new ArrayList<String>();
        Counter steveCounter;
        Timer findPersonTimer;

        public MyController(MeterRegistry registry) {
            // registers a gauge to observe the size of the population
            registry.collectionSize("population", people);

            // register a counter of questionable usefulness
            steveCounter = registry.counter("find_steve");

            // register a timer -- though for request timing it is easier to use @Timed
            findPersonTimer = registry.timer("http_requests", "method", "GET");
        }

        @GetMapping("/api/person")
        public String findPerson(@RequestParam String q) throws Exception {
            return findPersonTimer.record(() -> { // use the timer!
                if(q.toLowerCase().contains("steve")) {
                    steveCounter.increment(); // use the counter
                }

                return people.stream().filter(p -> q.equals(p)).findAny().orElse(null);
            });
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(SpringMetricsSampleApplication.class, args);
	}
}
