package io.zeebe.monitor.querydsl;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.QProcessEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class ProcessEntityPredicatesBuilder {

  final PathBuilder<QProcessEntity> pathBuilder =
      new PathBuilder<>(QProcessEntity.class, QProcessEntity.processEntity.getMetadata());

  private final List<Predicate> predicates = new ArrayList<>();

  public ProcessEntityPredicatesBuilder withBpmnProcessIdPrefix(String processId) {
    if (!isEmpty(processId)) {
      predicates.add(pathBuilder.getString("bpmnProcessId").startsWithIgnoreCase(processId));
    }
    return this;
  }

  public ProcessEntityPredicatesBuilder withKeys(Collection<Long> keys) {
    if (!CollectionUtils.isEmpty(keys)) {
      predicates.add(pathBuilder.getNumber("key", Long.class).in(keys));
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
}
