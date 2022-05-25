/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.Serializable;
import java.util.List;

/**
 * Base interface of the statistics of the root node
 *
 * @author Yanick Pignot
 */
public interface RootStat extends Serializable
{
	/**
	 * @return the exportationImplementationName
	 */
	public abstract String getExportationImplementationName();

	/**
	 * @return the categories
	 */
	public abstract int getCategories();

	/**
	 * @return the category headers
	 */
	public abstract int getCategoryHeaders();

	/**
	 * @return the category stats
	 */
	public abstract List<CategoryStat> getCategoryStats();

	/**
	 * @return the totalDifferentUsers
	 */
	public abstract int getTotalDifferentUsers();

}