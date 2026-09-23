package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;

import org.junit.Test;

public class GlobalNotificationStatusTest {

  @Test
  public void testEnabled_toBoolean_returnsTrue() {
    assertTrue(GlobalNotificationStatus.ENABLED.toBoolean());
  }

  @Test
  public void testDisabled_toBoolean_returnsFalse() {
    assertFalse(GlobalNotificationStatus.DISABLED.toBoolean());
  }

  @Test
  public void testValues_containsBothConstants() {
    GlobalNotificationStatus[] values = GlobalNotificationStatus.values();
    assertEquals(2, values.length);
  }

  @Test
  public void testValueOf_enabled() {
    assertEquals(
      GlobalNotificationStatus.ENABLED,
      GlobalNotificationStatus.valueOf("ENABLED")
    );
  }

  @Test
  public void testValueOf_disabled() {
    assertEquals(
      GlobalNotificationStatus.DISABLED,
      GlobalNotificationStatus.valueOf("DISABLED")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalid_throwsException() {
    GlobalNotificationStatus.valueOf("INVALID");
  }
}
