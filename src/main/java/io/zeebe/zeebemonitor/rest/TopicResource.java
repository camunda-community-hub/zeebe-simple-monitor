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
package io.zeebe.zeebemonitor.rest;

import java.util.List;

import io.zeebe.zeebemonitor.repository.PartitionRepository;
import io.zeebe.zeebemonitor.zeebe.TopicService;
import io.zeebe.zeebemonitor.zeebe.ZeebeConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/topics")
public class TopicResource
{

    @Autowired
    private PartitionRepository partitionRepository;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ZeebeConnectionService zeebeConnections;

    @RequestMapping("/")
    public List<String> getTopics()
    {
        return partitionRepository.getTopicNames();
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public void createTopic(@RequestBody TopicDto topicCommand)
    {
        zeebeConnections
            .getClient()
            .newCreateTopicCommand()
            .name(topicCommand.getTopicName())
            .partitions(topicCommand.getPartitionCount())
            .replicationFactor(topicCommand.getReplicationFactor())
            .send()
            .join();

        topicService.synchronizeAsync();
    }

}
