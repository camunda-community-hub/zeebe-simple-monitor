package io.zeebe.monitor.zeebe.metric;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.IncidentEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentMetricListener {

  private final MeterRegistry registry;
  private final Map<IncidentEvent, Counter> createdCounters = new ConcurrentHashMap<>();
  private final Map<IncidentEvent, Counter> resolvedCounters = new ConcurrentHashMap<>();

  @Autowired
  public IncidentMetricListener(MeterRegistry meterRegistry) {
    this.registry = meterRegistry;
  }

  @EventListener
  public void onIncidentEvent(IncidentEvent event) {
    if (event.intent() == IncidentIntent.CREATED) {
      var counter =
          createdCounters.computeIfAbsent(
              event,
              (it) -> {
                return Counter.builder("zeebemonitor_importer_incident")
                    .tag("action", "created")
                    .tag("process", event.process())
                    .tag("elementId", event.elementId())
                    .description("number of processed incidents")
                    .register(registry);
              });
      counter.increment();
    } else if (event.intent() == IncidentIntent.RESOLVED) {
      var counter =
          resolvedCounters.computeIfAbsent(
              event,
              (it) -> {
                return Counter.builder("zeebemonitor_importer_incident")
                    .tag("action", "resolved")
                    .tag("process", event.process())
                    .tag("elementId", event.elementId())
                    .description("number of processed incidents")
                    .register(registry);
              });
      counter.increment();
    }
  }
}
