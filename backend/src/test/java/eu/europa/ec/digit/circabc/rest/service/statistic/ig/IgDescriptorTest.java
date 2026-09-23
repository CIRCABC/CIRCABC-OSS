package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class IgDescriptorTest {

  private IgDescriptor descriptor;

  @Before
  public void setUp() {
    descriptor = new IgDescriptor();
  }

  @Test
  public void testSetName_whenValueSet_thenGetReturnsIt() {
    descriptor.setName("testIG");
    assertEquals("testIG", descriptor.getName());
  }

  @Test
  public void testSetTitle_whenValueSet_thenGetReturnsIt() {
    descriptor.setTitle("Test Title");
    assertEquals("Test Title", descriptor.getTitle());
  }

  @Test
  public void testSetRef_whenNodeRefSet_thenGetReturnsIt() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    descriptor.setRef(ref);
    assertEquals(ref, descriptor.getRef());
  }

  @Test
  public void testSetSetOfLeaders_whenSetProvided_thenGetReturnsIt() {
    Set<String> leaders = new HashSet<>();
    leaders.add("user1");
    leaders.add("user2");
    descriptor.setSetOfLeaders(leaders);
    assertEquals(leaders, descriptor.getSetOfLeaders());
  }

  @Test
  public void testSetDescription_whenValueSet_thenGetReturnsIt() {
    descriptor.setDescription("A description");
    assertEquals("A description", descriptor.getDescription());
  }

  @Test
  public void testSetLightDescription_whenValueSet_thenGetReturnsIt() {
    descriptor.setLightDescription("Short desc");
    assertEquals("Short desc", descriptor.getLightDescription());
  }

  @Test
  public void testSetCreationDate_whenValueSet_thenGetReturnsIt() {
    descriptor.setCreationDate("2026-01-01");
    assertEquals("2026-01-01", descriptor.getCreationDate());
  }

  @Test
  public void testSetPublicVisibility_whenTrue_thenGetReturnsTrue() {
    descriptor.setPublicVisibility(true);
    assertTrue(descriptor.getPublicVisibility());
  }

  @Test
  public void testSetPublicEnabled_whenFalse_thenGetReturnsFalse() {
    descriptor.setPublicEnabled(false);
    assertFalse(descriptor.getPublicEnabled());
  }

  @Test
  public void testSetRegisteredEnabled_whenTrue_thenGetReturnsTrue() {
    descriptor.setRegisteredEnabled(true);
    assertTrue(descriptor.getRegisteredEnabled());
  }

  @Test
  public void testSetAvailableServices_whenSetProvided_thenGetReturnsIt() {
    Set<String> services = Set.of("Library", "Newsgroup");
    descriptor.setAvailableServices(services);
    assertEquals(services, descriptor.getAvailableServices());
  }

  @Test
  public void testSetLastAccessDate_whenValueSet_thenGetReturnsIt() {
    descriptor.setLastAccessDate("2026-05-01");
    assertEquals("2026-05-01", descriptor.getLastAccessDate());
  }

  @Test
  public void testSetLastUpdateDate_whenValueSet_thenGetReturnsIt() {
    descriptor.setLastUpdateDate("2026-05-02");
    assertEquals("2026-05-02", descriptor.getLastUpdateDate());
  }

  @Test
  public void testSetNbDocuments_whenValueSet_thenGetReturnsIt() {
    descriptor.setNbDocuments(42);
    assertEquals(Integer.valueOf(42), descriptor.getNbDocuments());
  }

  @Test
  public void testSetNbMembers_whenValueSet_thenGetReturnsIt() {
    descriptor.setNbMembers(10);
    assertEquals(Integer.valueOf(10), descriptor.getNbMembers());
  }

  @Test
  public void testSetLibraryDocSize_whenValueSet_thenGetReturnsIt() {
    descriptor.setLibraryDocSize(1024.5);
    assertEquals(Double.valueOf(1024.5), descriptor.getLibraryDocSize());
  }

  @Test
  public void testSetInformationInfoSize_whenValueSet_thenGetReturnsIt() {
    descriptor.setInformationInfoSize(512.0);
    assertEquals(Double.valueOf(512.0), descriptor.getInformationInfoSize());
  }

  @Test
  public void testSetNbEvents_whenValueSet_thenGetReturnsIt() {
    descriptor.setNbEvents(5);
    assertEquals(Integer.valueOf(5), descriptor.getNbEvents());
  }

  @Test
  public void testSetNbPosts_whenValueSet_thenGetReturnsIt() {
    descriptor.setNbPosts(100);
    assertEquals(Integer.valueOf(100), descriptor.getNbPosts());
  }

  @Test
  public void testSetDeepness_whenValueSet_thenGetReturnsIt() {
    descriptor.setDeepness(3);
    assertEquals(Integer.valueOf(3), descriptor.getDeepness());
  }

  @Test
  public void testSetContactInformation_whenValueSet_thenGetReturnsIt() {
    descriptor.setContactInformation("admin@ec.europa.eu");
    assertEquals("admin@ec.europa.eu", descriptor.getContactInformation());
  }

  @Test
  public void testDefaultConstructor_whenCreated_thenAllFieldsNull() {
    IgDescriptor fresh = new IgDescriptor();
    assertNull(fresh.getName());
    assertNull(fresh.getTitle());
    assertNull(fresh.getRef());
    assertNull(fresh.getSetOfLeaders());
    assertNull(fresh.getDescription());
    assertNull(fresh.getNbDocuments());
    assertNull(fresh.getDeepness());
  }

  @Test
  public void testSetName_whenNull_thenGetReturnsNull() {
    descriptor.setName("something");
    descriptor.setName(null);
    assertNull(descriptor.getName());
  }
}
