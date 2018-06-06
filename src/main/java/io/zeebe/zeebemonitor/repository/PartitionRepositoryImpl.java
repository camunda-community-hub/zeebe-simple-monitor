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
package io.zeebe.zeebemonitor.repository;

import java.util.List;
import java.util.stream.Collectors;

import io.zeebe.protocol.Protocol;
import io.zeebe.zeebemonitor.entity.PartitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PartitionRepositoryImpl implements PartitionRepositoryCustom
{
    @Autowired
    private PartitionRepository repository;

    @Override
    public List<String> getTopicNames()
    {
        return repository
                .findAll()
                .stream()
                .filter(p -> p.getId() != Protocol.SYSTEM_PARTITION)
                .map(PartitionEntity::getTopicName)
                .distinct()
                .collect(Collectors.toList());
    }
}
