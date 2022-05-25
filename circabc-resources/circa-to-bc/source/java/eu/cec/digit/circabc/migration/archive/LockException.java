/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive;

/**
 * Exception resulting of an error of locking
 *
 * @author Yanick Pignot
 */
public class LockException extends Exception
{
	/** */
	private static final long serialVersionUID = 2706554646056594069L;

	/**
	 * @param message
	 * @param cause
	 */
	public LockException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public LockException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public LockException(Throwable cause)
	{
		super(cause);
	}

}
