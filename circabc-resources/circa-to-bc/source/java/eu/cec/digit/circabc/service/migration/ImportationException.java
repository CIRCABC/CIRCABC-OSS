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
 * Excpetion thrown for each importation problem
 *
 * @author Yanick Pignot
 */
public class ImportationException extends Exception
{

	private static final Log logger = LogFactory.getLog(ImportationException.class);

	/** */
	private static final long serialVersionUID = -2902873101377817466L;

	/**
	 *
	 */
	public ImportationException()
	{
		super();
		log("Unexpected", null);
	}

	/**
	 * @param message
	 */
	public ImportationException(String message)
	{
		super(message);
		log(message, null);
	}

	/**
	 * @param cause
	 */
	public ImportationException(Throwable cause)
	{
		super(cause);
		log("Unexpected", cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ImportationException(String message, Throwable cause)
	{
		super(message, cause);

		log(message, cause);
	}

	@SuppressWarnings("deprecation")
	private void log(String message, Throwable cause)
	{
		logger.error("Error occurs during importation");
		logger.error("  --  with message: " + message);
		logger.error("  --  with cause:   " + ((cause == null) ? "null" : cause.getMessage()));

		if(logger.isDebugEnabled() && cause != null)
		{
			final LoggingOutputStream loggingOutputStream = new LoggingOutputStream(Category.getInstance(ImportationException.class), Priority.ERROR);
			cause.printStackTrace(new PrintStream(loggingOutputStream, true));
		}
	}
}
