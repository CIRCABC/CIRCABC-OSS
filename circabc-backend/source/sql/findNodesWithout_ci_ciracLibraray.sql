SELECT 'nodes['
  || TO_CHAR( ROWNUM -1)
  || ']='
  || '"'
  || alf_store.protocol
  || '://'
  || alf_store.identifier
  || '/'
  || alf_node.uuid
  || '"'
  || ';' AS noderef
FROM alf_node ,
  alf_store
WHERE alf_node.store_id = alf_store.id
AND alf_node.id        IN (
  (SELECT child_node_id
  FROM ALF_CHILD_ASSOC
    START WITH PARENT_NODE_ID IN
    ( SELECT node_id FROM alf_node_aspects WHERE alf_node_aspects.qname_id=2150
    )
    CONNECT BY prior child_node_id = PARENT_NODE_ID
  AND type_qname_id                =107
  AND prior is_primary             =1
  AND level                        <100
  MINUS
  SELECT node_id FROM alf_node_aspects WHERE qname_id = 2148
  MINUS
  SELECT id FROM alf_node  WHERE type_qname_id in ( 1666 ,1663,1660 ) ) );
  
