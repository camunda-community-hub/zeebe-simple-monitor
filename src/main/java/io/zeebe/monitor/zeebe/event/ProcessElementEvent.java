package io.zeebe.monitor.zeebe.event;

public record ProcessElementEvent(
    String process, String elementType, String elementId, String intent) {}
