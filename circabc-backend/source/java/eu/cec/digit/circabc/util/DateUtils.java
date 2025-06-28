/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

  private DateUtils() {}

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
