/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

/**
 * Entity for circa persisted Document.
 *
 * @author Yanick Pignot
 */
public class Document
{
	private String identifier;
	private String title;
	private String alternative;
	private String language;
	private String classification;
	private String version;
	private String subject;
	private String creator;
	private String created;
	private String expiration;
	private String owner;
	private String modified;
	private String abstractDesc;
	private String issued;
	private Integer availability;
	private String reference;
	private String status;
	private String dynAttribute1;
	private String dynAttribute2;
	private String dynAttribute3;
	private String dynAttribute4;
	private String dynAttribute5;

	private String docPool;
	private String parentUrl;

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
	 * @return the alternative
	 */
	public final String getAlternative()
	{
		return alternative;
	}
	/**
	 * @param alternative the alternative to set
	 */
	public final void setAlternative(final String alternative)
	{
		this.alternative = alternative;
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
	 * @return the docPool
	 */
	public final String getDocPool()
	{
		return docPool;
	}
	/**
	 * @param docPool the docPool to set
	 */
	public final void setDocPool(final String docPool)
	{
		this.docPool = docPool;
	}
	/**
	 * @return the dynAttribute1
	 */
	public final String getDynAttribute1()
	{
		return dynAttribute1;
	}
	/**
	 * @param dynAttribute1 the dynAttribute1 to set
	 */
	public final void setDynAttribute1(final String dynAttribute1)
	{
		this.dynAttribute1 = dynAttribute1;
	}
	/**
	 * @return the dynAttribute2
	 */
	public final String getDynAttribute2()
	{
		return dynAttribute2;
	}
	/**
	 * @param dynAttribute2 the dynAttribute2 to set
	 */
	public final void setDynAttribute2(final String dynAttribute2)
	{
		this.dynAttribute2 = dynAttribute2;
	}
	/**
	 * @return the dynAttribute3
	 */
	public final String getDynAttribute3()
	{
		return dynAttribute3;
	}
	/**
	 * @param dynAttribute3 the dynAttribute3 to set
	 */
	public final void setDynAttribute3(final String dynAttribute3)
	{
		this.dynAttribute3 = dynAttribute3;
	}
	/**
	 * @return the dynAttribute4
	 */
	public final String getDynAttribute4()
	{
		return dynAttribute4;
	}
	/**
	 * @param dynAttribute4 the dynAttribute4 to set
	 */
	public final void setDynAttribute4(final String dynAttribute4)
	{
		this.dynAttribute4 = dynAttribute4;
	}
	/**
	 * @return the dynAttribute5
	 */
	public final String getDynAttribute5()
	{
		return dynAttribute5;
	}
	/**
	 * @param dynAttribute5 the dynAttribute5 to set
	 */
	public final void setDynAttribute5(final String dynAttribute5)
	{
		this.dynAttribute5 = dynAttribute5;
	}
	/**
	 * @return the expiration
	 */
	public final String getExpiration()
	{
		return expiration;
	}
	/**
	 * @param expiration the expiration to set
	 */
	public final void setExpiration(final String expiration)
	{
		this.expiration = expiration;
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
	 * @return the issued
	 */
	public final String getIssued()
	{
		return issued;
	}
	/**
	 * @param issued the issued to set
	 */
	public final void setIssued(final String issued)
	{
		this.issued = issued;
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
	 * @return the owner
	 */
	public final String getOwner()
	{
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public final void setOwner(final String owner)
	{
		this.owner = owner;
	}
	/**
	 * @return the parentUrl
	 */
	public final String getParentUrl()
	{
		return parentUrl;
	}
	/**
	 * @param parentUrl the parentUrl to set
	 */
	public final void setParentUrl(final String parentUrl)
	{
		this.parentUrl = parentUrl;
	}
	/**
	 * @return the reference
	 */
	public final String getReference()
	{
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public final void setReference(final String reference)
	{
		this.reference = reference;
	}
	/**
	 * @return the status
	 */
	public final String getStatus()
	{
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public final void setStatus(final String status)
	{
		this.status = status;
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
	/**
	 * @return the version
	 */
	public final String getVersion()
	{
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public final void setVersion(final String version)
	{
		this.version = version;
	}
	/**
	 * @return the availability
	 */
	public final Integer getAvailability()
	{
		return availability;
	}
	/**
	 * @param availability the availability to set
	 */
	public final void setAvailability(Integer availability)
	{
		this.availability = availability;
	}
}
