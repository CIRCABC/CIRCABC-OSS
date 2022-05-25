/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive;

/**
 * Thrown when an iteration process label violates the <b>unicity</b> constraint.
 *
 * @author Yanick Pignot
 */
public class DuplicateIterationNameException extends Exception
{
	/** */
	private static final long serialVersionUID = 2701945646056594069L;


	/**
	 * @param message
	 */
	public DuplicateIterationNameException(String name)
	{
		super("The iteration process '" + name + " is already used.");
	}



}
