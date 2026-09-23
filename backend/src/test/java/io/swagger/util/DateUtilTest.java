package io.swagger.util;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

public class DateUtilTest {

  @Test
  public void testSetTimeAt23H59M59S_whenValidDate_thenSetsEndOfDay() {
    Calendar cal = Calendar.getInstance();
    cal.set(2026, Calendar.MAY, 4, 10, 30, 15);
    cal.set(Calendar.MILLISECOND, 123);
    Date input = cal.getTime();

    Date result = DateUtil.setTimeAt23H59M59S(input);

    Calendar resultCal = Calendar.getInstance();
    resultCal.setTime(result);
    assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(59, resultCal.get(Calendar.MINUTE));
    assertEquals(59, resultCal.get(Calendar.SECOND));
    assertEquals(999, resultCal.get(Calendar.MILLISECOND));
    assertEquals(2026, resultCal.get(Calendar.YEAR));
    assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH));
    assertEquals(4, resultCal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testSetTimeAtMidnight_whenValidDate_thenSetsStartOfDay() {
    Calendar cal = Calendar.getInstance();
    cal.set(2026, Calendar.MAY, 4, 14, 45, 30);
    cal.set(Calendar.MILLISECOND, 500);
    Date input = cal.getTime();

    Date result = DateUtil.setTimeAtMidnight(input);

    Calendar resultCal = Calendar.getInstance();
    resultCal.setTime(result);
    assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, resultCal.get(Calendar.MINUTE));
    assertEquals(0, resultCal.get(Calendar.SECOND));
    assertEquals(0, resultCal.get(Calendar.MILLISECOND));
    assertEquals(2026, resultCal.get(Calendar.YEAR));
    assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH));
    assertEquals(4, resultCal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testInOneMonth23h59M59S_thenReturnsDateOneMonthAheadAtEndOfDay() {
    Calendar before = Calendar.getInstance();
    before.add(Calendar.MONTH, 1);

    Date result = DateUtil.inOneMonth23h59M59S();

    Calendar resultCal = Calendar.getInstance();
    resultCal.setTime(result);
    assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(59, resultCal.get(Calendar.MINUTE));
    assertEquals(59, resultCal.get(Calendar.SECOND));
    assertEquals(999, resultCal.get(Calendar.MILLISECOND));
    assertEquals(before.get(Calendar.MONTH), resultCal.get(Calendar.MONTH));
    assertEquals(before.get(Calendar.YEAR), resultCal.get(Calendar.YEAR));
  }

  @Test
  public void testIsDateOlderThan_whenDateWithinThreshold_thenReturnsTrue() {
    Date recent = new Date(System.currentTimeMillis() - 1000);
    assertTrue(DateUtil.isDateOlderThan(recent, 5000));
  }

  @Test
  public void testIsDateOlderThan_whenDateExceedsThreshold_thenReturnsFalse() {
    Date old = new Date(System.currentTimeMillis() - 10000);
    assertFalse(DateUtil.isDateOlderThan(old, 5000));
  }
}
