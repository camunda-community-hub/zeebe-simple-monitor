package io.zeebe.monitor.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.QIncidentEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class IncidentEntityPredicatesBuilder {
  final PathBuilder<QIncidentEntity> pathBuilder = new PathBuilder<>(QIncidentEntity.class, QIncidentEntity.incidentEntity.getMetadata());
  private final List<Predicate> predicates = new ArrayList<>();

  public IncidentEntityPredicatesBuilder onlyUnresolved() {
    predicates.add(pathBuilder.getNumber("resolved", Long.class).isNull());
    return this;
  }

  public IncidentEntityPredicatesBuilder withProcessId(String processId) {
    if (!isEmpty(processId)) {
      predicates.add(pathBuilder.getString("bpmnProcessId").containsIgnoreCase(processId));
    }
    return this;
  }

  public IncidentEntityPredicatesBuilder withErrorType(String errorType) {
    if (!isEmpty(errorType)) {
      predicates.add(pathBuilder.getString("errorType").containsIgnoreCase(errorType));
    }
    return this;
  }

  public IncidentEntityPredicatesBuilder createdAfter(String timestamp) {
    if (!isEmpty(timestamp)) {
      final Optional<Long> created = parseIsoToUtcMillis(timestamp);
      created.ifPresent(utcMillis -> predicates.add(pathBuilder.getNumber("created", Long.class).goe(utcMillis)));
    }
    return this;
  }

  public IncidentEntityPredicatesBuilder createdBefore(String timestamp) {
    if (!isEmpty(timestamp)) {
      final Optional<Long> created = parseIsoToUtcMillis(timestamp);
      created.ifPresent(utcMillis -> predicates.add(pathBuilder.getNumber("created", Long.class).loe(utcMillis)));
    }
    return this;
  }

  public Predicate build() {
    BooleanExpression result = Expressions.asBoolean(true).isTrue();
    for (Predicate predicate : predicates) {
      result = result.and(predicate);
    }
    return result;
  }

  private Optional<Long> parseIsoToUtcMillis(String timestamp) {
    try {
      final ZonedDateTime zonedDateTime = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp));
      final long utcMillis = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toInstant().toEpochMilli();
      return Optional.of(utcMillis);
    } catch (DateTimeParseException ignore) {
      // ignore
    }
    return Optional.empty();
  }
}
