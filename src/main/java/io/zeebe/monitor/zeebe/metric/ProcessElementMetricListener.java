package io.zeebe.monitor.zeebe.metric;

import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.ProcessElementEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessElementMetricListener {

  private final MeterRegistry registry;
  private final Map<ProcessElementEvent, Counter> counters = new ConcurrentHashMap<>();

  @Autowired
  public ProcessElementMetricListener(MeterRegistry meterRegistry) {
    this.registry = meterRegistry;
  }

  @EventListener
  public void onProcessElementEvent(ProcessElementEvent event) {
    if (!ProcessInstanceIntent.ELEMENT_COMPLETED.name().equals(event.intent())) {
      return;
    }
    var counter =
        counters.computeIfAbsent(
            event,
            (it) -> {
              return Counter.builder("zeebemonitor_importer_element_instance")
                  .description("number of processed element_instances")
                  .tag("process", event.process())
                  .tag("elementType", event.elementType())
                  .tag("elementId", event.elementId())
                  .register(registry);
            });
    counter.increment();
  }
}
