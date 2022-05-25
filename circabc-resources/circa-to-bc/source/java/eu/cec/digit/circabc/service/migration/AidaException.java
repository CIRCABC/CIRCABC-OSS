/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import eu.cec.digit.circabc.migration.log4j.LoggingOutputStream;


/**
 * Excpetion thrown for each aida problem
 *
 * @author Yanick Pignot
 */
public class AidaException extends Exception
{

	private static final Log logger = LogFactory.getLog(AidaException.class);

	/** */
	private static final long serialVersionUID = -2986173101377817466L;

	/**
	 *
	 */
	public AidaException()
	{
		super();
		log("Unexpected", null);
	}

	/**
	 * @param message
	 */
	public AidaException(String message)
	{
		super(message);
		log(message, null);
	}

	/**
	 * @param cause
	 */
	public AidaException(Throwable cause)
	{
		super(cause);
		log("Unexpected", cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AidaException(String message, Throwable cause)
	{
		super(message, cause);
		log(message, cause);
	}

	@SuppressWarnings("deprecation")
	private void log(String message, Throwable cause)
	{
		logger.error("Error occurs during aida process");
		logger.error("  --  with message: " + message);
		logger.error("  --  with cause:   " + ((cause == null) ? "null" : cause.getMessage()));

		if(logger.isDebugEnabled() && cause != null)
		{
			final LoggingOutputStream loggingOutputStream = new LoggingOutputStream(Category.getInstance(AidaException.class), Priority.ERROR);
			cause.printStackTrace(new PrintStream(loggingOutputStream, true));
		}
	}

}
