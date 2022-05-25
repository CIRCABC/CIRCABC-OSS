/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive;

/**
 * Exception resulting of an error of archiving
 *
 * @author Yanick Pignot
 */
public class ArchiveException extends Exception
{
	/** */
	private static final long serialVersionUID = 2706554646056991144L;

	/**
	 * @param message
	 * @param cause
	 */
	public ArchiveException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ArchiveException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ArchiveException(Throwable cause)
	{
		super(cause);
	}

}
