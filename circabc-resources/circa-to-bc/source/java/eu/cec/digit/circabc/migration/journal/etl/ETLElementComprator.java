/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.etl;

import java.util.Comparator;

import eu.cec.digit.circabc.migration.entities.generated.user.Person;

/**
 * @author Yanick Pignot
 *
 */
abstract class ETLElementComparator<T> implements Comparator<T>
{
	final boolean ascending;

	public ETLElementComparator()
	{
		this(true);
	}

	public ETLElementComparator(final boolean ascending)
	{
		this.ascending = ascending;
	}

	protected int comparePersonUid(final Person o1, final Person o2)
	{
		final String uid1 = (String) o1.getUserId().getValue();
		final String uid2 = (String) o2.getUserId().getValue();

		if(ascending)
		{
			return uid1.compareTo(uid2);
		}
		else
		{
			return uid2.compareTo(uid1);
		}
	}

}
