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
 * Excpetion thrown for each exportation problem
 *
 * @author Yanick Pignot
 */
public class ETLException extends Exception
{

	private static final Log logger = LogFactory.getLog(ETLException.class);

	/** */
	private static final long serialVersionUID = -2986173101377817466L;

	/**
	 *
	 */
	public ETLException()
	{
		super();
		log("Unexpected", null);
	}

	/**
	 * @param message
	 */
	public ETLException(String message)
	{
		super(message);
		log(message, null);
	}

	/**
	 * @param cause
	 */
	public ETLException(Throwable cause)
	{
		super(cause);
		log("Unexpected", cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ETLException(String message, Throwable cause)
	{
		super(message, cause);
		log(message, cause);
	}

	@SuppressWarnings("deprecation")
	private void log(String message, Throwable cause)
	{
		logger.error("Error occurs during exportation");
		logger.error("  --  with message: " + message);
		logger.error("  --  with cause:   " + ((cause == null) ? "null" : cause.getMessage()));

		if(logger.isDebugEnabled() && cause != null)
		{
			final LoggingOutputStream loggingOutputStream = new LoggingOutputStream(Category.getInstance(ETLException.class), Priority.ERROR);
			cause.printStackTrace(new PrintStream(loggingOutputStream, true));
		}
	}

}
