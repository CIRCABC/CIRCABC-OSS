package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;

public class ChildTest {

  @Test
  public void testEmptyConstructor_createsEmptyChildren() {
    Child child = new Child();
    assertNotNull(child.getChildren());
    assertTrue(child.getChildren().isEmpty());
    assertNull(child.getName());
    assertNull(child.getNode());
  }

  @Test
  public void testFullConstructor_setsAllFields() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    List<Child> children = new ArrayList<>();
    children.add(new Child());

    Child child = new Child("testName", nodeRef, children);

    assertEquals("testName", child.getName());
    assertEquals(nodeRef, child.getNode());
    assertEquals(1, child.getChildren().size());
  }

  @Test
  public void testFullConstructor_whenNullChildren_createsEmptyList() {
    Child child = new Child("name", null, null);
    assertNotNull(child.getChildren());
    assertTrue(child.getChildren().isEmpty());
  }

  @Test
  public void testIsLeaf_whenNoChildren_returnsTrue() {
    Child child = new Child();
    assertTrue(child.isLeaf());
  }

  @Test
  public void testIsLeaf_whenHasChildren_returnsFalse() {
    List<Child> children = new ArrayList<>();
    children.add(new Child());
    Child child = new Child("parent", null, children);
    assertFalse(child.isLeaf());
  }

  @Test
  public void testSetChildren_replacesExistingList() {
    Child child = new Child();
    List<Child> newChildren = new ArrayList<>();
    newChildren.add(new Child());
    child.setChildren(newChildren);
    assertEquals(1, child.getChildren().size());
  }

  @Test
  public void testIsLeaf_whenChildrenSetToNull_returnsTrue() {
    Child child = new Child();
    child.setChildren(null);
    assertTrue(child.isLeaf());
  }
}
