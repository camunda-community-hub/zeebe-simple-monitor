package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.MessageSubscriptionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageSubscriptionMetricListener {

  private final Counter subsCounter;
  private final Counter eventCounter;

  @Autowired
  public MessageSubscriptionMetricListener(MeterRegistry meterRegistry) {
    this.subsCounter =
        Counter.builder("zeebemonitor_importer_message_subscription")
            .description("number of processed message subscriptions")
            .register(meterRegistry);
    this.eventCounter =
        Counter.builder("zeebemonitor_importer_message_start_event_subscription")
            .description("number of processed message start events")
            .register(meterRegistry);
  }

  @EventListener
  public void onMessageSubscriptionEvent(MessageSubscriptionEvent event) {
    if (event.startEvent()) {
      eventCounter.increment();
    } else {
      subsCounter.increment();
    }
  }
}
