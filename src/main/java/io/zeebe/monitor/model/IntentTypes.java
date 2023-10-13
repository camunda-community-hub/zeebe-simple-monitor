package io.zeebe.monitor.model;

// This was created by running DISTINCT on this column in a long-running
// simple-monitor db install
public enum IntentTypes {
    ELEMENT_ACTIVATING,
    SEQUENCE_FLOW_TAKEN,
    ELEMENT_COMPLETED,
    ELEMENT_ACTIVATED,
    ELEMENT_COMPLETING,
    ELEMENT_TERMINATED,
    ELEMENT_TERMINATING,
}
