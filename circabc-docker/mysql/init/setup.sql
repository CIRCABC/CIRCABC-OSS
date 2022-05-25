CREATE USER 'alfresco' IDENTIFIED BY 'alfresco';
CREATE DATABASE alfresco DEFAULT CHARACTER SET utf8 ; 
GRANT ALL ON alfresco.* TO 'alfresco';  

CREATE USER 'circabc_audit' IDENTIFIED BY 'circabc_audit';
CREATE DATABASE circabc_audit DEFAULT CHARACTER SET utf8 ; 
GRANT ALL ON circabc_audit.* TO 'circabc_audit';
