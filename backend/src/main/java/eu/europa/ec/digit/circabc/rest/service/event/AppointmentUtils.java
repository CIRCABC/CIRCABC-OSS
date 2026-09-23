/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.event;

import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Utility helper for converting between the various date and time representations used when handling
 * calendar appointments/events.
 *
 * <p>Appointment data is exchanged across several time models within CIRCABC: Joda-Time {@link
 * LocalTime} for the time-of-day portion, the {@code google-ical} {@link DateValue} for the
 * date portion of recurrence rules, and the standard JDK {@link Date}. This class centralises the
 * conversions between those types and also provides helpers for formatting date/time values as
 * GMT-normalised strings and for computing day boundaries.
 *
 * <p>The class is stateless and cannot be instantiated; all operations are exposed as static
 * methods.
 */
public abstract class AppointmentUtils {

  /** Identifier of the GMT time zone used as the normalisation target for formatted output. */
  private static final String GMT = "GMT";

  /** {@link SimpleDateFormat} pattern used to render normalised date/time strings (basic ISO-8601 style, e.g. {@code 20240131T235959}). */
  private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

  /** Private constructor to prevent instantiation of this static utility class. */
  private AppointmentUtils() {}

  /**
   * Converts a Joda-Time {@link LocalTime} into a JDK {@link Date}.
   *
   * <p>The date portion of the returned value is taken from the current date; only the
   * hour/minute/second of the supplied time are applied (milliseconds are set to zero).
   *
   * @param localTime the time-of-day to convert
   * @return a {@link Date} on the current day with the given time-of-day
   */
  public static Date convertLocalTimeToDate(LocalTime localTime) {
    // from Joda to JDK
    DateTime dt = new DateTime();
    dt = dt.withTime(
      localTime.getHourOfDay(),
      localTime.getMinuteOfHour(),
      localTime.getSecondOfMinute(),
      0
    );

    return dt.toDate();
  }

  /**
   * Converts an iCal {@link DateValue} (year/month/day) into a JDK {@link Date}.
   *
   * <p>The time-of-day portion of the returned value is taken from the current time.
   *
   * @param dateValue the calendar date to convert
   * @return a {@link Date} representing the given date with the current time-of-day
   */
  public static Date convertDateValueToDate(DateValue dateValue) {
    DateTime d = new DateTime();
    d = d.withDate(dateValue.year(), dateValue.month(), dateValue.day());

    return d.toDate();
  }

  /**
   * Extracts the time-of-day from a JDK {@link Date} as a Joda-Time {@link LocalTime}.
   *
   * @param date the date whose time-of-day is to be extracted
   * @return the {@link LocalTime} corresponding to the given date
   */
  public static LocalTime convertDateToLocalTime(Date date) {
    DateTime dt = new DateTime(date);
    return dt.toLocalTime();
  }

  /**
   * Extracts the calendar date (year/month/day) from a JDK {@link Date} as an iCal {@link
   * DateValue}.
   *
   * @param date the date to convert
   * @return the {@link DateValue} corresponding to the given date
   */
  public static DateValue convertDateToDateValue(Date date) {
    DateTime dt = new DateTime(date);
    return new DateValueImpl(
      dt.getYear(),
      dt.getMonthOfYear(),
      dt.getDayOfMonth()
    );
  }

  /**
   * Combines a calendar date and a time-of-day into a single JDK {@link Date}.
   *
   * @param localTime the time-of-day component (hour/minute/second; milliseconds set to zero)
   * @param dateValue the calendar date component (year/month/day)
   * @return a {@link Date} composed from the given date and time-of-day
   */
  public static Date convertLocalTimeDateValueToDate(
    LocalTime localTime,
    DateValue dateValue
  ) {
    DateTime d = new DateTime();
    d = d.withDate(dateValue.year(), dateValue.month(), dateValue.day());
    d = d.withTime(
      localTime.getHourOfDay(),
      localTime.getMinuteOfHour(),
      localTime.getSecondOfMinute(),
      0
    );

    return d.toDate();
  }

  /**
   * Builds a date/time from the given calendar date and time-of-day interpreted in the specified
   * time zone, converts it to GMT and formats it using the {@code yyyyMMdd'T'HHmmss} pattern.
   *
   * @param localTime the time-of-day component, interpreted in {@code timezoneID}
   * @param dateValue the calendar date component (year/month/day), interpreted in {@code
   *     timezoneID}
   * @param timezoneID the identifier of the source time zone (see {@link TimeZone#getTimeZone(String)})
   * @return the GMT-normalised date/time formatted as {@code yyyyMMdd'T'HHmmss}
   */
  public static String convertLocalTimeDateValueTimezoneToGMTString(
    LocalTime localTime,
    DateValue dateValue,
    String timezoneID
  ) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar(
      TimeZone.getTimeZone(timezoneID)
    );

    gregorianCalendar.set(Calendar.YEAR, dateValue.year());
    gregorianCalendar.set(Calendar.MONTH, dateValue.month() - 1);
    gregorianCalendar.set(Calendar.DAY_OF_MONTH, dateValue.day());
    gregorianCalendar.set(Calendar.HOUR_OF_DAY, localTime.getHourOfDay());
    gregorianCalendar.set(Calendar.MINUTE, localTime.getMinuteOfHour());
    gregorianCalendar.set(Calendar.SECOND, localTime.getSecondOfMinute());
    GregorianCalendar gmtCalendar = new GregorianCalendar(
      TimeZone.getTimeZone(GMT)
    );
    gmtCalendar.setTimeInMillis(gregorianCalendar.getTimeInMillis());
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone(GMT));
    return sdf.format(gmtCalendar.getTime());
  }

  /**
   * Takes an existing {@link Date}, overrides its time-of-day with the supplied {@link LocalTime}
   * interpreted in the specified time zone, converts the result to GMT and formats it using the
   * {@code yyyyMMdd'T'HHmmss} pattern.
   *
   * @param localTime the time-of-day to apply, interpreted in {@code timezoneID}
   * @param date the base date supplying the year/month/day (and initial time)
   * @param timezoneID the identifier of the source time zone (see {@link TimeZone#getTimeZone(String)})
   * @return the GMT-normalised date/time formatted as {@code yyyyMMdd'T'HHmmss}
   */
  public static String convertLocalTimeDateTimezoneToGMTString(
    LocalTime localTime,
    Date date,
    String timezoneID
  ) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar(
      TimeZone.getTimeZone(timezoneID)
    );
    gregorianCalendar.setTime(date);
    gregorianCalendar.set(Calendar.HOUR_OF_DAY, localTime.getHourOfDay());
    gregorianCalendar.set(Calendar.MINUTE, localTime.getMinuteOfHour());
    gregorianCalendar.set(Calendar.SECOND, localTime.getSecondOfMinute());
    GregorianCalendar gmtCalendar = new GregorianCalendar(
      TimeZone.getTimeZone(GMT)
    );
    gmtCalendar.setTimeInMillis(gregorianCalendar.getTimeInMillis());
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone(GMT));
    return sdf.format(gmtCalendar.getTime());
  }

  /**
   * Returns a copy of the given date set to the very start of that day (00:00:00.000) in the
   * default time zone.
   *
   * @param date the reference date
   * @return a {@link Date} at 00:00:00.000 on the same day as {@code date}
   */
  public static Date getStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Returns a copy of the given date set to the very end of that day (23:59:59.999) in the default
   * time zone.
   *
   * @param date the reference date
   * @return a {@link Date} at 23:59:59.999 on the same day as {@code date}
   */
  public static Date getEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }
}
