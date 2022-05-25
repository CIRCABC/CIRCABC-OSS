/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.ParameterCheck;

import eu.cec.digit.circabc.service.migration.CategoryStat;
import eu.cec.digit.circabc.service.migration.InterestGroupStat;

/**
 * Concrete implementation of statistics of a single interest group
 *
 * @author Yanick Pignot
 */
public class CategoryStatImpl implements CategoryStat, Serializable
{
	/** */
	private static final long serialVersionUID = 7533577536849562885L;

	private int categoryAdmins = 0;
	private int interestGroups = 0;
	private final String categoryHeader;
	private final String categoryName;
	private final boolean hiddenCategoryHeader;
	private List<InterestGroupStat> interestGroupStats = new ArrayList<InterestGroupStat>();
	private int totalDifferentUsers = 0;

	/**
	 * @param categoryHeader
	 * @param categoryName
	 */
	CategoryStatImpl(final String categoryName, final String categoryHeader, final boolean hiddenCategoryHeader)
	{
		super();
		ParameterCheck.mandatory("Category Header", categoryHeader);
		ParameterCheck.mandatory("Category Name", categoryName);
		ParameterCheck.mandatory("Is Category hidden", hiddenCategoryHeader);
		
		this.categoryHeader = categoryHeader;
		this.categoryName = categoryName;
		this.hiddenCategoryHeader = hiddenCategoryHeader;
	}

	public int getCategoryAdmins()
	{
		return categoryAdmins;
	}

	public String getCategoryHeader()
	{
		return categoryHeader;
	}

	public String getCategoryName()
	{
		return categoryName;
	}

	public List<InterestGroupStat> getInterestGroupStats()
	{
		return interestGroupStats;
	}

	public int getInterestGroups()
	{
		return interestGroups;
	}

	public boolean isHiddenCategoryHeader()
	{
		return hiddenCategoryHeader;
	}

	public int getTotalDifferentUsers()
	{
		return totalDifferentUsers;
	}

	/**
	 * @param categoryAdmins the categoryAdmins to set
	 */
	/*package*/ final void setCategoryAdmins(int categoryAdmins)
	{
		this.categoryAdmins = categoryAdmins;
	}

	/**
	 * @param categoryAdmins the categoryAdmins to set
	 */
	/*package*/ final void addCategoryAdmin()
	{
		++this.categoryAdmins;
	}

	/**
	 * @param interestGroupStats the interestGroupStats to set
	 */
	/*package*/ final void addInterestGroupStats(final InterestGroupStat interestGroupStat)
	{
		this.interestGroupStats.add(interestGroupStat);
	}

	/**
	 * @param interestGroups the interestGroups to set
	 */
	/*package*/ void setInterestGroups(int interestGroups)
	{
		this.interestGroups = interestGroups;
	}

	/**
	 * @param totalDifferentUsers the totalDifferentUsers to set
	 */
	/*package*/  final void setTotalDifferentUsers(int totalDifferentUsers)
	{
		this.totalDifferentUsers = totalDifferentUsers;
	}




}