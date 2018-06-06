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
package io.zeebe.zeebemonitor.entity;

import java.util.UUID;

import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.springframework.data.annotation.Id;

public class RecordEntity
{
    // write Long values as numbers
    private static final JsonWriterSettings JSON_WRITER_SETTINGS = JsonWriterSettings.builder()
            .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
            .build();

    @Id
    private String id = UUID.randomUUID().toString();

    private int partitionId;
    private long position;

    private Document content;

    public RecordEntity()
    { }

    public RecordEntity(int partitionId, long position, String content)
    {
        this.partitionId = partitionId;
        this.position = position;
        this.content = Document.parse(content);
    }

    public Object getContent()
    {
        return content;
    }

    public String getContentAsJson()
    {
        return content.toJson(JSON_WRITER_SETTINGS);
    }

    public void setContent(Document content)
    {
        this.content = content;
    }

    public int getPartitionId()
    {
        return partitionId;
    }

    public void setPartitionId(int partitionId)
    {
        this.partitionId = partitionId;
    }

    public long getPosition()
    {
        return position;
    }

    public void setPosition(long position)
    {
        this.position = position;
    }

}
