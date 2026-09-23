package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import io.swagger.model.NotificationStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class UserNotificationReportTest {

  private NodeRef location;

  @Before
  public void setUp() {
    location = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetters_whenConstructedWithValues_thenReturnsCorrectValues() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profileA",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );

    assertEquals(location, report.getLocation());
    assertEquals("user1", report.getUserAuthority());
    assertEquals("profileA", report.getUserProfile());
    assertEquals(
      NotificationStatus.SUBSCRIBED,
      report.getUserNotificationStatus()
    );
    assertEquals(
      NotificationStatus.INHERITED,
      report.getProfileNotificationStatus()
    );
  }

  @Test
  public void testGetGlobalNotificationStatus_whenEnabled_thenReturnsEnabled() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertEquals(
      GlobalNotificationStatus.ENABLED,
      report.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testGetGlobalNotificationStatus_whenDisabled_thenReturnsDisabled() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      false,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertEquals(
      GlobalNotificationStatus.DISABLED,
      report.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testGetGlobalNotificationStatus_whenNull_thenUsesWillReceive() {
    UserNotificationReportImpl reportTrue = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      null,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );
    assertEquals(
      GlobalNotificationStatus.ENABLED,
      reportTrue.getGlobalNotificationStatus()
    );

    UserNotificationReportImpl reportFalse = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      null,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      false
    );
    assertEquals(
      GlobalNotificationStatus.DISABLED,
      reportFalse.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testIsUserNotifiable_whenWillReceiveTrue_thenReturnsTrue() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertTrue(report.isUserNotifiable());
  }

  @Test
  public void testIsUserNotifiable_whenWillReceiveFalse_thenReturnsFalse() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.UNSUBSCRIBED,
      false
    );

    assertFalse(report.isUserNotifiable());
  }

  @Test
  public void testEquals_whenSameValues_thenEqual() {
    UserNotificationReportImpl a = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );
    UserNotificationReportImpl b = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testEquals_whenDifferentUser_thenNotEqual() {
    UserNotificationReportImpl a = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );
    UserNotificationReportImpl b = new UserNotificationReportImpl(
      location,
      "user2",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertNotEquals(a, b);
  }

  @Test
  public void testEquals_whenDifferentNotifiableStatus_thenNotEqual() {
    UserNotificationReportImpl a = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );
    UserNotificationReportImpl b = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      false
    );

    assertNotEquals(a, b);
  }

  @Test
  public void testEquals_whenNull_thenNotEqual() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertNotEquals(report, null);
  }

  @Test
  public void testEquals_whenSameInstance_thenEqual() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    assertEquals(report, report);
  }

  @Test
  public void testToString_whenNotifiable_thenContainsReceive() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );

    String str = report.toString();
    assertTrue(str.contains("user1"));
    assertTrue(str.contains("profile"));
    assertFalse(str.contains("NOT"));
  }

  @Test
  public void testToString_whenNotNotifiable_thenContainsNot() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile",
      true,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.UNSUBSCRIBED,
      false
    );

    String str = report.toString();
    assertTrue(str.contains("NOT"));
  }
}
