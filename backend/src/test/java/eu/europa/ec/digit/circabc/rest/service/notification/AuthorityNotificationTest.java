package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import io.swagger.model.NotificationStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.junit.Test;

public class AuthorityNotificationTest {

  @Test
  public void testConstructor_whenUserAuthority_thenFieldsSetCorrectly() {
    AuthorityNotificationImpl notification = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "testuser",
      true
    );

    assertEquals(
      NotificationStatus.SUBSCRIBED,
      notification.getNotificationStatus()
    );
    assertEquals("testuser", notification.getAuthority());
    assertEquals(AuthorityType.USER, notification.getAuthorityType());
    assertTrue(notification.getInherited());
  }

  @Test
  public void testConstructor_whenGroupAuthority_thenAuthorityTypeIsGroup() {
    AuthorityNotificationImpl notification = new AuthorityNotificationImpl(
      NotificationStatus.UNSUBSCRIBED,
      "GROUP_admins"
    );

    assertEquals(
      NotificationStatus.UNSUBSCRIBED,
      notification.getNotificationStatus()
    );
    assertEquals("GROUP_admins", notification.getAuthority());
    assertEquals(AuthorityType.GROUP, notification.getAuthorityType());
    assertFalse(notification.getInherited());
  }

  @Test
  public void testTwoArgConstructor_whenCalled_thenInheritedIsFalse() {
    AuthorityNotificationImpl notification = new AuthorityNotificationImpl(
      NotificationStatus.INHERITED,
      "user1"
    );

    assertFalse(notification.getInherited());
  }

  @Test
  public void testEquals_whenSameAuthorityAndStatus_thenEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );
    AuthorityNotificationImpl b = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1",
      true
    );

    assertEquals(a, b);
  }

  @Test
  public void testEquals_whenDifferentAuthority_thenNotEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );
    AuthorityNotificationImpl b = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user2"
    );

    assertNotEquals(a, b);
  }

  @Test
  public void testEquals_whenDifferentStatus_thenNotEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );
    AuthorityNotificationImpl b = new AuthorityNotificationImpl(
      NotificationStatus.UNSUBSCRIBED,
      "user1"
    );

    assertNotEquals(a, b);
  }

  @Test
  public void testEquals_whenSameInstance_thenEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );

    assertEquals(a, a);
  }

  @Test
  public void testEquals_whenComparedToNull_thenNotEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );

    assertNotEquals(a, null);
  }

  @Test
  public void testEquals_whenDifferentType_thenNotEqual() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );

    assertNotEquals(a, "not a notification");
  }

  @Test
  public void testHashCode_whenEqualObjects_thenSameHashCode() {
    AuthorityNotificationImpl a = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );
    AuthorityNotificationImpl b = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1",
      true
    );

    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testToString_whenCalled_thenContainsAllFields() {
    AuthorityNotificationImpl notification = new AuthorityNotificationImpl(
      NotificationStatus.SUBSCRIBED,
      "user1"
    );

    String result = notification.toString();
    assertTrue(result.contains("SUBSCRIBED"));
    assertTrue(result.contains("user1"));
    assertTrue(result.contains("USER"));
  }
}
