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

import org.springframework.data.annotation.Id;

public class ConfigurationEntity
{
    @Id
    private String clientId = UUID.randomUUID().toString();

    private String connectionString;

    public ConfigurationEntity()
    {
    }

    public ConfigurationEntity(String connectionString)
    {
        this.connectionString = connectionString;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getConnectionString()
    {
        return connectionString;
    }

    public void setConnectionString(String connectionString)
    {
        this.connectionString = connectionString;
    }
}
