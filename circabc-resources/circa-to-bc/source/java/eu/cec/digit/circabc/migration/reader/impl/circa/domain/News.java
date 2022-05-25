/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

import java.util.List;

/**
 * Entity for circa persisted News.
 *
 * @author Yanick Pignot
 */
public class News
{
	private String title;
	private String subject;
	private String created;
	private String modified;
	private String creator;
	private boolean remote;
	private String valid;
	private boolean moderated;
	private String availability;
	private String service;

	private List<NewsLinguistic> NewsLinguistics;

	/**
	 * @return the availability
	 */
	public final String getAvailability()
	{
		return availability;
	}

	/**
	 * @param availability the availability to set
	 */
	public final void setAvailability(final String availability)
	{
		this.availability = availability;
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
	 * @return the moderated
	 */
	public final boolean isModerated()
	{
		return moderated;
	}

	/**
	 * @param moderated the moderated to set
	 */
	public final void setModerated(final boolean moderated)
	{
		this.moderated = moderated;
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
	 * @return the newsLinguistics
	 */
	public final List<NewsLinguistic> getNewsLinguistics()
	{
		return NewsLinguistics;
	}

	/**
	 * @param newsLinguistics the newsLinguistics to set
	 */
	public final void setNewsLinguistics(final List<NewsLinguistic> newsLinguistics)
	{
		NewsLinguistics = newsLinguistics;
	}

	/**
	 * @return the remote
	 */
	public final boolean isRemote()
	{
		return remote;
	}

	/**
	 * @param remote the remote to set
	 */
	public final void setRemote(final boolean remote)
	{
		this.remote = remote;
	}

	/**
	 * @return the service
	 */
	public final String getService()
	{
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public final void setService(final String service)
	{
		this.service = service;
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
	 * @return the valid
	 */
	public final String getValid()
	{
		return valid;
	}

	/**
	 * @param valid the valid to set
	 */
	public final void setValid(final String valid)
	{
		this.valid = valid;
	}


}
