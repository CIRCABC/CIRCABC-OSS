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
 * Base interface of the statistics of a single category
 *
 * @author Yanick Pignot
 */
public interface CategoryStat extends Serializable
{
	/**
	 * @return the categoryAdmins
	 */
	public abstract int getCategoryAdmins();

	/**
	 * @return the categoryHeader
	 */
	public abstract String getCategoryHeader();

	/**
	 * @return the category name
	 */
	public abstract String getCategoryName();

	/**
	 * @return the categoryInterestGroups
	 */
	public abstract int getInterestGroups();

	/**
	 * @return the categoryInterestGroups stats
	 */
	public abstract List<InterestGroupStat> getInterestGroupStats();

	/**
	 * @return the hiddenCategoryHeader
	 */
	public abstract boolean isHiddenCategoryHeader();

	/**
	 * @return the totalDifferentUsers
	 */
	public abstract int getTotalDifferentUsers();
}