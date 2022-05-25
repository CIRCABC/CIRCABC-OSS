/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Convert a berkeley value to a permission item.
 *
 * @author Yanick Pignot
 */
public class ApplicationItemConverter implements ValueConverter<ApplicationItem>
{
	/** */
	private static final long serialVersionUID = 3413451351090202078L;

	private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		}
	};

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.ValueConverter#adapt(java.lang.String)
	 */
	public ApplicationItem adapt(String permissionValue) throws ExportationException
	{
		if(permissionValue == null)
		{
			return null;
		}

		final StringTokenizer tokens = new StringTokenizer(permissionValue, "|", false);

		// profile code is not used in circabc
		tokens.nextToken();
		final String date = tokens.nextToken().trim();
		final String message;
		if(tokens.hasMoreTokens())
		{
			message = tokens.nextToken().trim();
		}
		else
		{
			message = "";
		}

		try
		{
			return new ApplicationItem(getDate(date), message);
		}
		catch (ParseException e)
		{
			throw new ExportationException("Impossible to add an application for membersip due to a illegal date", e);
		}
	}

	/**
	 * @throws ParseException
	 */
	private final Date getDate(final String dateAsString) throws ParseException
	{
    	final String gmt;
    	String date;

    	if(dateAsString.contains("GMT"))
    	{
    		int lastSpace = dateAsString.lastIndexOf(' ');
    		gmt = dateAsString.substring(lastSpace + 1);
    		date = dateAsString.substring(0, lastSpace);
    	}
    	else
    	{
			// some dates doesn't contain the time zone, use the default one
    		gmt = TimeZone.getDefault().getID();
    		date = dateAsString;
    	}

    	if(date.indexOf(':') < 0)
		{
			// some dates doesn't contain the time
			date += " 00:00:00";
		}

    	final Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, gmt));
		DATE_FORMAT.get().setCalendar(cal);

		return DATE_FORMAT.get().parse(date);
	}

}