/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public abstract class TransactionHelper
{

	private TransactionHelper(){}


	/**
	 * @param <T>
	 * @param elements
	 * @param maxTransactionUnit
	 * @return
	 */
	public static <T> List<List<T>> dividListForTransactionUnits(List<T> elements, final int maxTransactionUnit)
	{
		final List<List<T>> units = new ArrayList<List<T>>();

		final int maxIndex = elements.size();
		int fromIndex = 0;
		int toIndex = (maxIndex < maxTransactionUnit) ? maxIndex : maxTransactionUnit;

		while(toIndex <= maxIndex && toIndex > fromIndex)
		{
			units.add(elements.subList(fromIndex, toIndex));

			fromIndex += maxTransactionUnit;
			toIndex = (toIndex + maxTransactionUnit < maxIndex) ? toIndex + maxTransactionUnit : maxIndex;
		}

		return units;
	}

}
