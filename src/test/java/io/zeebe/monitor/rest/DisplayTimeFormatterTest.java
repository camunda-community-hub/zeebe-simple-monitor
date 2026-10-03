package io.zeebe.monitor.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisplayTimeFormatterTest {

  private TimeZone originalTimezone;

  @BeforeEach
  void rememberDefaultTimezone() {
    originalTimezone = TimeZone.getDefault();
  }

  @AfterEach
  void restoreDefaultTimezone() {
    TimeZone.setDefault(originalTimezone);
  }

  @Test
  void format_timestamp_in_UTC() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    final var formatter = new DisplayTimeFormatter();
    final long timestamp = Instant.parse("2026-08-23T12:00:00.123Z").toEpochMilli();

    assertThat(formatter.format(timestamp)).isEqualTo("2026-08-23T12:00:00.123Z");
  }

  @Test
  void format_timestamp_in_JVM_default_timezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    final var formatter = new DisplayTimeFormatter();
    final long timestamp = Instant.parse("2026-08-23T12:00:00Z").toEpochMilli();

    assertThat(formatter.format(timestamp)).isEqualTo("2026-08-23T17:30:00+05:30");
  }

  @Test
  void apply_daylight_saving_time_for_region_timezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
    final var formatter = new DisplayTimeFormatter();
    final long winterTimestamp = Instant.parse("2026-01-15T12:00:00Z").toEpochMilli();
    final long summerTimestamp = Instant.parse("2026-07-15T12:00:00Z").toEpochMilli();

    assertThat(formatter.format(winterTimestamp)).isEqualTo("2026-01-15T13:00:00+01:00");
    assertThat(formatter.format(summerTimestamp)).isEqualTo("2026-07-15T14:00:00+02:00");
  }
}
