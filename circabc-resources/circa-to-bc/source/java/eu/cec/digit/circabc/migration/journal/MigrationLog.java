/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.journal;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLElement;

/**
 * Single validation message instance
 *
 * @author Yanick Pignot
 */
public class MigrationLog implements Serializable
{
	/** */
	private static final long serialVersionUID = 6701349606729478875L;

	private static final String ELEMENT_LOG_PREFIX = "\n\tElement: ";
	private static final String VALUE_LOG_PREFIX = "\n\tValue: ";

	public enum LogLevel
	{
		INFO,
		FATAL,
		ERROR,
		WARNING,
		DEBUG
	}

	private LogLevel messageLevel;
	private String message;
	private List<Object> objectConcerned;
	private Date date = new Date();

	/**
	 * @param messageLevel
	 * @param message
	 * @param objectConcerned
	 */
	public MigrationLog(LogLevel messageLevel, String message, List<Object> objectConcerned)
	{
		super();
		this.messageLevel = messageLevel;
		this.message = message;
		this.objectConcerned = objectConcerned;
	}

	/**
	 * @param messageLevel
	 * @param message
	 * @param objectConcerned
	 */
	public MigrationLog(LogLevel messageLevel, String message, Object objectConcerned)
	{
		super();
		this.messageLevel = messageLevel;
		this.message = message;
		this.objectConcerned = Collections.<Object> singletonList(objectConcerned);
	}

	/**
	 * @return the message
	 */
	public final String getMessage()
	{
		return message;
	}

	/**
	 * @return the messageLevel
	 */
	public final LogLevel getMessageLevel()
	{
		return messageLevel;
	}

	/**
	 * @return the objectConcerned
	 */
	public final List<Object> getObjectConcerned()
	{
		return objectConcerned;
	}

	/**
	 * @return the date
	 */
	public final Date getDate()
	{
		return date;
	}


	@Override
	public String toString()
	{
		return toString(new SimpleDateFormat());
	}

	public String toString(final DateFormat dateFormat)
	{
		final StringBuffer buff = new StringBuffer(dateFormat.format(date));

		buff
			.append("  ")
			.append(messageLevel)
			.append("  :  ")
			.append(message);

		appendElementId(buff, objectConcerned);

		return buff.toString();
	}

	@SuppressWarnings("unchecked")
	private void appendElementId(final StringBuffer buff, final Object element)
	{
		if(element == null)
		{
			// nothing to add
		}
		else if(element instanceof Iterable)
		{
			for(final Object iteration : (Iterable<Object>) element)
			{
				appendElementId(buff, iteration);
			}
		}
		else if(element instanceof Object[])
		{
			for(final Object iteration : (Object[]) element)
			{
				appendElementId(buff, iteration);
			}
		}
		else if(element instanceof XMLElement)
		{
			final XMLElement xmlElement = ((XMLElement)element);
			final String path  = ElementsHelper.getXPath(xmlElement);

			buff.append(ELEMENT_LOG_PREFIX);
			buff.append((path != null) ? path : element.toString());
		}
		else
		{
			buff.append(VALUE_LOG_PREFIX);
			buff.append(element.toString());
		}
	}

}
