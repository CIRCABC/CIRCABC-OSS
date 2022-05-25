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
public class TransformationElementComparator extends ETLElementComparator<TransformationElement> implements Serializable
{

	/**
	 *
	 */
	private static final long serialVersionUID = -1615622086012126359L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final TransformationElement o1, final TransformationElement o2)
	{
		return super.comparePersonUid(o1.getOriginalPerson(), o2.getOriginalPerson());
	}

}
