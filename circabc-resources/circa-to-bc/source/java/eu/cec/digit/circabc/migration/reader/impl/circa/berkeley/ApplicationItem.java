/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.io.Serializable;
import java.util.Date;

/**
 * Wrap an application for membership item in the berkeley db
 *
 * @author Yanick Pignot
 */
public class ApplicationItem implements Serializable
{
	/** */
	private static final long serialVersionUID = -8558692209947074396L;
	final Date date;
	final String message;

	/**
	 * @param date
	 * @param message
	 */
	ApplicationItem(final Date date, final String message)
	{
		super();
		this.date = date;
		this.message = message;
	}

	/**
	 * @return the dateAsString
	 */
	public final Date getDate()
	{
		return date;
	}


	/**
	 * @return the message
	 */
	public final String getMessage()
	{
		return message;
	}
}