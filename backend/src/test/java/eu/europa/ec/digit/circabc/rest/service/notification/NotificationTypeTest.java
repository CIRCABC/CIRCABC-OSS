package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotificationTypeTest {

  @Test
  public void testValues_shouldContainAllExpectedConstants() {
    NotificationType[] values = NotificationType.values();
    assertEquals(5, values.length);
  }

  @Test
  public void testValueOf_whenValidName_thenReturnsEnum() {
    assertEquals(
      NotificationType.NOTIFY_CONTENT_UPLOAD,
      NotificationType.valueOf("NOTIFY_CONTENT_UPLOAD")
    );
    assertEquals(
      NotificationType.NOTIFY_CONTENT_UPDATE,
      NotificationType.valueOf("NOTIFY_CONTENT_UPDATE")
    );
    assertEquals(
      NotificationType.NOTIFY_USER_INVITATION,
      NotificationType.valueOf("NOTIFY_USER_INVITATION")
    );
    assertEquals(
      NotificationType.NOTIFY_USER_INVITATION_ADMINS,
      NotificationType.valueOf("NOTIFY_USER_INVITATION_ADMINS")
    );
    assertEquals(
      NotificationType.NOTIFY_USER_MEMBERSHIP_UPDATE,
      NotificationType.valueOf("NOTIFY_USER_MEMBERSHIP_UPDATE")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_whenInvalidName_thenThrowsException() {
    NotificationType.valueOf("INVALID_TYPE");
  }

  @Test(expected = NullPointerException.class)
  public void testValueOf_whenNull_thenThrowsException() {
    NotificationType.valueOf(null);
  }
}
