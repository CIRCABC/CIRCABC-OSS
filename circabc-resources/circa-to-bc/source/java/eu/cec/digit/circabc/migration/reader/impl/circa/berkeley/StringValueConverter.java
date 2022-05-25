/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

/**
 * The default converter implementation.
 *
 * @author Yanick Pignot
 */
public class StringValueConverter implements ValueConverter<String>
{
	/** */
	private static final long serialVersionUID = -2353066104780479228L;

	/**
	 * Return always a no null String.
	 **/
	public String adapt(final String value)
	{
		return value == null ? "" : value;
	}
}
