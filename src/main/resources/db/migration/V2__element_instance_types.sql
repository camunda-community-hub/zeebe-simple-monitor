CREATE
TYPE public.ei_bpmn_element_type AS ENUM (
  'END_EVENT',
  'START_EVENT',
  'RECEIVE_TASK',
  'BOUNDARY_EVENT',
  'INTERMEDIATE_CATCH_EVENT',
  'EXCLUSIVE_GATEWAY',
  'MULTI_INSTANCE_BODY',
  'CALL_ACTIVITY',
  'PARALLEL_GATEWAY',
  'SERVICE_TASK',
  'PROCESS',
  'EVENT_BASED_GATEWAY',
  'SUB_PROCESS',
  'SEQUENCE_FLOW'
);

ALTER
TYPE public.ei_bpmn_element_type OWNER TO postgres;

CREATE
TYPE public.ei_intent AS ENUM (
  'ELEMENT_ACTIVATING',
  'SEQUENCE_FLOW_TAKEN',
  'ELEMENT_COMPLETED',
  'ELEMENT_ACTIVATED',
  'ELEMENT_COMPLETING',
  'ELEMENT_TERMINATED',
  'ELEMENT_TERMINATING'
);

ALTER
TYPE public.ei_intent OWNER TO postgres;

ALTER TABLE element_instance
    ALTER COLUMN intent_ TYPE ei_intent
    USING intent_::ei_intent;

ALTER TABLE element_instance
    ALTER COLUMN bpmn_element_type_ TYPE ei_bpmn_element_type
    USING bpmn_element_type_::ei_bpmn_element_type;

