package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class EventResultSetRowTest {

  private EventResultSetRow row;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
    row = new EventResultSetRow(nodeRef);
  }

  @Test
  public void testGetNodeRef_returnsConstructorValue() {
    assertEquals(nodeRef, row.getNodeRef());
  }

  @Test
  public void testGetValues_returnsEmptyMap() {
    assertTrue(row.getValues().isEmpty());
  }

  @Test
  public void testGetValue_byColumnName_returnsNull() {
    assertNull(row.getValue("any"));
  }

  @Test
  public void testGetValue_byQName_returnsNull() {
    assertNull(row.getValue(QName.createQName("http://test", "local")));
  }

  @Test
  public void testGetNodeRefs_returnsEmptyMap() {
    assertTrue(row.getNodeRefs().isEmpty());
  }

  @Test
  public void testGetNodeRef_bySelectorName_returnsNull() {
    assertNull(row.getNodeRef("selector"));
  }

  @Test
  public void testGetScore_returnsZero() {
    assertEquals(0f, row.getScore(), 0.0001f);
  }

  @Test
  public void testGetScores_returnsEmptyMap() {
    assertTrue(row.getScores().isEmpty());
  }

  @Test
  public void testGetScore_bySelectorName_returnsZero() {
    assertEquals(0f, row.getScore("selector"), 0.0001f);
  }

  @Test
  public void testGetResultSet_returnsNull() {
    assertNull(row.getResultSet());
  }

  @Test
  public void testGetQName_returnsNull() {
    assertNull(row.getQName());
  }

  @Test
  public void testGetIndex_returnsZero() {
    assertEquals(0, row.getIndex());
  }

  @Test
  public void testGetChildAssocRef_returnsNull() {
    assertNull(row.getChildAssocRef());
  }
}
