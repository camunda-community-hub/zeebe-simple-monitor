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

import io.zeebe.zeebemonitor.entity.RecordEntity;
import io.zeebe.zeebemonitor.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequestMapping("/api/records")
public class RecordResource
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RecordRepository recordRepository;

    @RequestMapping("/")
    public Iterable<RecordEntity> getRecords()
    {
        return recordRepository.findAll();
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Iterable<RecordEntity> getRecords(@RequestBody String query)
    {
        return mongoTemplate.find(new BasicQuery(query), RecordEntity.class);
    }

    @RequestMapping(value = "/count", method = RequestMethod.POST)
    public long getRecordCount(@RequestBody String query)
    {
        return mongoTemplate.count(new BasicQuery(query), RecordEntity.class);
    }

}
