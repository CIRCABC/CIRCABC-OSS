/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

/**
 * An abstract base class for each element. Mainly used to have common additional property that will be added.
 *
 * @author Yanick Pignot
 */
public abstract class XMLElement implements Cloneable
{
	/**
	 * The xpath reference (ie: /Circabc/CategoryHeader[1]/Category[1]/InterestGroup[1]/Library[1]/Space[1]/MlContent[1])
	 */
	private String xpath;

	/**
	 *	The parent element of the current xml element.
	 **/
	private XMLElement parent;


	/**
	 * @return the xpath
	 */
	/* package*/  final String getXPath()
	{
		return xpath;
	}

	/**
	 * @param xpath the path to set
	 */
	/*package*/ final void setXPath(String xpath)
	{
		this.xpath = xpath;
	}

	/**
	 * @return the parent
	 */
	/*package*/  final XMLElement getParent()
	{
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	/*package*/  final void setParent(final XMLElement parent)
	{
		this.parent = parent;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(" + xpath + ")";
	}

	public Object clone()
	{
		XMLElement element = null;
	    try
		{
			element = (XMLElement) super.clone();
		}
	    catch (CloneNotSupportedException e)
		{
			throw new IllegalArgumentException(e);
		}
	    return element;
	}
}
