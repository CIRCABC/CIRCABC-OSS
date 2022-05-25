/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.Serializable;

/**
 * Simple object that keep an interest group with its category
 *
 * @author Yanick Pignot
 */
public class CategoryInterestGroupPair implements Serializable
{
	private static final String SEPARATOR = ":";

	/** */
	private static final long serialVersionUID = 6477595706451935959L;

	private String category;
	private String interestGroup;

	/**
	 * @param category
	 * @param interestGroup
	 */
	public CategoryInterestGroupPair(final String category, final String interestGroup)
	{
		super();
		this.category = category;
		this.interestGroup = interestGroup;
	}

	/**
	 * @return the category
	 */
	public final String getCategory()
	{
		return category;
	}
	/**
	 * @return the interestGroup
	 */
	public final String getInterestGroup()
	{
		return interestGroup;
	}

	@Override
	public String toString()
	{
		return category + SEPARATOR + interestGroup;
	}

	public static CategoryInterestGroupPair fromString(String str)
	{
		int separatorIdx = str.indexOf(SEPARATOR);

		return new CategoryInterestGroupPair(
					str.substring(0, separatorIdx),
					str.substring(separatorIdx + 1));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((category == null) ? 0 : category.hashCode());
		result = PRIME * result + ((interestGroup == null) ? 0 : interestGroup.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CategoryInterestGroupPair other = (CategoryInterestGroupPair) obj;
		if (category == null)
		{
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (interestGroup == null)
		{
			if (other.interestGroup != null)
				return false;
		} else if (!interestGroup.equals(other.interestGroup))
			return false;
		return true;
	}
}