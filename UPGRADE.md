
Upgrade documentation for Zeebe Simple Monitor 
==================================================

## Upgrading from v2.1.0

In order to improve database lookup performance, a handful of indices was added to the database structure.
Zeebe Simple Monitor will not alter existing database tables automatically, but if you have a PostgreSQL
or other DB running, you may want to alter the table structures manually, in order to improve lookup performance.
Again, this is an optional step - Zeebe Simple Monitor will work as is without it - just a bit slower response times.

Of course, if you use an in-memory DB or do not need to keep prior data, then simply drop all tables and sequences,
and let Zeebe Simple Monitor create them again for you (automatic creation works).

### Upgrade procedure

1. stop Zeebe Simple Monitor (v2.1.0)
2. run the SQL script below against your PostgreSQL Database
3. start up Zeebe Simple Monitor (new version)

```sql
CREATE INDEX error_processInstanceKeyIndex ON error (process_instance_key_);
CREATE INDEX incident_processInstanceKeyIndex ON incident (process_instance_key_);
CREATE INDEX job_processInstanceKeyIndex ON job (process_instance_key_);
CREATE INDEX message_subscription_processInstanceKeyIndex ON message_subscription (process_instance_key_);
CREATE INDEX timer_processInstanceKeyIndex ON timer (process_instance_key_);
```
(This SQL was developed and tested using a recent PostgreSQL instance.
You might need to adopt that if you're using another Database)

## Upgrading from v2.0.0

Due to some issues with storing variables and element instances, the database structure changed.
Zeebe Simple Monitor will not alter existing database tables automatically, but if you have a PostgreSQL
or other DB running, you need to alter the table structures manually, in order to keep your data.

Of course, if you use an in-memory DB or do not need to keep prior data, then simply drop all tables and sequences,
and let Zeebe Simple Monitor create them again for you (automatic creation works).

### Upgrade procedure

1. stop Zeebe Simple Monitor (v2.0.0)
2. run the SQL script below against your PostgreSQL Database
3. start up Zeebe Simple Monitor (new version)

```sql
-- part 1, element_instance table changes
ALTER TABLE element_instance ADD COLUMN ID varchar(255);
UPDATE element_instance SET id = (partition_id_::varchar || '-' || position_::varchar) where true;
ALTER TABLE element_instance DROP CONSTRAINT element_instance_pkey;
ALTER TABLE element_instance ADD PRIMARY KEY (id);
CREATE INDEX element_instance_processInstanceKeyIndex ON element_instance (process_instance_key_);
-- part 2, variable table changes
ALTER TABLE variable ADD COLUMN ID varchar(255);
ALTER TABLE variable ADD COLUMN PARTITION_ID_ integer DEFAULT 1;
UPDATE variable SET id = (partition_id_::varchar || '-' || position_::varchar) where true;
ALTER TABLE variable DROP CONSTRAINT variable_pkey;
ALTER TABLE variable ADD PRIMARY KEY (id);
CREATE INDEX variable_processInstanceKeyIndex ON variable (process_instance_key_);
```
(This SQL was developed and tested using a recent PostgreSQL instance.
 You might need to adopt that if you're using another Database)
