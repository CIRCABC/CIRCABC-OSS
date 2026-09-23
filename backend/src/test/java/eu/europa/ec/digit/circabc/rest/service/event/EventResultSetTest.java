package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.junit.Before;
import org.junit.Test;

public class EventResultSetTest {

  private EventResultSet resultSet;
  private List<EventResultSetRow> rows;
  private NodeRef nodeRef1;
  private NodeRef nodeRef2;

  @Before
  public void setUp() {
    nodeRef1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "id-1");
    nodeRef2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "id-2");
    rows = new ArrayList<>();
    rows.add(new EventResultSetRow(nodeRef1));
    rows.add(new EventResultSetRow(nodeRef2));
    resultSet = new EventResultSet(rows);
  }

  @Test
  public void testLength_whenResultsExist_thenReturnsSize() {
    assertEquals(2, resultSet.length());
  }

  @Test
  public void testLength_whenEmpty_thenReturnsZero() {
    EventResultSet empty = new EventResultSet(Collections.emptyList());
    assertEquals(0, empty.length());
  }

  @Test
  public void testGetNumberFound_thenReturnsSize() {
    assertEquals(2L, resultSet.getNumberFound());
  }

  @Test
  public void testGetRow_thenReturnsCorrectRow() {
    ResultSetRow row = resultSet.getRow(0);
    assertEquals(nodeRef1, row.getNodeRef());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetRow_whenIndexOutOfBounds_thenThrows() {
    resultSet.getRow(5);
  }

  @Test
  public void testGetNodeRef_thenReturnsNull() {
    assertNull(resultSet.getNodeRef(0));
  }

  @Test
  public void testGetScore_thenReturnsZero() {
    assertEquals(0f, resultSet.getScore(0), 0.0f);
  }

  @Test
  public void testGetNodeRefs_thenReturnsEmptyList() {
    assertTrue(resultSet.getNodeRefs().isEmpty());
  }

  @Test
  public void testGetChildAssocRefs_thenReturnsEmptyList() {
    assertTrue(resultSet.getChildAssocRefs().isEmpty());
  }

  @Test
  public void testGetChildAssocRef_thenReturnsNull() {
    assertNull(resultSet.getChildAssocRef(0));
  }

  @Test
  public void testIterator_thenIteratesOverRows() {
    Iterator<ResultSetRow> it = resultSet.iterator();
    assertTrue(it.hasNext());
    assertEquals(nodeRef1, it.next().getNodeRef());
    assertEquals(nodeRef2, it.next().getNodeRef());
    assertFalse(it.hasNext());
  }

  @Test
  public void testGetFieldFacet_thenReturnsEmptyList() {
    assertTrue(resultSet.getFieldFacet("any").isEmpty());
  }

  @Test
  public void testGetFacetQueries_thenReturnsEmptyMap() {
    assertTrue(resultSet.getFacetQueries().isEmpty());
  }

  @Test
  public void testGetHighlighting_thenReturnsEmptyMap() {
    assertTrue(resultSet.getHighlighting().isEmpty());
  }

  @Test
  public void testHasMore_thenReturnsFalse() {
    assertFalse(resultSet.hasMore());
  }

  @Test
  public void testGetResultSetMetaData_thenReturnsNull() {
    assertNull(resultSet.getResultSetMetaData());
  }

  @Test
  public void testGetSpellCheckResult_thenReturnsNull() {
    assertNull(resultSet.getSpellCheckResult());
  }
}
