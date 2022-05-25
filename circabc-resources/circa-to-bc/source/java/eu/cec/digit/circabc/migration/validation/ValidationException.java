/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.validation;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import eu.cec.digit.circabc.migration.journal.MigrationLog;
import eu.cec.digit.circabc.migration.log4j.LoggingOutputStream;


/**
 * Exception resulting of an error of validation
 *
 * @author Yanick Pignot
 */
public class ValidationException extends Exception
{

	private static final Log logger = LogFactory.getLog(ValidationException.class);

	private List<MigrationLog> messages = null;

	/** */
	private static final long serialVersionUID = 6993201196922135427L;

	/**
	 *
	 */
	public ValidationException()
	{
		super();
	}

	/**
	 * @param A list of MigrationLog
	 */
	public ValidationException(List<MigrationLog> messages)
	{
		super(asString(messages));

		this.messages = messages;

		log(messages.size() + " validation problem found", null);

	}

	/**
	 * @param A MigrationLog
	 */
	public ValidationException(MigrationLog message)
	{
		this(Collections.<MigrationLog>singletonList(message));
	}


	/**
	 * @param message
	 * @param cause
	 */
	public ValidationException(String message, Throwable cause)
	{
		super(message, cause);

		log(message, cause);

	}

	/**
	 * @param message
	 */
	public ValidationException(String message)
	{
		super(message);
		log(message, null);
	}

	/**
	 * @param cause
	 */
	public ValidationException(Throwable cause)
	{
		super(cause);

		log("Unexpected", cause);
	}

	/**
	 * @return the messages
	 */
	public final List<MigrationLog> getMessages()
	{
		return messages;
	}

	private static String asString(List<MigrationLog> messages)
	{
		if(messages == null)
		{
			return "No Message Found";
		}
		else
		{
			final StringBuffer buff = new StringBuffer();

			for (MigrationLog message : messages)
			{
				buff
					.append('\n')
					.append(message.toString());

			}

			return buff.toString();
		}
	}

	@SuppressWarnings("deprecation")
	private void log(String message, Throwable cause)
	{
		logger.error("Error occurs during importation");
		logger.error("  --  with message: " + message);
		logger.error("  --  with cause:   " + ((cause == null) ? "null" : cause.getMessage()));

		if(logger.isDebugEnabled())
		{
			if(cause != null)
			{
				final LoggingOutputStream loggingOutputStream = new LoggingOutputStream(Category.getInstance(ValidationException.class), Priority.ERROR);
				cause.printStackTrace(new PrintStream(loggingOutputStream, true));
			}
			if(getMessages() != null)
			{
				for(final MigrationLog msg: getMessages())
				{
					switch(msg.getMessageLevel())
					{
						case FATAL:
							logger.fatal(msg.toString());
							break;
						case ERROR:
							logger.error(msg.toString());
							break;
						case WARNING:
							logger.warn(msg.toString());
							break;
					}
				}
			}
		}
	}

}
