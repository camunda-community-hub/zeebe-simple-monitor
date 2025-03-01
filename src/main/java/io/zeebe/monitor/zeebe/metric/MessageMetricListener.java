package io.zeebe.monitor.zeebe.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.zeebe.monitor.zeebe.event.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageMetricListener {

  private final Counter counter;

  @Autowired
  public MessageMetricListener(MeterRegistry meterRegistry) {
    this.counter =
        Counter.builder("zeebemonitor_importer_message")
            .description("number of processed messages")
            .register(meterRegistry);
  }

  @EventListener
  public void onMessageEvent(MessageEvent event) {
    counter.increment();
  }
}
