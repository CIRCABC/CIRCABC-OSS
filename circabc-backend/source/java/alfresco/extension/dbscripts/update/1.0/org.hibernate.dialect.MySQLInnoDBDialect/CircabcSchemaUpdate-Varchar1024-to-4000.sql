--     Copyright European Community 2006 - Licensed under the EUPL V.1.0
--  
--  		   http://ec.europa.eu/idabc/en/document/6523
--  		   

DELETE FROM alf_applied_patch WHERE id = 'patch.db-Circa1.0-OracleVarcharEnlargement';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-Circa1.0-OracleVarcharEnlargement', 'Manually executed script upgrade for Circabc Oracle: The varchar columns are enlarged from 1024 char to 4000',
    0, 10000, -1, 63, null, 'UNKOWN', 1, 1, 'Script completed'
  );

