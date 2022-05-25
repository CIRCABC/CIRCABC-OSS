--     Copyright European Community 2006 - Licensed under the EUPL V.1.0
--
--  		   http://ec.europa.eu/idabc/en/document/6523
--

select 'DROP TABLE ' || table_name || ' CASCADE CONSTRAINTS;' from user_tables
UNION ALL
select 'DROP SEQUENCE ' || sequence_name || ' ;'  from user_sequences
union all
select 'DROP ' || OBJECT_TYPE || ' '  ||  OBJECT_NAME || ' ;'  from USER_PROCEDURES
union all
select 'DROP VIEW  '   ||  USER_VIEWS.VIEW_NAME || ' ;'  from USER_VIEWS;

imp circabc_local_alfresco4/circabc_local_alfresco4 file=./exp_pignoya_circabc_local-schwerr-_3_4_8_3.dmp fromuser=pignoya_circabc_local touser=circabc_local_alfresco4
imp circabc_local_alfresco4/circabc_local_alfresco4 file=./exp_circabc_audit_pignoya.dmp fromuser=circabc_audit_pignoya touser=circabc_local_alfresco4

DROP TABLE CBC_LOG CASCADE CONSTRAINTS;
DROP TABLE CBC_LOG_ACTIVITY CASCADE CONSTRAINTS;
DROP SEQUENCE CBC_LOG_SEQ;
DROP SEQUENCE CBC_LOG_ACTIVITY_SEQ;
DROP TABLE CBC_LOCK CASCADE CONSTRAINTS;

delete from DATABASECHANGELOG where filename='classpath:alfresco/module/circabcAmp/context/db-changelog.xml' and id in (1, 2, 3, 4, 5);
