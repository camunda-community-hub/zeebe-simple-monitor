package io.zeebe.monitor.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.QIncidentEntity;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class IncidentEntityPredicatesBuilder {
  final PathBuilder<QIncidentEntity> pathBuilder = new PathBuilder<>(QIncidentEntity.class, QIncidentEntity.incidentEntity.getMetadata());
  private List<Predicate> predicates = new ArrayList<>();

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

  public BooleanExpression build() {
    BooleanExpression result = Expressions.asBoolean(true).isTrue();
    for (Predicate predicate : predicates) {
      result = result.and(predicate);
    }
    return result;
  }
}
