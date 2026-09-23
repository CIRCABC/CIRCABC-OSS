package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import io.swagger.model.NotificationStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.junit.Before;
import org.junit.Test;

public class AuthorityNotificationImplTest {

  private AuthorityNotificationImpl instance;

  @Before
  public void setUp() {
    instance = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "GROUP_TestGroup",
      true
    );
  }

  @Test
  public void testConstructor_withInherited_setsAllFields() {
    assertEquals(
      NotificationStatus.SUBSCRIBED,
      instance.getNotificationStatus()
    );
    assertEquals("GROUP_TestGroup", instance.getAuthority());
    assertEquals(AuthorityType.GROUP, instance.getAuthorityType());
    assertTrue(instance.getInherited());
  }

  @Test
  public void testConstructor_withoutInherited_defaultsFalse() {
    AuthorityNotificationImpl impl = new AuthorityNotificationImpl(
      NotificationStatus.UNSUBSCRIBED,
      "admin"
    );
    assertEquals(NotificationStatus.UNSUBSCRIBED, impl.getNotificationStatus());
    assertEquals("admin", impl.getAuthority());
    assertEquals(AuthorityType.USER, impl.getAuthorityType());
    assertFalse(impl.getInherited());
  }

  @Test
  public void testEquals_sameObject_returnsTrue() {
    assertTrue(instance.equals(instance));
  }

  @Test
  public void testEquals_equalObjects_returnsTrue() {
    AuthorityNotificationImpl other = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "GROUP_TestGroup",
      false
    );
    assertTrue(instance.equals(other));
  }

  @Test
  public void testEquals_differentAuthority_returnsFalse() {
    AuthorityNotificationImpl other = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "GROUP_Other"
    );
    assertFalse(instance.equals(other));
  }

  @Test
  public void testEquals_differentStatus_returnsFalse() {
    AuthorityNotificationImpl other = new AuthorityNotificationImpl(
      NotificationStatus.UNSUBSCRIBED,
      "GROUP_TestGroup"
    );
    assertFalse(instance.equals(other));
  }

  @Test
  public void testEquals_nullObject_returnsFalse() {
    assertFalse(instance.equals(null));
  }

  @Test
  public void testEquals_differentType_returnsFalse() {
    assertFalse(instance.equals("not an AuthorityNotificationImpl"));
  }

  @Test
  public void testHashCode_equalObjects_sameHashCode() {
    AuthorityNotificationImpl other = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "GROUP_TestGroup"
    );
    assertEquals(instance.hashCode(), other.hashCode());
  }

  @Test
  public void testToString_containsAllInfo() {
    String result = instance.toString();
    assertTrue(result.contains("SUBSCRIBED"));
    assertTrue(result.contains("GROUP_TestGroup"));
    assertTrue(result.contains("GROUP"));
  }
}
