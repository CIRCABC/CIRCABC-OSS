package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import io.swagger.model.NotificationStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class UserNotificationReportImplTest {

  private NodeRef location;

  @Before
  public void setUp() {
    location = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetUserAuthority_returnsUser() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "admin",
      "CIRCABC",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals("admin", report.getUserAuthority());
  }

  @Test
  public void testGetLocation_returnsNodeRef() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals(location, report.getLocation());
  }

  @Test
  public void testGetUserProfile_returnsProfile() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "MyProfile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals("MyProfile", report.getUserProfile());
  }

  @Test
  public void testIsUserNotifiable_whenWillReceiveTrue_returnsTrue() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.SUBSCRIBED,
      true
    );
    assertTrue(report.isUserNotifiable());
  }

  @Test
  public void testIsUserNotifiable_whenWillReceiveFalse_returnsFalse() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.UNSUBSCRIBED,
      false
    );
    assertFalse(report.isUserNotifiable());
  }

  @Test
  public void testGetGlobalNotificationStatus_whenEnabled_returnsEnabled() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals(
      GlobalNotificationStatus.ENABLED,
      report.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testGetGlobalNotificationStatus_whenDisabled_returnsDisabled() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      false,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals(
      GlobalNotificationStatus.DISABLED,
      report.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testGetGlobalNotificationStatus_whenNull_usesWillReceive() {
    UserNotificationReportImpl reportTrue = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      null,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals(
      GlobalNotificationStatus.ENABLED,
      reportTrue.getGlobalNotificationStatus()
    );

    UserNotificationReportImpl reportFalse = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      null,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      false
    );
    assertEquals(
      GlobalNotificationStatus.DISABLED,
      reportFalse.getGlobalNotificationStatus()
    );
  }

  @Test
  public void testGetUserNotificationStatus_returnsStatus() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.INHERITED,
      false
    );
    assertEquals(
      NotificationStatus.UNSUBSCRIBED,
      report.getUserNotificationStatus()
    );
  }

  @Test
  public void testGetProfileNotificationStatus_returnsStatus() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertEquals(
      NotificationStatus.INHERITED,
      report.getProfileNotificationStatus()
    );
  }

  @Test
  public void testEquals_sameObject_returnsTrue() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertTrue(report.equals(report));
  }

  @Test
  public void testEquals_null_returnsFalse() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report.equals(null));
  }

  @Test
  public void testEquals_differentClass_returnsFalse() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report.equals("not a report"));
  }

  @Test
  public void testEquals_equalObjects_returnsTrue() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertTrue(report1.equals(report2));
    assertEquals(report1.hashCode(), report2.hashCode());
  }

  @Test
  public void testEquals_differentUser_returnsFalse() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user2",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report1.equals(report2));
  }

  @Test
  public void testEquals_differentComputedStatus_returnsFalse() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      false
    );
    assertFalse(report1.equals(report2));
  }

  @Test
  public void testEquals_differentLocation_returnsFalse() {
    NodeRef otherLocation = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-id"
    );
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      otherLocation,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report1.equals(report2));
  }

  @Test
  public void testEquals_differentProfile_returnsFalse() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile2",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report1.equals(report2));
  }

  @Test
  public void testToString_containsUserAndProfile() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "admin",
      "AdminProfile",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    String str = report.toString();
    assertTrue(str.contains("admin"));
    assertTrue(str.contains("AdminProfile"));
    assertFalse(str.contains("NOT"));
  }

  @Test
  public void testToString_whenNotNotifiable_containsNot() {
    UserNotificationReportImpl report = new UserNotificationReportImpl(
      location,
      "admin",
      "AdminProfile",
      true,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.INHERITED,
      false
    );
    String str = report.toString();
    assertTrue(str.contains("NOT"));
  }

  @Test
  public void testHashCode_equalObjects_sameHashCode() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      false,
      NotificationStatus.UNSUBSCRIBED,
      NotificationStatus.UNSUBSCRIBED,
      true
    );
    assertEquals(report1.hashCode(), report2.hashCode());
  }

  @Test
  public void testEquals_nullUserBothSides_returnsTrue() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      null,
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      null,
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertTrue(report1.equals(report2));
  }

  @Test
  public void testEquals_nullUserOneSide_returnsFalse() {
    UserNotificationReportImpl report1 = new UserNotificationReportImpl(
      location,
      null,
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    UserNotificationReportImpl report2 = new UserNotificationReportImpl(
      location,
      "user1",
      "profile1",
      true,
      NotificationStatus.SUBSCRIBED,
      NotificationStatus.INHERITED,
      true
    );
    assertFalse(report1.equals(report2));
  }
}
