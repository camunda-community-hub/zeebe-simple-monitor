package io.zeebe.monitor.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.ProcessInstanceState;
import io.zeebe.monitor.entity.QProcessInstanceEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class ProcessInstancePredicateBuilder {

  final PathBuilder<QProcessInstanceEntity> pathBuilder =
      new PathBuilder<>(
          QProcessInstanceEntity.class, QProcessInstanceEntity.processInstanceEntity.getMetadata());
  private final List<Predicate> predicates = new ArrayList<>();

  public ProcessInstancePredicateBuilder withKeys(List<Long> keys) {
    if (!CollectionUtils.isEmpty(keys)) {
      predicates.add(pathBuilder.getNumber("key", Long.class).in(keys));
    } else {
      predicates.add(Expressions.asBoolean(true).isFalse());
    }
    return this;
  }

  public ProcessInstancePredicateBuilder withStates(List<ProcessInstanceState> states) {
    if (!CollectionUtils.isEmpty(states)) {
      predicates.add(pathBuilder.getString("state").in(states.stream().map(Enum::name).toList()));
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
