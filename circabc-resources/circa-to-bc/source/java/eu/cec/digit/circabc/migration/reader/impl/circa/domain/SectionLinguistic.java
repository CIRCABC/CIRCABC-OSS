/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

/**
 * Entity for circa persisted linguistic information of a Section.
 *
 * @author Yanick Pignot
 */
public class SectionLinguistic
{
	private String classification;
	private String identifier;
	private String language;
	private String parentURL;
	private String created;
	private String modified;
	private String title;
	private String subject;
	private String creator;
	private String extent;
	private String medium;
	private String abstractDesc;
	private String summary;

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
	 * @return the classification
	 */
	public final String getClassification()
	{
		return classification;
	}
	/**
	 * @param classification the classification to set
	 */
	public final void setClassification(final String classification)
	{
		this.classification = classification;
	}
	/**
	 * @return the created
	 */
	public final String getCreated()
	{
		return created;
	}
	/**
	 * @param created the created to set
	 */
	public final void setCreated(final String created)
	{
		this.created = created;
	}
	/**
	 * @return the creator
	 */
	public final String getCreator()
	{
		return creator;
	}
	/**
	 * @param creator the creator to set
	 */
	public final void setCreator(final String creator)
	{
		this.creator = creator;
	}
	/**
	 * @return the extent
	 */
	public final String getExtent()
	{
		return extent;
	}
	/**
	 * @param extent the extent to set
	 */
	public final void setExtent(final String extent)
	{
		this.extent = extent;
	}
	/**
	 * @return the identifier
	 */
	public final String getIdentifier()
	{
		return identifier;
	}
	/**
	 * @param identifier the identifier to set
	 */
	public final void setIdentifier(final String identifier)
	{
		this.identifier = identifier;
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
	 * @return the medium
	 */
	public final String getMedium()
	{
		return medium;
	}
	/**
	 * @param medium the medium to set
	 */
	public final void setMedium(final String medium)
	{
		this.medium = medium;
	}
	/**
	 * @return the modified
	 */
	public final String getModified()
	{
		return modified;
	}
	/**
	 * @param modified the modified to set
	 */
	public final void setModified(final String modified)
	{
		this.modified = modified;
	}
	/**
	 * @return the parentURL
	 */
	public final String getParentURL()
	{
		return parentURL;
	}
	/**
	 * @param parentURL the parentURL to set
	 */
	public final void setParentURL(final String parentURL)
	{
		this.parentURL = parentURL;
	}
	/**
	 * @return the subject
	 */
	public final String getSubject()
	{
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public final void setSubject(final String subject)
	{
		this.subject = subject;
	}
	/**
	 * @return the summary
	 */
	public final String getSummary()
	{
		return summary;
	}
	/**
	 * @param summary the summary to set
	 */
	public final void setSummary(final String summary)
	{
		this.summary = summary;
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
