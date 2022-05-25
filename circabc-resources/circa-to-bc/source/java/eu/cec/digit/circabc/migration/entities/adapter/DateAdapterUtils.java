/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

/**
 * Contains method for marshalling/unmarshalling XMLDate to/from JRe Date
 *
 * @author Yanick Pignot
 */
public abstract class DateAdapterUtils
{
	private DateAdapterUtils(){}

	/**
	 * Marshall a date value to a Date Time String
	 *
	 * @param jreDate				The date
	 * @return						The date time string value
	 */
	public static String marshalDateTime(final Date jreDate)
	{
		return DatatypeConverter.printDateTime(getCalendar(jreDate));
	}

	/**
	 * UnMarshall a date time String value to a jre date instance
	 *
	 * @param xmlDate				the date time string value to parse
	 * @return						The jre date
	 * @throws Exception			If the convertion fails
	 */
	public static Date unmarshalDateTime(final String xmlDate) throws Exception
	{
		return getTime(DatatypeConverter.parseDateTime(xmlDate));
	}


	/**
	 * Marshall a date value to a Date String
	 *
	 * @param jreDate				The date
	 * @return						The date string value
	 */
	public static String marshalDate(final Date jreDate)
	{
		return DatatypeConverter.printDate(getCalendar(jreDate));
	}

	/**
	 * UnMarshall a date String value to a jre date instance
	 *
	 * @param xmlDate				the date string value to parse
	 * @return						The jre date
	 * @throws Exception			If the convertion fails
	 */
	public static Date unmarshalDate(final String xmlDate) throws Exception
	{
		return getTime(DatatypeConverter.parseDate(xmlDate));
	}

	/**
	 * Marshall a date value to a Time String
	 *
	 * @param jreDate				The date
	 * @return						The time string value
	 */
	public static String marshalTime(final Date jreDate)
	{
		return DatatypeConverter.printTime(getCalendar(jreDate));
	}

	/**
	 * UnMarshall a time String value to a jre date instance
	 *
	 * @param xmlDate				the time string value to parse
	 * @return						The jre date
	 * @throws Exception			If the convertion fails
	 */
	public static Date unmarshalTime(final String xmlDate) throws Exception
	{
		return getTime(DatatypeConverter.parseTime(xmlDate));
	}


	private static Date getTime(Calendar calendar)
	{
		return calendar.getTime();
	}
	private static Calendar getCalendar(Date jreDate)
	{
		final Calendar cal = new GregorianCalendar();
	    cal.setTime(jreDate);

	    return cal;
	}

}


