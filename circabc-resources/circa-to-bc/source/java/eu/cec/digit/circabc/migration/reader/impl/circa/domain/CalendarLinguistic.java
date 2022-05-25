/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

/**
 * Entity for circa persisted linguistic information of a Calendar.
 *
 * @author Yanick Pignot
 */
public class CalendarLinguistic
{
	private long id;
	private String language;
	private String title;
	private String abstractDesc;
	private String location;
	private String organizer;
	private String agenda;
	private String events;
	private String contact;

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
	 * @return the agenda
	 */
	public final String getAgenda()
	{
		return agenda;
	}
	/**
	 * @param agenda the agenda to set
	 */
	public final void setAgenda(final String agenda)
	{
		this.agenda = agenda;
	}
	/**
	 * @return the contact
	 */
	public final String getContact()
	{
		return contact;
	}
	/**
	 * @param contact the contact to set
	 */
	public final void setContact(final String contact)
	{
		this.contact = contact;
	}
	/**
	 * @return the events
	 */
	public final String getEvents()
	{
		return events;
	}
	/**
	 * @param events the events to set
	 */
	public final void setEvents(final String events)
	{
		this.events = events;
	}
	/**
	 * @return the id
	 */
	public final long getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public final void setId(final long id)
	{
		this.id = id;
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
	 * @return the location
	 */
	public final String getLocation()
	{
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public final void setLocation(final String location)
	{
		this.location = location;
	}
	/**
	 * @return the organizer
	 */
	public final String getOrganizer()
	{
		return organizer;
	}
	/**
	 * @param organizer the organizer to set
	 */
	public final void setOrganizer(final String organizer)
	{
		this.organizer = organizer;
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
