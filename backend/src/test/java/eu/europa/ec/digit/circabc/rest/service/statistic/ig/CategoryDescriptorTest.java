package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class CategoryDescriptorTest {

  private CategoryDescriptor descriptor;

  @Before
  public void setUp() {
    descriptor = new CategoryDescriptor();
  }

  @Test
  public void testName_whenSet_thenReturnsValue() {
    descriptor.setName("testCategory");
    assertEquals("testCategory", descriptor.getName());
  }

  @Test
  public void testTitle_whenSet_thenReturnsValue() {
    descriptor.setTitle("Test Title");
    assertEquals("Test Title", descriptor.getTitle());
  }

  @Test
  public void testRef_whenSet_thenReturnsValue() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    descriptor.setRef(ref);
    assertEquals(ref, descriptor.getRef());
  }

  @Test
  public void testListOfIgs_whenSet_thenReturnsValue() {
    List<IgDescriptor> igs = new ArrayList<>();
    descriptor.setListOfIgs(igs);
    assertSame(igs, descriptor.getListOfIgs());
  }

  @Test
  public void testListOfAdmins_whenSet_thenReturnsValue() {
    Set<String> admins = new HashSet<>();
    admins.add("admin1");
    descriptor.setListOfAdmins(admins);
    assertEquals(admins, descriptor.getListOfAdmins());
  }

  @Test
  public void testDefaultConstructor_thenFieldsAreNull() {
    assertNull(descriptor.getName());
    assertNull(descriptor.getTitle());
    assertNull(descriptor.getRef());
    assertNull(descriptor.getListOfIgs());
    assertNull(descriptor.getListOfAdmins());
  }
}
