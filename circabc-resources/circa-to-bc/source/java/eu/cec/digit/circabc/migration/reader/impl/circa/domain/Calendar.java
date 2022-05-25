/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.domain;

import java.util.Date;
import java.util.List;

/**
 * Entity for circa persisted Calendar.
 *
 * @author Yanick Pignot
 */
public class Calendar
{
	private String id;
	private String calendarType;

	private int termsMedium;
	private int priority;
	private Date date;
	private String librarySection;
	private String repeatCode;
	private String multicode;
	private String multicounter;
	private String customRate;
	private String customPeriod;
	private String customDuration;
	private String stimeh;
	private String stimem;
	private String etimeh;
	private String etimem;
	private String tzone;
	private String status;
	private String audience;
	private String owner;
	private String phone;
	private String fax;
	private String email;
	private String ownerURL;
	private String internalUsers;
	private String externalUsers;
	private String acceptedUsers;
	private String deniedUsers;
	private String subject;

	private List<CalendarLinguistic> calendarLinguistics;

	/**
	 * @return the acceptedUsers
	 */
	public final String getAcceptedUsers()
	{
		return acceptedUsers;
	}

	/**
	 * @return the audience
	 */
	public final String getAudience()
	{
		return audience;
	}

	/**
	 * @return the calendarLinguistics
	 */
	public final List<CalendarLinguistic> getCalendarLinguistics()
	{
		return calendarLinguistics;
	}

	/**
	 * @return the calendarType
	 */
	public final String getCalendarType()
	{
		return calendarType;
	}

	/**
	 * @return the customDuration
	 */
	public final String getCustomDuration()
	{
		return customDuration;
	}

	/**
	 * @return the customPeriod
	 */
	public final String getCustomPeriod()
	{
		return customPeriod;
	}

	/**
	 * @return the customRate
	 */
	public final String getCustomRate()
	{
		return customRate;
	}

	/**
	 * @return the date
	 */
	public final Date getDate()
	{
		return date;
	}

	/**
	 * @return the deniedUsers
	 */
	public final String getDeniedUsers()
	{
		return deniedUsers;
	}

	/**
	 * @return the email
	 */
	public final String getEmail()
	{
		return email;
	}

	/**
	 * @return the etimeh
	 */
	public final String getEtimeh()
	{
		return etimeh;
	}

	/**
	 * @return the etimem
	 */
	public final String getEtimem()
	{
		return etimem;
	}

	/**
	 * @return the externalUsers
	 */
	public final String getExternalUsers()
	{
		return externalUsers;
	}

	/**
	 * @return the fax
	 */
	public final String getFax()
	{
		return fax;
	}

	/**
	 * @return the id
	 */
	public final String getId()
	{
		return id;
	}

	/**
	 * @return the internalUsers
	 */
	public final String getInternalUsers()
	{
		return internalUsers;
	}

	/**
	 * @return the librarySection
	 */
	public final String getLibrarySection()
	{
		return librarySection;
	}

	/**
	 * @return the multicode
	 */
	public final String getMulticode()
	{
		return multicode;
	}

	/**
	 * @return the multicounter
	 */
	public final String getMulticounter()
	{
		return multicounter;
	}

	/**
	 * @return the owner
	 */
	public final String getOwner()
	{
		return owner;
	}

	/**
	 * @return the ownerURL
	 */
	public final String getOwnerURL()
	{
		return ownerURL;
	}

	/**
	 * @return the phone
	 */
	public final String getPhone()
	{
		return phone;
	}

	/**
	 * @return the priority
	 */
	public final int getPriority()
	{
		return priority;
	}

	/**
	 * @return the repeatCode
	 */
	public final String getRepeatCode()
	{
		return repeatCode;
	}

	/**
	 * @return the status
	 */
	public final String getStatus()
	{
		return status;
	}

	/**
	 * @return the stimeh
	 */
	public final String getStimeh()
	{
		return stimeh;
	}

	/**
	 * @return the stimem
	 */
	public final String getStimem()
	{
		return stimem;
	}

	/**
	 * @return the subject
	 */
	public final String getSubject()
	{
		return subject;
	}

	/**
	 * @return the termsMedium
	 */
	public final int getTermsMedium()
	{
		return termsMedium;
	}

	/**
	 * @return the tzone
	 */
	public final String getTzone()
	{
		return tzone;
	}

	/**
	 * @param acceptedUsers the acceptedUsers to set
	 */
	public final void setAcceptedUsers(final String acceptedUsers)
	{
		this.acceptedUsers = acceptedUsers;
	}

	/**
	 * @param audience the audience to set
	 */
	public final void setAudience(final String audience)
	{
		this.audience = audience;
	}

	/**
	 * @param calendarLinguistics the calendarLinguistics to set
	 */
	public final void setCalendarLinguistics(final List<CalendarLinguistic> calendarLinguistics)
	{
		this.calendarLinguistics = calendarLinguistics;
	}

	/**
	 * @param calendarType the calendarType to set
	 */
	public final void setCalendarType(final String calendarType)
	{
		this.calendarType = calendarType;
	}

	/**
	 * @param customDuration the customDuration to set
	 */
	public final void setCustomDuration(final String customDuration)
	{
		this.customDuration = customDuration;
	}

	/**
	 * @param customPeriod the customPeriod to set
	 */
	public final void setCustomPeriod(final String customPeriod)
	{
		this.customPeriod = customPeriod;
	}

	/**
	 * @param customRate the customRate to set
	 */
	public final void setCustomRate(final String customRate)
	{
		this.customRate = customRate;
	}

	/**
	 * @param date the date to set
	 */
	public final void setDate(final Date date)
	{
		this.date = date;
	}

	/**
	 * @param deniedUsers the deniedUsers to set
	 */
	public final void setDeniedUsers(final String deniedUsers)
	{
		this.deniedUsers = deniedUsers;
	}

	/**
	 * @param email the email to set
	 */
	public final void setEmail(final String email)
	{
		this.email = email;
	}

	/**
	 * @param etimeh the etimeh to set
	 */
	public final void setEtimeh(final String etimeh)
	{
		this.etimeh = etimeh;
	}

	/**
	 * @param etimem the etimem to set
	 */
	public final void setEtimem(final String etimem)
	{
		this.etimem = etimem;
	}

	/**
	 * @param externalUsers the externalUsers to set
	 */
	public final void setExternalUsers(final String externalUsers)
	{
		this.externalUsers = externalUsers;
	}

	/**
	 * @param fax the fax to set
	 */
	public final void setFax(final String fax)
	{
		this.fax = fax;
	}

	/**
	 * @param id the id to set
	 */
	public final void setId(final String id)
	{
		this.id = id;
	}

	/**
	 * @param internalUsers the internalUsers to set
	 */
	public final void setInternalUsers(final String internalUsers)
	{
		this.internalUsers = internalUsers;
	}

	/**
	 * @param librarySection the librarySection to set
	 */
	public final void setLibrarySection(final String librarySection)
	{
		this.librarySection = librarySection;
	}

	/**
	 * @param multicode the multicode to set
	 */
	public final void setMulticode(final String multicode)
	{
		this.multicode = multicode;
	}

	/**
	 * @param multicounter the multicounter to set
	 */
	public final void setMulticounter(final String multicounter)
	{
		this.multicounter = multicounter;
	}

	/**
	 * @param owner the owner to set
	 */
	public final void setOwner(final String owner)
	{
		this.owner = owner;
	}

	/**
	 * @param ownerURL the ownerURL to set
	 */
	public final void setOwnerURL(final String ownerURL)
	{
		this.ownerURL = ownerURL;
	}

	/**
	 * @param phone the phone to set
	 */
	public final void setPhone(final String phone)
	{
		this.phone = phone;
	}

	/**
	 * @param priority the priority to set
	 */
	public final void setPriority(final int priority)
	{
		this.priority = priority;
	}

	/**
	 * @param repeatCode the repeatCode to set
	 */
	public final void setRepeatCode(final String repeatCode)
	{
		this.repeatCode = repeatCode;
	}

	/**
	 * @param status the status to set
	 */
	public final void setStatus(final String status)
	{
		this.status = status;
	}

	/**
	 * @param stimeh the stimeh to set
	 */
	public final void setStimeh(final String stimeh)
	{
		this.stimeh = stimeh;
	}

	/**
	 * @param stimem the stimem to set
	 */
	public final void setStimem(final String stimem)
	{
		this.stimem = stimem;
	}

	/**
	 * @param subject the subject to set
	 */
	public final void setSubject(final String subject)
	{
		this.subject = subject;
	}

	/**
	 * @param termsMedium the termsMedium to set
	 */
	public final void setTermsMedium(final int termsMedium)
	{
		this.termsMedium = termsMedium;
	}

	/**
	 * @param tzone the tzone to set
	 */
	public final void setTzone(final String tzone)
	{
		this.tzone = tzone;
	}

}
