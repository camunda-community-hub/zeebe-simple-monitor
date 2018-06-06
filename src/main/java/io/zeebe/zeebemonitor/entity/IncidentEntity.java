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

public class IncidentEntity
{
    private long key;

    private long workflowInstanceKey;
    private String activityId;
    private String errorType;
    private String errorMessage;

    public IncidentEntity()
    {
    }

    public IncidentEntity(long key, long workflowInstanceKey, String activityId, String errorType, String errorMessage)
    {
        this.key = key;
        this.workflowInstanceKey = workflowInstanceKey;
        this.activityId = activityId;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public String getActivityId()
    {
        return activityId;
    }

    public void setActivityId(String activityId)
    {
        this.activityId = activityId;
    }

    public String getErrorType()
    {
        return errorType;
    }

    public IncidentEntity setErrorType(String errorType)
    {
        this.errorType = errorType;
        return this;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public IncidentEntity setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
        return this;
    }

    public long getIncidentKey()
    {
        return key;
    }

    public void setKey(long incidentKey)
    {
        this.key = incidentKey;
    }

    public long getWorkflowInstanceKey()
    {
        return workflowInstanceKey;
    }

    public void setWorkflowInstanceKey(long workflowInstanceKey)
    {
        this.workflowInstanceKey = workflowInstanceKey;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (key ^ (key >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final IncidentEntity other = (IncidentEntity) obj;
        if (key != other.key)
        {
            return false;
        }
        return true;
    }

}
