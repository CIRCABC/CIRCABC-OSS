/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

import java.util.List;

/**
 * Entity for circa persisted Section.
 *
 * @author Yanick Pignot
 */
public class Section
{
	private String classification;
	private String identifier;
	private String parentURL;
	private String created;
	private String expiration;
	private String modified;
	private String usersPerms;
	private String classPerms;
	private String creator;
	private String owner;
	private String originator;
	private String shareStatus;

	private List<SectionLinguistic> sectionLinguistics;

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
	 * @return the classPerms
	 */
	public final String getClassPerms()
	{
		return classPerms;
	}

	/**
	 * @param classPerms the classPerms to set
	 */
	public final void setClassPerms(final String classPerms)
	{
		this.classPerms = classPerms;
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
	 * @return the originator
	 */
	public final String getOriginator()
	{
		return originator;
	}

	/**
	 * @param originator the originator to set
	 */
	public final void setOriginator(final String originator)
	{
		this.originator = originator;
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
	 * @return the sectionLinguistics
	 */
	public final List<SectionLinguistic> getSectionLinguistics()
	{
		return sectionLinguistics;
	}

	/**
	 * @param sectionLinguistics the sectionLinguistics to set
	 */
	public final void setSectionLinguistics(final List<SectionLinguistic> sectionLinguistics)
	{
		this.sectionLinguistics = sectionLinguistics;
	}

	/**
	 * @return the shareStatus
	 */
	public final String getShareStatus()
	{
		return shareStatus;
	}

	/**
	 * @param shareStatus the shareStatus to set
	 */
	public final void setShareStatus(final String shareStatus)
	{
		this.shareStatus = shareStatus;
	}

	/**
	 * @return the usersPerms
	 */
	public final String getUsersPerms()
	{
		return usersPerms;
	}

	/**
	 * @param usersPerms the usersPerms to set
	 */
	public final void setUsersPerms(final String usersPerms)
	{
		this.usersPerms = usersPerms;
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
}
