package io.zeebe.monitor.zeebe.event;

import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;

public record ProcessInstanceEvent(ProcessInstanceIntent intent) {}
