/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.etl;

import java.io.Serializable;

/**
 * @author Yanick Pignot
 *
 */
public class PathologicUserComparator extends ETLElementComparator<PathologicUser> implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = -280174741716054745L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final PathologicUser o1, final PathologicUser o2)
	{
		return super.comparePersonUid(o1.getPerson(), o2.getPerson());
	}

}
