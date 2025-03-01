package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.ErrorEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ErrorMetricListener {

  private final Counter counter;

  @Autowired
  public ErrorMetricListener(MeterRegistry meterRegistry) {
    this.counter =
        Counter.builder("zeebemonitor_importer_error")
            .description("number of processed errors")
            .register(meterRegistry);
  }

  @EventListener
  public void onErrorCreated(ErrorEvent event) {
    counter.increment();
  }
}
