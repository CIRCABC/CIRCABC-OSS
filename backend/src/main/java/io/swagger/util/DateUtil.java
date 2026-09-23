package io.swagger.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class providing helper methods for common {@link Date} manipulations,
 * such as normalizing the time-of-day portion of a date (to midnight or to the
 * last millisecond of the day), computing a date one month in the future, and
 * checking whether a date falls within a given age threshold.
 *
 * <p>This class is not meant to be instantiated; all functionality is exposed
 * through static methods.
 */
public class DateUtil {

  /**
   * Private constructor to hide the implicit public one.
   */
  private DateUtil() {
    // Utility class, do not instantiate
  }

  /**
   * Take the given date and set the time at 23:59:59.999
   * @param date the date to udpate
   * @return the given date with the time set to 23:59:59.999
   */
  public static Date setTimeAt23H59M59S(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);

    return calendar.getTime();
  }

  /**
   * Take the given date and set the time at 00:00:00.000
   * @param date the date to udpate
   * @return the given date with the time set at 00:00:00.000
   */
  public static Date setTimeAtMidnight(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 00);
    calendar.set(Calendar.MINUTE, 00);
    calendar.set(Calendar.SECOND, 00);
    calendar.set(Calendar.MILLISECOND, 000);

    return calendar.getTime();
  }

  /**
   * Generate a date in one month from today and set the time to 23:59:59.999
   * @return date is one month from now with the time set to 23:59:59.999
   */
  public static Date inOneMonth23h59M59S() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  /**
   * Determines whether the given date is within the specified age threshold,
   * i.e. whether the elapsed time between now and the date is less than or
   * equal to the provided number of milliseconds.
   *
   * @param date the date to evaluate
   * @param milliseconds the age threshold, in milliseconds, measured from the
   *     current time
   * @return {@code true} if the elapsed time since {@code date} is less than or
   *     equal to {@code milliseconds}; {@code false} otherwise
   */
  public static boolean isDateOlderThan(
    final Date date,
    final long milliseconds
  ) {
    boolean older = false;
    final Date now = new Date();

    if ((now.getTime() - date.getTime()) <= milliseconds) {
      older = true;
    }
    return older;
  }
}
