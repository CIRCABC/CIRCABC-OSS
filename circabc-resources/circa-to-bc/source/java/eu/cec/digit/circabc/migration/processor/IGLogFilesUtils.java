/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import eu.cec.digit.circabc.migration.entities.generated.LogEntry;

/**
 * @author Yanick Pignot
 *
 */
public abstract class IGLogFilesUtils
{
	private static final String SEPARATOR = "|" ;
	private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		}
	};

	private IGLogFilesUtils(){}

	public static String toString(final LogEntry entry)
	{
		final StringBuilder builder = new StringBuilder()
			.append(DATE_FORMAT.get().format(entry.getDate()))
			.append(SEPARATOR)

			.append(entry.getUsername().replace("|", "-"))
			.append(SEPARATOR)

			.append(entry.getService())
			.append(SEPARATOR)

			.append(entry.getActivity())
			.append(SEPARATOR)

			.append(entry.isOk())
			.append(SEPARATOR)

			.append(entry.getPath().replace("|", "-"))
			.append(SEPARATOR)

			.append(entry.getInfo().replace("|", "-"))
			.append(SEPARATOR);

		return builder.toString();
	}

	public static LogEntry fromString(final String entryStr)
	{
		final LogEntry entry = new LogEntry();
		final StringTokenizer tokens = new StringTokenizer(entryStr, SEPARATOR, false);

		try
		{
			entry.setDate(DATE_FORMAT.get().parse(tokens.nextToken()));
		}
		catch (ParseException e)
		{
			entry.setDate(new Date());
		}
		entry.setUsername(tokens.nextToken());
		entry.setService(tokens.nextToken());
		entry.setActivity(tokens.nextToken());
		entry.setOk(Boolean.valueOf(tokens.nextToken()));
		entry.setPath(tokens.nextToken());

		if(tokens.hasMoreTokens())
		{
			entry.setInfo(tokens.nextToken());
		}

		return entry;
	}


}
