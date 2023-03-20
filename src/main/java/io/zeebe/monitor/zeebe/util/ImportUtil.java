package io.zeebe.monitor.zeebe.util;

import io.zeebe.exporter.proto.Schema;

import java.util.function.Consumer;
import java.util.function.Function;

public class ImportUtil {
  public static <T> void ifEvent(
      final T record,
      final Function<T, Schema.RecordMetadata> extractor,
      final Consumer<T> consumer) {
    final var metadata = extractor.apply(record);
    if (isEvent(metadata)) {
      consumer.accept(record);
    }
  }
  public static <T> boolean ifEvent(
      final T record,
      final Function<T, Schema.RecordMetadata> extractor) {
    final var metadata = extractor.apply(record);
    return isEvent(metadata);
  }
  public static boolean isEvent(final Schema.RecordMetadata metadata) {
    return metadata.getRecordType() == Schema.RecordMetadata.RecordType.EVENT;
  }
}
