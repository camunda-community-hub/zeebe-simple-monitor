package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.VariableEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VariableMetricListener {

  private final Counter variableCreatedCounter;
  private final Counter variableUpdatedCounter;

  @Autowired
  public VariableMetricListener(MeterRegistry meterRegistry) {
    this.variableCreatedCounter =
        Counter.builder("zeebemonitor_importer_variable")
            .tag("action", "imported")
            .description("number of processed variables")
            .register(meterRegistry);
    this.variableUpdatedCounter =
        Counter.builder("zeebemonitor_importer_variable")
            .tag("action", "updated")
            .description("number of processed variables")
            .register(meterRegistry);
  }

  @EventListener
  public void onVariableEvent(VariableEvent event) {
    if (event.updated()) {
      variableUpdatedCounter.increment();
    } else {
      variableCreatedCounter.increment();
    }
  }
}
