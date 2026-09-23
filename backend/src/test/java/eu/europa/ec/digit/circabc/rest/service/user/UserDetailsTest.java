package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;

import java.util.Locale;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class UserDetailsTest {

  private UserDetails userDetails;

  @Before
  public void setUp() {
    userDetails = new UserDetails();
  }

  @Test
  public void testNodeRef_whenSet_thenReturnsValue() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    userDetails.setNodeRef(ref);
    assertEquals(ref, userDetails.getNodeRef());
  }

  @Test
  public void testNodeRef_whenNotSet_thenReturnsNull() {
    assertNull(userDetails.getNodeRef());
  }

  @Test
  public void testUserName_whenSet_thenReturnsValue() {
    userDetails.setUserName("john.doe");
    assertEquals("john.doe", userDetails.getUserName());
  }

  @Test
  public void testEmail_whenSet_thenReturnsValue() {
    userDetails.setEmail("john@example.com");
    assertEquals("john@example.com", userDetails.getEmail());
  }

  @Test
  public void testFirstName_whenSet_thenReturnsValue() {
    userDetails.setFirstName("John");
    assertEquals("John", userDetails.getFirstName());
  }

  @Test
  public void testLastName_whenSet_thenReturnsValue() {
    userDetails.setLastName("Doe");
    assertEquals("Doe", userDetails.getLastName());
  }

  @Test
  public void testGlobalNotification_whenSet_thenReturnsValue() {
    userDetails.setGlobalNotification(Boolean.TRUE);
    assertEquals(Boolean.TRUE, userDetails.getGlobalNotification());
  }

  @Test
  public void testVisibility_whenSet_thenReturnsValue() {
    userDetails.setVisibility(Boolean.FALSE);
    assertEquals(Boolean.FALSE, userDetails.getVisibility());
  }

  @Test
  public void testContentFilterLanguage_whenSet_thenReturnsValue() {
    userDetails.setContentFilterLanguage(Locale.FRENCH);
    assertEquals(Locale.FRENCH, userDetails.getContentFilterLanguage());
  }

  @Test
  public void testAvatar_whenSet_thenReturnsValue() {
    NodeRef avatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-id"
    );
    userDetails.setAvatar(avatarRef);
    assertEquals(avatarRef, userDetails.getAvatar());
  }

  @Test
  public void testAllFields_whenSetToNull_thenReturnNull() {
    userDetails.setUserName(null);
    userDetails.setEmail(null);
    userDetails.setFirstName(null);
    userDetails.setLastName(null);
    userDetails.setDescription(null);
    userDetails.setFax(null);
    userDetails.setOrganisation(null);
    userDetails.setPhone(null);
    userDetails.setPostalAddress(null);
    userDetails.setTitle(null);
    userDetails.setUrl(null);
    userDetails.setGlobalNotification(null);
    userDetails.setVisibility(null);
    userDetails.setUserInterfaceLanguage(null);
    userDetails.setSignature(null);
    userDetails.setAvatar(null);
    userDetails.setContentFilterLanguage(null);

    assertNull(userDetails.getUserName());
    assertNull(userDetails.getEmail());
    assertNull(userDetails.getFirstName());
    assertNull(userDetails.getLastName());
    assertNull(userDetails.getDescription());
    assertNull(userDetails.getFax());
    assertNull(userDetails.getOrganisation());
    assertNull(userDetails.getPhone());
    assertNull(userDetails.getPostalAddress());
    assertNull(userDetails.getTitle());
    assertNull(userDetails.getUrl());
    assertNull(userDetails.getGlobalNotification());
    assertNull(userDetails.getVisibility());
    assertNull(userDetails.getUserInterfaceLanguage());
    assertNull(userDetails.getSignature());
    assertNull(userDetails.getAvatar());
    assertNull(userDetails.getContentFilterLanguage());
  }

  @Test
  public void testRemainingStringFields_whenSet_thenReturnValues() {
    userDetails.setDescription("A description");
    userDetails.setFax("+32 2 123456");
    userDetails.setOrganisation("EC");
    userDetails.setPhone("+32 2 654321");
    userDetails.setPostalAddress("Brussels");
    userDetails.setTitle("Mr");
    userDetails.setUrl("https://example.com");
    userDetails.setUserInterfaceLanguage("en");
    userDetails.setSignature("-- John");

    assertEquals("A description", userDetails.getDescription());
    assertEquals("+32 2 123456", userDetails.getFax());
    assertEquals("EC", userDetails.getOrganisation());
    assertEquals("+32 2 654321", userDetails.getPhone());
    assertEquals("Brussels", userDetails.getPostalAddress());
    assertEquals("Mr", userDetails.getTitle());
    assertEquals("https://example.com", userDetails.getUrl());
    assertEquals("en", userDetails.getUserInterfaceLanguage());
    assertEquals("-- John", userDetails.getSignature());
  }
}
