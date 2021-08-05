
Upgrade documentation for Zeebe Simple Monitor 
==================================================

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
ALTER TABLE element_instance DROP CONSTRAINT element_instance_pkey;
ALTER TABLE element_instance ADD COLUMN ID SERIAL NOT NULL CONSTRAINT element_instance_pkey PRIMARY KEY;
CREATE INDEX element_instance_processInstanceKeyIndex ON element_instance (process_instance_key_);
-- part 2, variable table changes
ALTER TABLE variable DROP CONSTRAINT variable_pkey;
ALTER TABLE variable ADD COLUMN ID SERIAL NOT NULL CONSTRAINT variable_pkey PRIMARY KEY;
ALTER TABLE variable ADD COLUMN PARTITION_ID_ integer DEFAULT 0;
CREATE INDEX variable_processInstanceKeyIndex ON variable (process_instance_key_);
```
(This SQL script uses some recent PostgreSQL features, like 'SERIAL'.
 You might need to adopt that if you're using another Database)
