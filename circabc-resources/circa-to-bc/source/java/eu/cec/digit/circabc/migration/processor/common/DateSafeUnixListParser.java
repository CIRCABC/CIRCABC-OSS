/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.common;

import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPListParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To correct few bugs, this class return a new Date() instand an exception if the parse fails.
 *
 * @see it.sauronsoftware.ftp4j.listparsers.UnixListParser
 *
 * @author Yanick Pignot
 */
public class DateSafeUnixListParser implements FTPListParser
{
	// Bug fix: allow the car " in the name
	private static final Pattern PATTERN = Pattern
		.compile("^([dl\\-])[r\\-][w\\-][xSs\\-][r\\-][w\\-][xSs\\-][r\\-][w\\-][xTt\\-]\\s+"
				+ "(?:\\d+\\s+)?\\S+\\s*\\S+\\s+(\\d+)\\s+(?:(\\w{3})\\s+(\\d{1,2}))\\s+"
				+ "(?:(\\d{4})|(?:(\\d{1,2}):(\\d{1,2})))\\s+"
				+ "([^\\\\*|]+)(?: -> ([^\\\\*\"|]+))?$");

	private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
		}
	};

	private static final boolean OPTIMISTIC_INSTANCIATE_BAD_LINES = true;

	public FTPFile[] parse(String[] lines) throws FTPListParseException {
	int size = lines.length;
	if (size == 0) {
		return new FTPFile[0];
	}
	// Removes the "total" line used in MAC style.
	if (lines[0].startsWith("total")) {
		size--;
		String[] lines2 = new String[size];
		for (int i = 0; i < size; i++) {
			lines2[i] = lines[i + 1];
		}
		lines = lines2;
	}
	// What's the date today?
	Calendar now = Calendar.getInstance();
	// Ok, starts parsing.
	int currentYear = now.get(Calendar.YEAR);
	FTPFile[] ret = new FTPFile[size];
	for (int i = 0; i < size; i++) {
		Matcher m = PATTERN.matcher(lines[i]);
		if (m.matches()) {
			ret[i] = new FTPFile();
			// Retrieve the data.
			String typeString = m.group(1);
			String sizeString = m.group(2);
			String monthString = m.group(3);
			String dayString = m.group(4);
			String yearString = m.group(5);
			String hourString = m.group(6);
			String minuteString = m.group(7);
			String nameString = m.group(8);
			String linkedString = m.group(9);
			// Parse the data.
			if (typeString.equals("-")) {
				ret[i].setType(FTPFile.TYPE_FILE);
			} else if (typeString.equals("d")) {
				ret[i].setType(FTPFile.TYPE_DIRECTORY);
			} else if (typeString.equals("l")) {
				ret[i].setType(FTPFile.TYPE_LINK);
				ret[i].setLink(linkedString);
			} else {
				throw new FTPListParseException();
			}
			long fileSize;
			try {
				fileSize = Long.parseLong(sizeString);
			} catch (Throwable t) {
				throw new FTPListParseException();
			}
			ret[i].setSize(fileSize);
			if (dayString.length() == 1) {
				dayString = "0" + dayString;
			}
			StringBuffer mdString = new StringBuffer();
			mdString.append(monthString);
			mdString.append(' ');
			mdString.append(dayString);
			mdString.append(' ');
			boolean checkYear = false;
			if (yearString == null) {
				mdString.append(currentYear);
				checkYear = true;
			} else {
				mdString.append(yearString);
				checkYear = false;
			}
			mdString.append(' ');
			if (hourString != null && minuteString != null) {
				if (hourString.length() == 1) {
					hourString = "0" + hourString;
				}
				if (minuteString.length() == 1) {
					minuteString = "0" + minuteString;
				}
				mdString.append(hourString);
				mdString.append(':');
				mdString.append(minuteString);
			} else {
				mdString.append("00:00");
			}
			Date md;
			try {
				md = DATE_FORMAT.get().parse(mdString.toString());
			} catch (Exception e) {
				// workaround: since we don't need the date, a wrong date doen'st distub us.
				// throw new FTPListParseException();
				md = new Date();
			}
			if (checkYear) {
				Calendar mc = Calendar.getInstance();
				mc.setTime(md);
				if (mc.after(now)) {
					mc.set(Calendar.YEAR, currentYear - 1);
					md = mc.getTime();
				}
			}
			ret[i].setModifiedDate(md);
			ret[i].setName(nameString);
		} else {

			if(OPTIMISTIC_INSTANCIATE_BAD_LINES)
			{
				ret[i] = new FTPFile();
				ret[i].setType(FTPFile.TYPE_DIRECTORY);
				ret[i].setSize(0);
				ret[i].setModifiedDate(new Date());
				ret[i].setName(lines[i]);
			}
			else
			{
				throw new FTPListParseException();
			}
		}
	}
	return ret;
	}
}
