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
import eu.cec.digit.circabc.service.migration.RootStat;

/**
 * Concrete implementation of statistics of a single interest group
 *
 * @author Yanick Pignot
 */
public class RootStatImpl implements RootStat, Serializable
{
	/** */
	private static final long serialVersionUID = 849938911849562885L;

	private int categories = 0;
	private int headers = 0;
	private List<CategoryStat> categoryStats = new ArrayList<CategoryStat>();
	private final String exportationImplementationName;
	private int totalDifferentUsers;

	/**
	 * @param exportationImplementationName
	 */
	RootStatImpl(final String exportationImplementationName)
	{
		super();
		
		ParameterCheck.mandatory("Exportation Implementation Name", exportationImplementationName);
		
		this.exportationImplementationName = exportationImplementationName;
	}

	public int getCategories()
	{
		return categories;
	}

	public int getCategoryHeaders()
	{
		return headers;
	}

	public List<CategoryStat> getCategoryStats()
	{
		return categoryStats;
	}

	public String getExportationImplementationName()
	{
		return exportationImplementationName;
	}

	public final int getTotalDifferentUsers()
	{
		return totalDifferentUsers;
	}


	/*package*/ void addCategoryStat(final CategoryStat categoryStat)
	{
		categoryStats.add(categoryStat);
	}

	/*package*/ final void setCategories(int categories)
	{
		this.categories = categories;
	}

	/*package*/ final void setHeaders(int headers)
	{
		this.headers = headers;
	}

	/**
	 * @param totalDifferentUsers the totalDifferentUsers to set
	 */
	/*package*/  final void setTotalDifferentUsers(int totalDifferentUsers)
	{
		this.totalDifferentUsers = totalDifferentUsers;
	}
}