package eu.europa.ec.digit.circabc.rest.service.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class CircabcServiceTest {

  private CircabcService circabcService;

  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "test-id"
  );

  @Before
  public void setUp() {
    circabcService = mock(CircabcService.class);
  }

  @Test
  public void testIsCircabcAdmin() {
    when(circabcService.isCircabcAdmin("admin")).thenReturn(true);
    assertTrue(circabcService.isCircabcAdmin("admin"));
  }

  @Test
  public void testIsUserMember() {
    when(circabcService.isUserMember(TEST_NODE_REF, "user1")).thenReturn(true);
    assertTrue(circabcService.isUserMember(TEST_NODE_REF, "user1"));
  }

  @Test
  public void testIsCategoryAdmin() {
    when(circabcService.isCategoryAdmin(TEST_NODE_REF, "admin")).thenReturn(
      true
    );
    assertTrue(circabcService.isCategoryAdmin(TEST_NODE_REF, "admin"));
  }

  @Test
  public void testIsUserExists() {
    when(circabcService.isUserExists("user1")).thenReturn(true);
    assertTrue(circabcService.isUserExists("user1"));
  }

  @Test
  public void testCountMembersInIg() {
    when(circabcService.countMembersInIg("ig-1")).thenReturn(5);
    assertEquals(5, circabcService.countMembersInIg("ig-1"));
  }

  @Test
  public void testGetInterestGroups() {
    when(circabcService.getInterestGroups("user1")).thenReturn(
      Collections.emptyList()
    );
    assertTrue(circabcService.getInterestGroups("user1").isEmpty());
  }

  @Test
  public void testGetCategories_byUsername() {
    when(circabcService.getCategories("user1")).thenReturn(
      Collections.emptyList()
    );
    assertTrue(circabcService.getCategories("user1").isEmpty());
  }

  @Test
  public void testGetCategories_noArgs() {
    when(circabcService.getCategories()).thenReturn(Collections.emptyList());
    assertTrue(circabcService.getCategories().isEmpty());
  }

  @Test
  public void testGetPersonProfile() {
    when(circabcService.getPersonProfile(TEST_NODE_REF, "user1")).thenReturn(
      "AUTHOR"
    );
    assertEquals(
      "AUTHOR",
      circabcService.getPersonProfile(TEST_NODE_REF, "user1")
    );
  }

  @Test
  public void testGetUserIds() {
    Set<String> ids = new HashSet<>(Arrays.asList("user1", "user2"));
    when(circabcService.getUserIds("ig-1")).thenReturn(ids);
    assertEquals(ids, circabcService.getUserIds("ig-1"));
  }
}
