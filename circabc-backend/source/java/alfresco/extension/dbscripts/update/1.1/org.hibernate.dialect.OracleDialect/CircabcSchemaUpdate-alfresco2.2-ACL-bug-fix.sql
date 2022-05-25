--     Copyright European Community 2006 - Licensed under the EUPL V.1.0
--
--  		   http://ec.europa.eu/idabc/en/document/6523
--

ALTER TABLE ALF_ACCESS_CONTROL_ENTRY
DROP CONSTRAINT "FK_ALF_ACE_AUTH"
;

ALTER TABLE ALF_ACCESS_CONTROL_ENTRY
ADD CONSTRAINT FK_ALF_ACE_AUTH FOREIGN KEY
(
"AUTHORITY_ID"
) REFERENCES ALF_AUTHORITY
(
"ID"
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE ALF_ACL_MEMBER
DROP CONSTRAINT "FK_ALF_ACLM_ACE"
;

ALTER TABLE ALF_ACL_MEMBER
ADD CONSTRAINT FK_ALF_ACLM_ACE FOREIGN KEY
(
"ACE_ID"
) REFERENCES ALF_ACCESS_CONTROL_ENTRY
(
"ID"
)
ON DELETE CASCADE ENABLE
;

DELETE FROM alf_applied_patch WHERE id = 'patch.db-Circa1.1-alfresco2.2.ACL-bug-fix';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-Circa1.1-alfresco2.2.ACL-bug-fix', 'Manually executed script upgrade for Circabc 1.1: To fix an Alfresco 2.2 bug when delete authority',
    0, 10000, -1, 63, null, 'UNKOWN', 1, 1, 'Script completed'
  );

