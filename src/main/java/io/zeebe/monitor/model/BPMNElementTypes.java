package io.zeebe.monitor.model;

public enum BPMNElementTypes {
    END_EVENT,
    START_EVENT,
    RECEIVE_TASK,
    BOUNDARY_EVENT,
    INTERMEDIATE_CATCH_EVENT,
    EXCLUSIVE_GATEWAY,
    MULTI_INSTANCE_BODY,
    CALL_ACTIVITY,
    PARALLEL_GATEWAY,
    SERVICE_TASK,
    PROCESS,
    EVENT_BASED_GATEWAY,
    SUB_PROCESS,
    SEQUENCE_FLOW,
}
