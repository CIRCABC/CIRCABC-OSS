package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.joda.time.LocalTime;
import org.junit.Test;

public class AppointmentUtilsTest {

  @Test
  public void testConvertLocalTimeToDate_whenValidTime_thenDateHasCorrectHourMinuteSecond() {
    LocalTime localTime = new LocalTime(14, 30, 45);

    Date result = AppointmentUtils.convertLocalTimeToDate(localTime);

    Calendar cal = Calendar.getInstance();
    cal.setTime(result);
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(30, cal.get(Calendar.MINUTE));
    assertEquals(45, cal.get(Calendar.SECOND));
  }

  @Test
  public void testConvertDateValueToDate_whenValidDateValue_thenDateHasCorrectYearMonthDay() {
    DateValue dateValue = new DateValueImpl(2025, 3, 15);

    Date result = AppointmentUtils.convertDateValueToDate(dateValue);

    Calendar cal = Calendar.getInstance();
    cal.setTime(result);
    assertEquals(2025, cal.get(Calendar.YEAR));
    assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
    assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testConvertDateToLocalTime_whenValidDate_thenLocalTimeMatchesHourMinuteSecond() {
    Calendar cal = Calendar.getInstance();
    cal.set(2025, Calendar.JUNE, 10, 9, 15, 30);
    Date date = cal.getTime();

    LocalTime result = AppointmentUtils.convertDateToLocalTime(date);

    assertEquals(9, result.getHourOfDay());
    assertEquals(15, result.getMinuteOfHour());
    assertEquals(30, result.getSecondOfMinute());
  }

  @Test
  public void testConvertDateToDateValue_whenValidDate_thenDateValueMatchesYearMonthDay() {
    Calendar cal = Calendar.getInstance();
    cal.set(2025, Calendar.DECEMBER, 25, 10, 0, 0);
    Date date = cal.getTime();

    DateValue result = AppointmentUtils.convertDateToDateValue(date);

    assertEquals(2025, result.year());
    assertEquals(12, result.month());
    assertEquals(25, result.day());
  }

  @Test
  public void testConvertLocalTimeDateValueToDate_whenValidInputs_thenCombinesDateAndTime() {
    LocalTime localTime = new LocalTime(16, 45, 10);
    DateValue dateValue = new DateValueImpl(2025, 7, 4);

    Date result = AppointmentUtils.convertLocalTimeDateValueToDate(
      localTime,
      dateValue
    );

    Calendar cal = Calendar.getInstance();
    cal.setTime(result);
    assertEquals(2025, cal.get(Calendar.YEAR));
    assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
    assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(16, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(45, cal.get(Calendar.MINUTE));
    assertEquals(10, cal.get(Calendar.SECOND));
  }

  @Test
  public void testConvertLocalTimeDateValueTimezoneToGMTString_whenCET_thenConvertsToGMT() {
    LocalTime localTime = new LocalTime(12, 0, 0);
    DateValue dateValue = new DateValueImpl(2025, 1, 15);

    String result =
      AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
        localTime,
        dateValue,
        "CET"
      );

    // CET is UTC+1 in January, so 12:00 CET = 11:00 GMT
    assertEquals("20250115T110000", result);
  }

  @Test
  public void testConvertLocalTimeDateTimezoneToGMTString_whenUTC_thenNoOffset() {
    LocalTime localTime = new LocalTime(8, 30, 0);
    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    cal.set(2025, Calendar.MARCH, 20, 0, 0, 0);
    Date date = cal.getTime();

    String result = AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
      localTime,
      date,
      "UTC"
    );

    assertEquals("20250320T083000", result);
  }

  @Test
  public void testGetStartOfDay_whenValidDate_thenTimeIsMidnight() {
    Calendar cal = Calendar.getInstance();
    cal.set(2025, Calendar.MAY, 4, 15, 30, 45);
    cal.set(Calendar.MILLISECOND, 500);
    Date date = cal.getTime();

    Date result = AppointmentUtils.getStartOfDay(date);

    Calendar resultCal = Calendar.getInstance();
    resultCal.setTime(result);
    assertEquals(2025, resultCal.get(Calendar.YEAR));
    assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH));
    assertEquals(4, resultCal.get(Calendar.DAY_OF_MONTH));
    assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, resultCal.get(Calendar.MINUTE));
    assertEquals(0, resultCal.get(Calendar.SECOND));
    assertEquals(0, resultCal.get(Calendar.MILLISECOND));
  }

  @Test
  public void testGetEndOfDay_whenValidDate_thenTimeIsEndOfDay() {
    Calendar cal = Calendar.getInstance();
    cal.set(2025, Calendar.MAY, 4, 8, 0, 0);
    Date date = cal.getTime();

    Date result = AppointmentUtils.getEndOfDay(date);

    Calendar resultCal = Calendar.getInstance();
    resultCal.setTime(result);
    assertEquals(2025, resultCal.get(Calendar.YEAR));
    assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH));
    assertEquals(4, resultCal.get(Calendar.DAY_OF_MONTH));
    assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(59, resultCal.get(Calendar.MINUTE));
    assertEquals(59, resultCal.get(Calendar.SECOND));
    assertEquals(999, resultCal.get(Calendar.MILLISECOND));
  }

  @Test
  public void testGetStartOfDay_whenAlreadyMidnight_thenUnchanged() {
    Calendar cal = Calendar.getInstance();
    cal.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();

    Date result = AppointmentUtils.getStartOfDay(date);

    assertEquals(date, result);
  }
}
