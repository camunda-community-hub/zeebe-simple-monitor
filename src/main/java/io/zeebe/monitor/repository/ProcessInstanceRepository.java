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

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

public interface ProcessInstanceRepository
    extends PagingAndSortingRepository<ProcessInstanceEntity, Long>, CrudRepository<ProcessInstanceEntity, Long> {

  Page<ProcessInstanceEntity> findByProcessDefinitionKey(
      long processDefinitionKey, Pageable pageable);

  Optional<ProcessInstanceEntity> findByKey(long key);

  long countByProcessDefinitionKey(long processDefinitionKey);

  long countByProcessDefinitionKeyAndEndIsNotNull(long processDefinitionKey);

  long countByProcessDefinitionKeyAndEndIsNull(long processDefinitionKey);

  Page<ProcessInstanceEntity> findByParentProcessInstanceKey(
      long parentProcessInstanceKey, Pageable pageable);

  long countByParentProcessInstanceKey(long parentProcessInstanceKey);

  @Query(value = "SELECT p.KEY_ FROM PROCESS_INSTANCE p" +
          " WHERE p.STATE_ = :state" +
          " AND p.END_ IS NOT NULL" +
          " AND (CAST(:currentTimeMillis AS BIGINT) - p.END_) > :expirationIntervalMillis"
          , nativeQuery = true)
  List<Long> findExpiredIdsByStateAndTime(
          @Param("state") String state,
          @Param("currentTimeMillis") long currentTimeMillis,
          @Param("expirationIntervalMillis") long expirationIntervalMillis);

  @Transactional(SUPPORTS)
  CompletableFuture<Void> deleteByKeyIn(Collection<Long> key);
}
