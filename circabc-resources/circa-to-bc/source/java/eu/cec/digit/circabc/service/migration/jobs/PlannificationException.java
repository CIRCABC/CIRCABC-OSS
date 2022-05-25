/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration.jobs;

/**
 * Manage Operation Planification Expcetions
 *
 * @author Yanick Pignot
 */
public class PlannificationException extends Exception
{

	/** */
	private static final long serialVersionUID = 4437063788817864171L;

	/**
	 *
	 */
	public PlannificationException()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PlannificationException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public PlannificationException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PlannificationException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
