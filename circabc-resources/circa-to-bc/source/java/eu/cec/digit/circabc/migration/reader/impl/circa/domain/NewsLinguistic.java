/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

/**
 * Entity for circa persisted linguistic information of a News.
 *
 * @author Yanick Pignot
 */
public class NewsLinguistic
{
	private String title;
	private String language;
	private String abstractDesc;

	/**
	 * @return the abstractDesc
	 */
	public final String getAbstractDesc()
	{
		return abstractDesc;
	}
	/**
	 * @param abstractDesc the abstractDesc to set
	 */
	public final void setAbstractDesc(final String abstractDesc)
	{
		this.abstractDesc = abstractDesc;
	}
	/**
	 * @return the language
	 */
	public final String getLanguage()
	{
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public final void setLanguage(final String language)
	{
		this.language = language;
	}
	/**
	 * @return the title
	 */
	public final String getTitle()
	{
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public final void setTitle(final String title)
	{
		this.title = title;
	}
}
