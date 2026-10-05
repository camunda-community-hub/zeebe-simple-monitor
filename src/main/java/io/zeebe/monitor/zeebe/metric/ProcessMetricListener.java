package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.ProcessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessMetricListener {

  private final Counter processCounter;

  @Autowired
  public ProcessMetricListener(MeterRegistry meterRegistry) {
    this.processCounter =
        Counter.builder("zeebemonitor_importer_process")
            .description("number of processed processes")
            .register(meterRegistry);
  }

  @EventListener
  public void onProcessEvent(ProcessEvent event) {
    processCounter.increment();
  }
}
