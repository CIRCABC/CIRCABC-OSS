
select * from alf_node_properties where qname_id = 27 order by string_value

select * from alf_node_properties where string_value like 'uid002%'

select * from alf_qname where local_name = 'username'

select * from alf_qname where id = 1520

select distinct qname_id from alf_node_properties where string_value like 'uid002%' or string_value like 'uid003%'


select * from alf_namespace

select * from alf_node where transaction_id = 6

select * from alf_prop_double_value

select * from alf_store

select * from alf_permission

select * from alf_authority

select * from alf_acl_member where acl_id = 15


select uri, local_name, boolean_value, long_value, float_value, double_value, string_value, actual_type_n, persisted_type_n 
from alf_qname aq, alf_namespace an, alf_node_properties anp 
where an.id = aq.ns_id and anp.qname_id = aq.id and anp.node_id = 15851



select * from alf_node an, alf_node_properties anp where anp.string_value = 'H02C12I06' and an.id = anp.node_id

select * from alf_authority where authority like '%GROUP_H%'

select * from alf_access_control_entry where authority_id = 314465



select * from alf_acl_change_set



select * from alf_node_properties where string_value = 'ngeorgns2'

select * from alf_node where id in (394983, 368487, 394829)

select * from alf_node_properties where node_id = 394829







select * from alf_access_control_list where id = 15

select * from alf_child_assoc

 


select aa.authority 
from alf_authority aa, alf_acl_member aam, ALF_ACCESS_CONTROL_ENTRY aace
where aa.id = aace.authority_id and aam.ace_id = aace.id and aam.acl_id = 10



select substr(anp2.string_value,8,2) HeaderId, substr(anp2.string_value,11,2) CategoryId, substr(anp2.string_value,14,2) IgId, substr(anp.string_value,4,5) Username
from alf_node_properties anp, ALF_CHILD_ASSOC aca, alf_node_properties anp2
where anp.qname_id = 44 and anp.node_id = aca.child_node_id and aca.parent_node_id = anp2.node_id and anp2.string_value like '%GROUP_H%' and anp.string_value is not null and anp.string_value like 'uid%'
order by anp.string_value


select * from alf_qname where local_name = 'content'
select * from alf_qname where id=42

select * from ALF_NODE_PROPERTIES where string_value = 'wallpaper-1975709.jpg'

select * from ALF_NODE_PROPERTIES where node_id in (28358, 28360)


# Fix constraints

select * from ALF_CHILD_ASSOC where qname_localname like '%InteroperabilitySolutionsofEuropeanPublicAdministrations%'

ALTER TABLE "ALF_NODE_PROPERTIES" ENABLE CONSTRAINT "FK_ALF_NPROP_N"

select distinct node_id from ALF_NODE_PROPERTIES where node_id not in (select id from ALF_NODE)
delete from ALF_NODE_PROPERTIES where node_id in (10887275, 10887277, 10887281, 10887320, 10887279, 10887265)


ALTER TABLE "ALF_NODE_ASSOC" ENABLE CONSTRAINT "FK_ALF_NASS_TNODE"

select distinct target_node_id from ALF_NODE_ASSOC where target_node_id not in (select id from ALF_NODE)
delete from ALF_NODE_ASSOC where target_node_id = 10887281


ALTER TABLE "ALF_NODE_ASSOC" ENABLE CONSTRAINT "FK_ALF_NASS_SNODE"

select distinct source_node_id from ALF_NODE_ASSOC where source_node_id not in (select id from ALF_NODE)
delete from ALF_NODE_ASSOC where source_node_id = 10887281


ALTER TABLE "ALF_NODE" ENABLE CONSTRAINT "FK_ALF_NODE_ACL"

select distinct acl_id from ALF_NODE where acl_id not in (select id from ALF_ACCESS_CONTROL_LIST)
select distinct acl_id from ALF_NODE minus select id from ALF_ACCESS_CONTROL_LIST
delete from ALF_NODE where acl_id in (10887097, 10887236)


ALTER TABLE "ALF_NODE_ASPECTS" ENABLE CONSTRAINT "FK_ALF_NASP_N"

select distinct node_id from ALF_NODE_ASPECTS where node_id not in (select id from ALF_NODE)
delete from ALF_NODE_ASPECTS where node_id in (10887275, 10887277, 10887281, 10887279, 10887265)



select * from user_constraints where constraint_name = 'FK_ALF_NASP_N'






select * from ALF_ACCESS_CONTROL_ENTRY where permission_id = 38

select * from ALF_AUTHORITY where id = 125

select id from ALF_PERMISSION where name = 'Read'

select id from ALF_QNAME where local_name = 'authorityName'

select * from ALF_QNAME where id = 97

select * from alf_node where id = 90518


select node_id from ALF_NODE_PROPERTIES where string_value = 'GROUP_ALL_CIRCA_USERS' and qname_id = (select id from ALF_QNAME where local_name = 'authorityName')


SELECT DECODE(COUNT(*),0,0,1) FROM ALF_ACL_MEMBER WHERE ace_id IN (SELECT id FROM ALF_ACCESS_CONTROL_ENTRY WHERE authority_id = (SELECT id FROM alf_authority WHERE authority ='GROUP_ALL_CIRCA_USERS'))


select * from ALF_NODE_PROPERTIES where string_value = 'ALL_CIRCA_USERS'
select * from ALF_NODE_PROPERTIES where node_id = 90518

select * from ALF_NODE_PROPERTIES
WHERE string_value = 'GROUP_ALL_CIRCA_USERS' AND node_id != (select node_id from ALF_NODE_PROPERTIES where string_value = 'GROUP_ALL_CIRCA_USERS' and qname_id = (select id from ALF_QNAME where local_name = 'authorityName'))

select an.*, aq.local_name from alf_node an, alf_qname aq 
where an.type_qname_id = aq.id and an.id in
(select node_id from ALF_NODE_PROPERTIES
WHERE string_value = 'ALL_CIRCA_USERS')

select distinct aq.local_name, count(aq.local_name) from ALF_NODE_PROPERTIES anp, ALF_QNAME aq WHERE anp.string_value = 'ALL_CIRCA_USERS Description' and anp.qname_id = aq.id group by aq.local_name


select ap.* from ALF_ACCESS_CONTROL_ENTRY ace, ALF_PERMISSION ap
WHERE ace.authority_id = (SELECT id FROM alf_authority WHERE authority = 'GROUP_ALL_CIRCA_USERS') AND ap.id = ace.permission_id



SELECT id FROM ALF_ACCESS_CONTROL_ENTRY WHERE authority_id = (SELECT id FROM alf_authority WHERE authority ='GROUP_EVERYONE')


select anp.*, aq.local_name from ALF_NODE an, ALF_NODE_PROPERTIES anp, ALF_QNAME aq where an.id = anp.node_id and anp.qname_id = aq.id and an.uuid = '84ec71f1-4422-4c8a-b432-ef29f73c9272'


select * from ALF_ACCESS_CONTROL_LIST


select * from ALF_NODE_PROPERTIES where string_value like '%EVERYONE%'

select * from ALF_AUTHORITY


select an.uuid NodeUUID, aa.authority, ap.name PermissionName, aace.allowed
from ALF_NODE an, ALF_ACCESS_CONTROL_LIST aacl, ALF_ACL_MEMBER aam, ALF_ACCESS_CONTROL_ENTRY aace, ALF_AUTHORITY aa, ALF_PERMISSION ap 
where an.acl_id = aacl.id 
and aacl.id = aam.acl_id 
and aam.ace_id = aace.id 
and aace.permission_id = ap.id 
and aace.authority_id = aa.id 
and an.uuid = 'e0b71ade-9773-446d-bad0-c78518fc5b4b'

and ap.name = 'All'


and an.uuid = '4cc0963e-58ae-4d9b-926d-a09c625f4d32'
and an.uuid = '0a83a1a3-efca-4e51-b193-3fbd06adb9fa'
and an.uuid = 'e0b71ade-9773-446d-bad0-c78518fc5b4b'
and aa.authority = 'admin'


select aam.*
from ALF_NODE an, ALF_ACCESS_CONTROL_LIST aacl, ALF_ACL_MEMBER aam
where an.acl_id = aacl.id 
and aacl.id = aam.acl_id 
and an.uuid = 'e0b71ade-9773-446d-bad0-c78518fc5b4b'



select * from ALF_ACCESS_CONTROL_LIST

select * from ALF_NODE an, ALF_NODE_PROPERTIES anp where an.id = anp.node_id and anp.string_value like '%circabcamp%'


select * from ALF_PROP_DATE_VALUE

-- Purge nodes
select * from alf_node_aspects where node_id in (select id from alf_node where node_deleted = 1)
