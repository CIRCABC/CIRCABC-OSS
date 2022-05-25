--     Copyright European Community 2006 - Licensed under the EUPL V.1.0
--
--  		   http://ec.europa.eu/idabc/en/document/6523
--



drop database alfresco;
create database alfresco;

select an.* from alf_node an where an.uuid = '9ea79734-33f1-43bc-9294-fdb48640b423'

select anp.*, aq.* from alf_node an, alf_node_properties anp, alf_qname aq where an.uuid = '9ea79734-33f1-43bc-9294-fdb48640b423' and an.id = anp.node_id and anp.qname_id = aq.id

select * from alf_qname where local_name = 'deleted'


SELECT n.* 
FROM alf_node n, alf_qname q, alf_namespace ns 
where q.id = n.type_qname_id 
and ns.id = q.ns_id 
and (ns.uri = 'http://www.alfresco.org/model/system/1.0' AND q.local_name = 'deleted'); 