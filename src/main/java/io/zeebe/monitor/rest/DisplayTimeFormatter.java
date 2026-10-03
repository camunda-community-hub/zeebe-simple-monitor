package io.zeebe.monitor.rest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DisplayTimeFormatter {

  private final DateTimeFormatter formatter;

  public DisplayTimeFormatter() {
    formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());
  }

  public String format(final long epochMillis) {
    return formatter.format(Instant.ofEpochMilli(epochMillis));
  }
}
