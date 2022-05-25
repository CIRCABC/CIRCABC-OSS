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
 * Convert a berkeley value to a permission item.
 *
 * @author Yanick Pignot
 */
public class PermissionItemConverter implements ValueConverter<List<PermissionItem>>
{
	/** */
	private static final long serialVersionUID = 3695265020194727270L;

	private static final String SEPARATOR = "|";
	private final static String PERMISSION_ITEM_REGEX = "\\([a-zA-Z0-9@_\\-\\=:]+:[0-9A-F]+\\)";

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.ValueConverter#adapt(java.lang.String)
	 */
	public List<PermissionItem> adapt(String permissionValue) throws ExportationException
	{
		final List<PermissionItem> items = new ArrayList<PermissionItem>();

		if(permissionValue != null)
		{
			final StringTokenizer tokens = new StringTokenizer(permissionValue, SEPARATOR, false);
			while(tokens.hasMoreTokens())
			{
				final String token = tokens.nextToken().trim();

				if(token.matches(PERMISSION_ITEM_REGEX))
				{
					final int separtionPos = token.lastIndexOf(':');

					items.add(new PermissionItem(
							token.substring(1, separtionPos),
							token.substring(separtionPos + 1, token.length() - 1)));
				}

			}
		}
		return items;
	}

}