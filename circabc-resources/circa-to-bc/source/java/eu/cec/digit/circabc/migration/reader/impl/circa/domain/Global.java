/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;


/**
 * Entity for circa persisted Global Document.
 *
 * @author Yanick Pignot
 */
public class Global
{
	private String identifier;
	private String title;
	private String alternative;
	private String language;
	private String classification;
	private String created;
	private String version;
	private String modified;
	private String usersPermissions;
	private String classPermissions;
	private String subject;
	private String abstractDesc;
	private String creator;
	private String owner;
	private String issued;
	private String expiration;

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
	 * @return the classPermissions
	 */
	public final String getClassPermissions()
	{
		return classPermissions;
	}
	/**
	 * @param classPermissions the classPermissions to set
	 */
	public final void setClassPermissions(final String classPermissions)
	{
		this.classPermissions = classPermissions;
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
	 * @return the usersPermissions
	 */
	public final String getUsersPermissions()
	{
		return usersPermissions;
	}
	/**
	 * @param usersPermissions the usersPermissions to set
	 */
	public final void setUsersPermissions(final String usersPermissions)
	{
		this.usersPermissions = usersPermissions;
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



}
