/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;

/**
 * Sort two iteration according the iteration start date
 *
 * @author Yanick Pignot
 */
final class IterationComparator implements Comparator<MigrationIteration>   ,Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 8521981788520033554L;

	final boolean ascending;

	IterationComparator()
	{
		this(true);
	}

	IterationComparator(final boolean ascending)
	{
		this.ascending = ascending;
	}

	public int compare(final MigrationIteration o1, final MigrationIteration o2)
	{
		final Date date1 = o1.getIterationStartDate();
		final Date date2 = o2.getIterationStartDate();

		if(ascending)
		{
			return date1.compareTo(date2);
		}
		else
		{
			return date2.compareTo(date1);
		}
	}
}