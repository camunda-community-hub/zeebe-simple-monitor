/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.VariableEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.List;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;


public interface VariableRepository extends CrudRepository<VariableEntity, String> {

  List<VariableEntity> findByProcessInstanceKeyOrderByTimestampAscIdAsc(long processInstanceKey);

  long countByProcessInstanceKey(long processInstanceKey);

  @Transactional(SUPPORTS)
  CompletableFuture<Void> deleteByProcessInstanceKeyIn(Collection<Long> processInstanceKeys);
}
