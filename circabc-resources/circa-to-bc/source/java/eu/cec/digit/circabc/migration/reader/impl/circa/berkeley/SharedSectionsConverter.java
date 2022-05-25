/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Convert a berkeley value to a list of short lib section path.
 *
 * @author Yanick Pignot
 */
public class SharedSectionsConverter implements ValueConverter<List<String>>
{
	/** */
	private static final long serialVersionUID = 7202081261871596197L;
	private final static String SEPARATOR = ":";

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.ValueConverter#adapt(java.lang.String)
	 */
	public List<String> adapt(String value) throws ExportationException
	{
		if(value == null)
		{
			return null;
		}

		final List<String> items = new ArrayList<String>();

		final StringTokenizer tokens = new StringTokenizer(value, SEPARATOR, false);
		while(tokens.hasMoreTokens())
		{
			items.add(tokens.nextToken().trim());
		}
		return items;
	}

}