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
package io.zeebe.zeebemonitor.zeebe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.zeebe.client.api.commands.Partition;
import io.zeebe.client.api.commands.Topic;
import io.zeebe.protocol.Protocol;
import io.zeebe.zeebemonitor.entity.PartitionEntity;
import io.zeebe.zeebemonitor.repository.PartitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TopicService
{
    @Autowired
    private PartitionRepository partitionRepository;

    @Autowired
    private ZeebeConnectionService connectionService;

    @Autowired
    private ZeebeSubscriber subscriber;

    @Async
    public void synchronizeAsync()
    {
        synchronizeWithBroker();
    }

    public void synchronizeWithBroker()
    {
        final List<Topic> topics = connectionService
            .getClient()
            .newTopicsRequest()
            .send()
            .join()
            .getTopics();

        final List<Partition> partitions = topics
                .stream()
                .flatMap(t -> t.getPartitions().stream())
                .collect(Collectors.toList());

        final List<Integer> availablePartitions = new ArrayList<>();
        for (PartitionEntity partitionEntity : partitionRepository.findAll())
        {
            availablePartitions.add(partitionEntity.getId());
        }

        partitions.removeIf(p -> availablePartitions.contains(p.getId()));

        partitions.forEach(p ->
        {
            final PartitionEntity partitionEntity = new PartitionEntity();
            partitionEntity.setId(p.getId());
            partitionEntity.setTopicName(p.getTopicName());

            partitionRepository.save(partitionEntity);
        });

        partitions
            .stream()
            .map(Partition::getTopicName)
            .filter(t -> !Protocol.SYSTEM_TOPIC.equals(t))
            .distinct()
            .forEach(newTopic -> subscriber.openSubscription(newTopic));
    }

}
