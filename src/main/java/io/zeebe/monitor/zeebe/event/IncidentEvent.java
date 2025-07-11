package io.zeebe.monitor.zeebe.event;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;

public record IncidentEvent(String process, String elementId, IncidentIntent intent) {}
