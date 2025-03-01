package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.JobEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class JobMetricListener {

  private final Counter counter;

  @Autowired
  public JobMetricListener(MeterRegistry meterRegistry) {
    this.counter =
        Counter.builder("zeebemonitor_importer_job")
            .description("number of processed jobs")
            .register(meterRegistry);
  }

  @EventListener
  public void onJobEvent(JobEvent event) {
    counter.increment();
  }
}
