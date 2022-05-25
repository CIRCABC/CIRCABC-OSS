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
package eu.cec.digit.circabc.repo.event;

import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** */
public abstract class AppointmentUtils {

    private static final String GMT = "GMT";
    private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

    private AppointmentUtils() {
    }

    public static Date convertLocalTimeToDate(LocalTime LocalTime) {
        // from Joda to JDK
        DateTime dt = new DateTime();
        dt =
                dt.withTime(
                        LocalTime.getHourOfDay(),
                        LocalTime.getMinuteOfHour(),
                        LocalTime.getSecondOfMinute(),
                        0);

        return dt.toDate();
    }

    public static Date convertDateValueToDate(DateValue dateValue) {
        DateTime d = new DateTime();
        d = d.withDate(dateValue.year(), dateValue.month(), dateValue.day());

        return d.toDate();
    }

    public static LocalTime convertDateToLocalTime(Date date) {
        DateTime dt = new DateTime(date);
        return dt.toLocalTime();
    }

    public static DateValue convertDateToDateValue(Date date) {
        DateTime dt = new DateTime(date);
        return new DateValueImpl(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
    }

    public static Date convertLocalTimeDateValueToDate(LocalTime LocalTime, DateValue dateValue) {
        DateTime d = new DateTime();
        d = d.withDate(dateValue.year(), dateValue.month(), dateValue.day());
        d =
                d.withTime(
                        LocalTime.getHourOfDay(),
                        LocalTime.getMinuteOfHour(),
                        LocalTime.getSecondOfMinute(),
                        0);

        return d.toDate();
    }

    public static String convertLocalTimeDateValueTimezoneToGMTString(
            LocalTime LocalTime, DateValue dateValue, String timezoneID) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone(timezoneID));

        gregorianCalendar.set(Calendar.YEAR, dateValue.year());
        gregorianCalendar.set(Calendar.MONTH, dateValue.month() - 1);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, dateValue.day());
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, LocalTime.getHourOfDay());
        gregorianCalendar.set(Calendar.MINUTE, LocalTime.getMinuteOfHour());
        gregorianCalendar.set(Calendar.SECOND, LocalTime.getSecondOfMinute());
        GregorianCalendar gmtCalendar = new GregorianCalendar(TimeZone.getTimeZone(GMT));
        gmtCalendar.setTimeInMillis(gregorianCalendar.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(GMT));
        return sdf.format(gmtCalendar.getTime());
    }

    public static String convertLocalTimeDateTimezoneToGMTString(
            LocalTime LocalTime, Date date, String timezoneID) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone(timezoneID));
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, LocalTime.getHourOfDay());
        gregorianCalendar.set(Calendar.MINUTE, LocalTime.getMinuteOfHour());
        gregorianCalendar.set(Calendar.SECOND, LocalTime.getSecondOfMinute());
        GregorianCalendar gmtCalendar = new GregorianCalendar(TimeZone.getTimeZone(GMT));
        gmtCalendar.setTimeInMillis(gregorianCalendar.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(GMT));
        return sdf.format(gmtCalendar.getTime());
    }
}
