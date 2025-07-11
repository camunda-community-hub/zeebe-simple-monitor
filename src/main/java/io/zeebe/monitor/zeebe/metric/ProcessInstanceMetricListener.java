package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceMetricListener {

  private final Counter instanceActivatedCounter;
  private final Counter instanceCompletedCounter;
  private final Counter instanceTerminatedCounter;

  @Autowired
  public ProcessInstanceMetricListener(MeterRegistry meterRegistry) {
    this.instanceActivatedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "activated")
            .description("number of activated process instances")
            .register(meterRegistry);
    this.instanceCompletedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "completed")
            .description("number of activated process instances")
            .register(meterRegistry);
    this.instanceTerminatedCounter =
        Counter.builder("zeebemonitor_importer_process_instance")
            .tag("action", "terminated")
            .description("number of activated process instances")
            .register(meterRegistry);
  }

  @EventListener
  public void onProcessInstanceEvent(ProcessInstanceEvent event) {
    switch (event.intent()) {
      case ELEMENT_ACTIVATED -> instanceActivatedCounter.increment();
      case ELEMENT_COMPLETED -> instanceCompletedCounter.increment();
      case ELEMENT_TERMINATED -> instanceTerminatedCounter.increment();
    }
  }
}
