package com.github.checketts.springmetricssample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.metrics.annotation.Timed;
import org.springframework.metrics.instrument.Counter;
import org.springframework.metrics.instrument.MeterRegistry;
import org.springframework.metrics.instrument.Timer;
import org.springframework.metrics.instrument.binder.LogbackMetrics;
import org.springframework.metrics.instrument.prometheus.EnablePrometheusScraping;
import org.springframework.metrics.instrument.prometheus.PrometheusMeterRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnablePrometheusScraping
@EnableScheduling
@EnableAspectJAutoProxy
public class SpringMetricsSampleApplication {
    private static final Logger LOG = LoggerFactory.getLogger(SpringMetricsSampleApplication.class);

    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry();
    }

    @Bean
    public LogbackMetrics logbackMetrics() {
        return new LogbackMetrics();
    }

    @Bean
    public ThreadPoolTaskExecutor tpExec(MeterRegistry registry){
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(10);
        exec.setMaxPoolSize(10);
        exec.initialize();

        Timer t = registry.timer("tp_exec");
        registry.monitor("tp_exec_exec", exec.getThreadPoolExecutor());
        exec.setTaskDecorator(new TaskDecorator() {
            @Override
            public Runnable decorate(Runnable runnable) {
                return t.wrap(runnable);
            }
        });
        return exec;
    }


    @Bean
    public ThreadPoolTaskScheduler tpSched(MeterRegistry registry){
        ThreadPoolTaskScheduler exec = new ThreadPoolTaskScheduler();
        exec.initialize();

        registry.monitor("tp_wsched", exec.getScheduledThreadPoolExecutor());
        //Give to metric registry to measure? exec.getScheduledThreadPoolExecutor();
        exec.setPoolSize(10);
        exec.initialize();
        return exec;
    }



	public static void main(String[] args) {
		SpringApplication.run(SpringMetricsSampleApplication.class, args);
	}
}
