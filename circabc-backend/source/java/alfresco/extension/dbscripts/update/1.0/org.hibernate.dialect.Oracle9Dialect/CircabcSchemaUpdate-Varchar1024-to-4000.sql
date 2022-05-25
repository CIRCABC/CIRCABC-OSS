--     Copyright European Community 2006 - Licensed under the EUPL V.1.0
--  
--  		   http://ec.europa.eu/idabc/en/document/6523
--  		   


-- SELECT 'alter table ' || table_name || ' modify ' || column_name || ' VARCHAR2(4000 CHAR);'
-- FROM cols
-- WHERE data_type = 'VARCHAR2'
--  AND CHAR_LENGTH = 1024
--  AND(TABLE_NAME LIKE 'ALF%' OR TABLE_NAME LIKE 'AVM%' OR TABLE_NAME LIKE 'JBPM%');

alter table AVM_NODE_PROPERTIES modify STRING_VALUE VARCHAR2(4000 CHAR);
alter table AVM_NODE_PROPERTIES_NEW modify STRING_VALUE VARCHAR2(4000 CHAR);
alter table AVM_STORE_PROPERTIES modify STRING_VALUE VARCHAR2(4000 CHAR);
alter table ALF_APPLIED_PATCH modify DESCRIPTION VARCHAR2(4000 CHAR);
alter table ALF_APPLIED_PATCH modify REPORT VARCHAR2(4000 CHAR);
alter table ALF_ATTRIBUTES modify STRING_VALUE VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify RETURN_VAL VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify ARG_1 VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify ARG_2 VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify ARG_3 VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify ARG_4 VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify ARG_5 VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify SERIALIZED_URL VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify EXCEPTION_MESSAGE VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify HOST_ADDRESS VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify CLIENT_ADDRESS VARCHAR2(4000 CHAR);
alter table ALF_AUDIT_FACT modify MESSAGE_TEXT VARCHAR2(4000 CHAR);
alter table ALF_NODE_PROPERTIES modify STRING_VALUE VARCHAR2(4000 CHAR);


DELETE FROM alf_applied_patch WHERE id = 'patch.db-Circa1.0-OracleVarcharEnlargement';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-Circa1.0-OracleVarcharEnlargement', 'Manually executed script upgrade for Circabc Oracle: The varchar columns are enlarged from 1024 char to 4000',
    0, 10000, -1, 63, null, 'UNKOWN', 1, 1, 'Script completed'
  );

