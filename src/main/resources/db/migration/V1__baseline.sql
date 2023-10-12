CREATE TABLE public.element_instance
(
    id                      character varying(255) NOT NULL,
    bpmn_element_type_      character varying,
    element_id_             character varying(255),
    flow_scope_key_         bigint,
    intent_                 character varying,
    key_                    bigint,
    partition_id_           integer,
    position_               bigint,
    process_definition_key_ bigint,
    process_instance_key_   bigint,
    timestamp_              bigint
);


ALTER TABLE public.element_instance OWNER TO zeebe;
CREATE TABLE public.error
(
    position_             bigint NOT NULL,
    error_event_position_ bigint,
    exception_message_ oid,
    process_instance_key_ bigint,
    stacktrace_ oid,
    timestamp_            bigint
);


ALTER TABLE public.error OWNER TO zeebe;
CREATE TABLE public.hazelcast_config
(
    id       character varying(255) NOT NULL,
    sequence bigint                 NOT NULL
);


ALTER TABLE public.hazelcast_config OWNER TO zeebe;
CREATE TABLE public.incident
(
    key_                    bigint NOT NULL,
    bpmn_process_id_        character varying(255),
    created_                bigint,
    element_instance_key_   bigint,
    error_msg_ oid,
    error_type_             character varying(255),
    job_key_                bigint,
    process_definition_key_ bigint,
    process_instance_key_   bigint,
    resolved_               bigint
);


ALTER TABLE public.incident OWNER TO zeebe;
CREATE TABLE public.job
(
    key_                  bigint NOT NULL,
    element_instance_key_ bigint,
    job_type_             character varying(255),
    process_instance_key_ bigint,
    retries_              integer,
    state_                character varying(255),
    timestamp_            bigint,
    worker_               character varying(255)
);


ALTER TABLE public.job OWNER TO zeebe;
CREATE TABLE public.message
(
    key_             bigint NOT NULL,
    correlation_key_ character varying(255),
    message_id_      character varying(255),
    name_            character varying(255),
    payload_ oid,
    state_           character varying(255),
    timestamp_       bigint
);


ALTER TABLE public.message OWNER TO zeebe;
CREATE TABLE public.message_subscription
(
    id_                     character varying(255) NOT NULL,
    correlation_key_        character varying(255),
    element_instance_key_   bigint,
    message_name_           character varying(255),
    process_definition_key_ bigint,
    process_instance_key_   bigint,
    state_                  character varying(255),
    target_flow_node_id_    character varying(255),
    timestamp_              bigint
);


ALTER TABLE public.message_subscription OWNER TO zeebe;
CREATE TABLE public.process
(
    key_             bigint NOT NULL,
    bpmn_process_id_ character varying(255),
    resource_ oid,
    timestamp_       bigint,
    version_         integer
);


ALTER TABLE public.process OWNER TO zeebe;
CREATE TABLE public.process_instance
(
    key_                         bigint NOT NULL,
    bpmn_process_id_             character varying(255),
    end_                         bigint,
    parent_element_instance_key_ bigint,
    parent_process_instance_key_ bigint,
    partition_id_                integer,
    process_definition_key_      bigint,
    start_                       bigint,
    state_                       character varying(255),
    version_                     integer
);


ALTER TABLE public.process_instance OWNER TO zeebe;
CREATE TABLE public.timer
(
    key_                    bigint NOT NULL,
    due_date_               bigint,
    element_instance_key_   bigint,
    process_definition_key_ bigint,
    process_instance_key_   bigint,
    repetitions             integer,
    state_                  character varying(255),
    target_element_id_      character varying(255),
    timestamp_              bigint
);


ALTER TABLE public.timer OWNER TO zeebe;
CREATE TABLE public.variable
(
    id                    character varying(255) NOT NULL,
    name_                 character varying(255),
    partition_id_         integer,
    position_             bigint,
    process_instance_key_ bigint,
    scope_key_            bigint,
    state_                character varying(255),
    timestamp_            bigint,
    value_ oid
);


ALTER TABLE public.variable OWNER TO zeebe;
ALTER TABLE ONLY public.element_instance
    ADD CONSTRAINT element_instance_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.error
    ADD CONSTRAINT error_pkey PRIMARY KEY (position_);

ALTER TABLE ONLY public.hazelcast_config
    ADD CONSTRAINT hazelcast_config_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.incident
    ADD CONSTRAINT incident_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.job
    ADD CONSTRAINT job_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.message_subscription
    ADD CONSTRAINT message_subscription_pkey PRIMARY KEY (id_);

ALTER TABLE ONLY public.process_instance
    ADD CONSTRAINT process_instance_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.process
    ADD CONSTRAINT process_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.timer
    ADD CONSTRAINT timer_pkey PRIMARY KEY (key_);

ALTER TABLE ONLY public.variable
    ADD CONSTRAINT variable_pkey PRIMARY KEY (id);

CREATE INDEX element_instance_processinstancekeyindex ON public.element_instance USING btree (process_instance_key_);

CREATE INDEX error_processinstancekeyindex ON public.error USING btree (process_instance_key_);

CREATE INDEX incident_processinstancekeyindex ON public.incident USING btree (process_instance_key_);

CREATE INDEX job_processinstancekeyindex ON public.job USING btree (process_instance_key_);

CREATE INDEX message_subscription_processinstancekeyindex ON public.message_subscription USING btree (process_instance_key_);

CREATE INDEX timer_processinstancekeyindex ON public.timer USING btree (process_instance_key_);

CREATE INDEX variable_processinstancekeyindex ON public.variable USING btree (process_instance_key_);

