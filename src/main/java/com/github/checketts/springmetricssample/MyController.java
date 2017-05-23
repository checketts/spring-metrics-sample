package com.github.checketts.springmetricssample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.metrics.annotation.Timed;
import org.springframework.metrics.instrument.Counter;
import org.springframework.metrics.instrument.MeterRegistry;
import org.springframework.metrics.instrument.Timer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clintchecketts on 5/23/17.
 */
@RestController
public class MyController {
  private static final Logger LOG = LoggerFactory.getLogger(MyController.class);
  private final TaskExecutor executor;
  private final TaskScheduler sched;
  List<String> people = new ArrayList<>();
  Counter steveCounter;
  Timer findPersonTimer;

  @Scheduled(fixedRate = 5000)
  @Timed(value = "sample_bob_scheduled", extraTags = {"name", "bob"})
  public void runMe() {
    LOG.info("Running the runMe");
  }

  public MyController(MeterRegistry registry,
                      @Qualifier("tpExec") TaskExecutor executor,
                      @Qualifier("tpSched") TaskScheduler sched) {
    this.executor = executor;
    this.sched = sched;

    // registers a gauge to observe the size of the population
    registry.collectionSize("population", people);

    // register a counter of questionable usefulness
    steveCounter = registry.counter("find_steve");

    // register a timer -- though for request timing it is easier to use @Timed
    findPersonTimer = registry.timer("http_requests", "method", "GET");
  }

  @GetMapping("/api/person")
  public String findPerson(@RequestParam String q) throws Throwable {
    for (int i = 0; i < 30; i++) {
      final int j = i;
      sched.scheduleAtFixedRate(() -> {
        LOG.info("Fixed rate: " + j);
      }, 1000);
    }

    for (int i = 0; i < 30; i++) {
      final int j = i;
      executor.execute(() -> {
        LOG.info("Execute: " + j);
      });
    }

    return findPersonTimer.record(() -> { // use the timer!
      if (q.toLowerCase().contains("steve")) {
        steveCounter.increment(); // use the counter
      }

      return people.stream().filter(p -> q.equals(p)).findAny().orElse(null);
    });
  }
}
