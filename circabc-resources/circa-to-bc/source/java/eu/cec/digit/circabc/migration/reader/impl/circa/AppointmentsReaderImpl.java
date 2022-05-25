/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import static eu.cec.digit.circabc.migration.reader.impl.circa.util.AppointementCorrespondance.AUDIENCE;
import static eu.cec.digit.circabc.migration.reader.impl.circa.util.AppointementCorrespondance.CUSTOM_MULTICODE;
import static eu.cec.digit.circabc.migration.reader.impl.circa.util.AppointementCorrespondance.CUSTOM_PERIOD;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceClosed;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceOpen;
import eu.cec.digit.circabc.migration.entities.generated.properties.ContactInformation;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.MeetingRequestStatus;
import eu.cec.digit.circabc.migration.entities.generated.properties.SingleDate;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimeZone;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.dao.CircaDaoFactory;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Calendar;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.CalendarLinguistic;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.util.AppointementCorrespondance;
import eu.cec.digit.circabc.migration.reader.impl.circa.util.ParsedPath;
import eu.cec.digit.circabc.migration.validation.impl.NodeReferencesValidator;
import eu.cec.digit.circabc.service.event.AudienceStatus;
import eu.cec.digit.circabc.service.event.MainOccurence;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Build an appointments list from circa database.
 *
 * @author Yanick Pignot
 */
public class AppointmentsReaderImpl implements CalendarReader
{
	private static final Log logger = LogFactory.getLog(AppointmentsReaderImpl.class);

	private static final String USER_SEPARATOR = ",";
	private static final String MEETING_TYPE = "M";
	private static final String ID_SEPARTOR = "Q";

	private CircaDaoFactory daoFactory;
	private FileClient fileClient;
	private UserReader userReader;
	private String circaDomainPrefix;

	private static final String LONG_NAME_REGEX = ".+\\(.+@.+\\)";
	private static final String CIRCA_NAME_REGEX = "[^\\(]+@[^\\)]+";

	public List<Appointment> getAllAppointments(final Events events) throws ExportationException
	{
		final InterestGroup igRoot = ElementsHelper.getElementInterestGroup(events);
		final Category category = ElementsHelper.getElementCategory(igRoot);
		final String virtualCirca = (String) category.getName().getValue();
		final String interestGroup = (String)igRoot.getName().getValue();

		try
		{
			final List<Appointment> appointmentsXml = new ArrayList<Appointment>();
			final List<Calendar> appointmentsDb = daoFactory.getCalendarDao().getAllCalendars(virtualCirca, interestGroup);

			/* Group appointments from the same recurrence */
			final Map<String, List<Calendar>> groupedAppointments = new HashMap<String, List<Calendar>>();

			for(final Calendar calendar: appointmentsDb)
			{
				final String shortId = extractIDCommonPart(calendar.getId());
				List<Calendar> occurences =  groupedAppointments.get(shortId);
				if(occurences == null)
				{
					occurences = new ArrayList<Calendar>();
				}
				occurences.add(calendar);
				groupedAppointments.put(shortId, occurences);
			}

			for(final Map.Entry<String, List<Calendar>> entry: groupedAppointments.entrySet())
			{
				final List<Calendar> calendars = entry.getValue();
				final Calendar firstOccurence = findFirstOccurence(calendars);
				final CalendarLinguistic calendarLinguistic = getMostRelevantLingusiticData(firstOccurence);

				boolean isMeeting = isMeeting(firstOccurence);

				final Appointment appointment;


				if(isMeeting)
				{
					appointment = new Meeting();
				}
				else
				{
					appointment = new Event();
				}

				final Map<String, String> convertedUsers = fillCommonProperties(appointment, firstOccurence, calendarLinguistic );

				if(isMeeting)
				{
					fillSpecificProperties((Meeting) appointment, calendars, firstOccurence, calendarLinguistic, events, convertedUsers);

					if(logger.isDebugEnabled())
					{
						logger.debug("New meeting found:");
					}

				}
				else
				{
					fillSpecificProperties((Event)appointment,firstOccurence);

					if(logger.isDebugEnabled())
					{
						logger.debug("New event found:");
					}
				}

				if(logger.isDebugEnabled())
				{
					logger.debug("  -  with title:     " + appointment.getAppointmentTitle());
					logger.debug("  -  with audience:  " + ((appointment.getAudienceClosed() != null) ? "Closed" : "Open"));
					logger.debug("  -  with recurence: " + ((appointment.getSingleDate() != null) ? "NONE" : ((appointment.getTimesOccurence() != null) ? "Time occurence" : "Every time occurence")));
				}

				appointmentsXml.add(appointment);
			}

			return appointmentsXml;

		}
		catch (Exception e)
		{
			if(logger.isErrorEnabled()) {
        		logger.error("An Error occur", e); 
			}
			throw new ExportationException("Impossible to acces to the circa database to get the appointments of the interest group: " + virtualCirca + ":" + interestGroup, e);
		}
	}

	private Calendar findFirstOccurence(final List<Calendar> calendars)
	{
		Calendar first = null;

		for(final Calendar calendar: calendars)
		{
			if(first == null)
			{
				first = calendar;
			}
			else if(first.getDate().after(calendar.getDate()))
			{
				first = calendar;
			}
		}

		return first;
	}

	/**
	 * in circa, all occurences of a same appoitment can be found according the first part of the id.
	 *
	 *  An id seems: {commonPart}Q{specificPart}
	 */
	private String extractIDCommonPart(final String id)
	{
		final int separatorIdx = id.indexOf(ID_SEPARTOR);

		return id.substring(0, separatorIdx);
	}

	private boolean isMeeting(final Calendar calendar)
	{
		return calendar.getCalendarType().equals(MEETING_TYPE);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> fillCommonProperties(final Appointment appointment, final Calendar firstCalendar, final CalendarLinguistic calendarLinguistic)
	{
		final Map<String, String> convertedUsers = new HashMap<String, String>();

		appointment.setAppointmentTitle(calendarLinguistic.getTitle());
		appointment.setLanguage(I18NUtil.parseLocale(calendarLinguistic.getLanguage()));
		appointment.setAbstract(calendarLinguistic.getAbstractDesc());

		appointment.setDate(new Date());
		appointment.setStartDate(firstCalendar.getDate());
		appointment.setStartTime(getDateFromTime(safeInt(firstCalendar.getStimeh()), safeInt(firstCalendar.getStimem())));
		appointment.setEndTime(getDateFromTime(safeInt(firstCalendar.getEtimeh()), safeInt(firstCalendar.getEtimem())));

		final MainOccurence mainOccurence = AppointementCorrespondance.REPEAT_CODE.get(firstCalendar.getRepeatCode());

		if(mainOccurence != null)
		{
			switch (mainOccurence)
			{
				case EveryTimes:
					appointment.setEveryTimesOccurence(
							new EveryTimesOccurence(
									safeInt(firstCalendar.getCustomRate()),
									CUSTOM_PERIOD.get(firstCalendar.getCustomPeriod()),
									safeInt(firstCalendar.getCustomDuration())
							)
						);
					break;

				case Times:
					appointment.setTimesOccurence(
							new TimesOccurence(
									CUSTOM_MULTICODE.get(firstCalendar.getMulticode()),
									safeInt(firstCalendar.getMulticounter())
							)
						);
					break;

				default:
					appointment.setSingleDate(new SingleDate());
				break;
			}
		}
		else
		{
			logger.warn("Impossible to get Main occurence details of " + appointment.getClass().getSimpleName() + " (" + appointment.getAppointmentTitle() + "): '" + "' due to an invalid DB value for RepeatCode '" + firstCalendar.getRepeatCode() + "'. Single date will be used!");

			appointment.setSingleDate(new SingleDate());
		}

		final String tzone = firstCalendar.getTzone();
		try
		{
			appointment.setTimeZoneId(TimeZone.fromValue(tzone));
		}
		catch(IllegalArgumentException ex)
		{
			logger.warn("Impossible to get Timezone of " + appointment.getClass().getSimpleName() + " (" + appointment.getAppointmentTitle() + "): '" + "' due to an invalid DB value for TZone '" + tzone + "'. " + TimeZone.VALUE_14 + " will be used!");

			appointment.setTimeZoneId(TimeZone.VALUE_14);
		}
		appointment.setLocation(calendarLinguistic.getLocation());

		final String email = firstCalendar.getEmail();
		appointment.setContact(new ContactInformation(
				calendarLinguistic.getContact(),
				email,
				firstCalendar.getPhone(),
				firstCalendar.getOwnerURL()
			));

		final AudienceStatus audienceStatus = AUDIENCE.get(firstCalendar.getAudience());
		
		StringTokenizer tokens = new StringTokenizer(firstCalendar.getInternalUsers(), USER_SEPARATOR, false);
		final boolean isClosed = AudienceStatus.Closed.equals(audienceStatus);
		
		if(isClosed || tokens.hasMoreTokens())
		{
			if(!isClosed)
			{
				logger.warn("Audience status of " 
						+ appointment.getClass().getSimpleName() + " (" + appointment.getAppointmentTitle() 
						+ "): is setted being " + audienceStatus.toString() + " but will be closed since invited users found in the database!");
			}
			
			final AudienceClosed closed = new AudienceClosed();
			
			String userId = null;
			Person person = null;
			String personEmail = null;
			while(tokens.hasMoreTokens())
			{
				userId = retrieveUserId(tokens.nextToken());
				try
				{
					person = this.userReader.getPerson(userId);
					personEmail = (person == null || person.getEmail() == null) ? null : (String) person.getEmail().getValue();
				}
				catch (ExportationException ignore){}

				if(personEmail != null && personEmail.length() > 0)
				{
					convertedUsers.put(userId, personEmail);
				}
				else
				{
					convertedUsers.put(userId, LdapHelper.removeDomainFromUid(userId));
				}
				closed.withInvitedUsers(convertedUsers.get(userId));
			}

			tokens = new StringTokenizer(firstCalendar.getExternalUsers(), USER_SEPARATOR, false);
			while(tokens.hasMoreTokens())
			{
				userId = retrieveUserId(tokens.nextToken());
				closed.withInvitedUsers(userId);
			}

			// manage a rare bd corruption!
			if(closed.getInvitedUsers().size() == 0)
			{
				// if the email of the oganisator is not empty, he will become the unique invited of the appointment!
				if(email != null && email.length() > 0)
				{
					closed.withInvitedUsers(email);
					appointment.setAudienceClosed(closed);
				}
				else
				{
					// No email found, don't keep the appoitment closed (without user, it will result a sax parser excpetion)
					appointment.setAudienceOpen(new AudienceOpen());
				}
			}
			else
			{
				appointment.setAudienceClosed(closed);
			}
		}
		else
		{
			appointment.setAudienceOpen(new AudienceOpen());
		}

		return convertedUsers;
	}

	private CalendarLinguistic getMostRelevantLingusiticData(final Calendar calendar)
	{
		final Map<String, CalendarLinguistic> linguistics = new HashMap<String, CalendarLinguistic>();

		for(final CalendarLinguistic cl: calendar.getCalendarLinguistics())
		{
			linguistics.put(cl.getLanguage(), cl);
		}

		// prefer the Official pivot languages
		if(linguistics.containsKey("EN"))
		{
			return linguistics.get("EN");
		}
		else if(linguistics.containsKey("FR"))
		{
			return linguistics.get("FR");
		}
		else if(linguistics.containsKey("DE"))
		{
			return linguistics.get("DE");
		}
		else
		{
			return linguistics.values().iterator().next();
		}
	}

	private void fillSpecificProperties(final Event event, final Calendar firstCalendar)
	{
		event.setType(AppointementCorrespondance.EVENT_TYPE.get(String.valueOf(firstCalendar.getTermsMedium())));
		event.setPriority(AppointementCorrespondance.EVENT_PRIORITY.get(String.valueOf(firstCalendar.getPriority())));
	}

	private void fillSpecificProperties(final Meeting meeting, final List<Calendar> calendars, final Calendar firstCalendar, final CalendarLinguistic calendarLinguistic, final Events events, final Map<String, String> convertedUserId) throws ExportationException
	{
		meeting.setType(AppointementCorrespondance.MEETING_TYPE.get(String.valueOf(firstCalendar.getTermsMedium())));
		meeting.setAvailability(AppointementCorrespondance.AVAILABILITY.get(firstCalendar.getStatus()));
		meeting.setOrganization(calendarLinguistic.getOrganizer());
		meeting.setAgenda(calendarLinguistic.getAgenda());

		final String librarySection = meeting.getLibrarySection();
		if(librarySection != null && librarySection.trim().length() > 0)
		{
			final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(events), fileClient, circaDomainPrefix);
	    	final StringBuilder builder = new StringBuilder(NodeReferencesValidator.CIRCABC_REFERENCE);
	    	builder
				.append(FileClient.PATH_SEPARATOR)
				.append(parsedPath.getVirtualCirca())
				.append(FileClient.PATH_SEPARATOR)
				.append(parsedPath.getInterestGroup())
				.append(FileClient.PATH_SEPARATOR)
				.append(Library.class.getSimpleName())
				.append(librarySection);

	    	meeting.setLibrarySection(builder.toString());
		}

		for(final Calendar calendar: calendars)
		{
			final Date date = calendar.getDate();
			fillRequestStatus(calendar.getAcceptedUsers(), true, meeting, date, convertedUserId);
			fillRequestStatus(calendar.getDeniedUsers(), false, meeting, date, convertedUserId);
		}
	}

	/**
	 * @param calendar
	 * @param meeting
	 * @param date
	 * @param convertedUserId
	 */
	private void fillRequestStatus(final String usersString, final boolean accpeted, final Meeting meeting, final Date date, final Map<String, String> convertedUserId)
	{
		StringTokenizer tokens = new StringTokenizer(usersString, USER_SEPARATOR, false);
		String userId;
		String toUseId;

		if(tokens.hasMoreTokens())
		{
			if (meeting.getAudienceClosed() != null )
			{
				final List<String> invitedUsers = meeting.getAudienceClosed().getInvitedUsers();
				while(tokens.hasMoreTokens())
				{
					userId = retrieveUserId(tokens.nextToken());
					userId = LdapHelper.removeDomainFromUid(userId);
					toUseId = convertedUserId.containsKey(userId) ? convertedUserId.get(userId): userId;
	
					if(invitedUsers.contains(toUseId) == false)
					{
						invitedUsers.add(toUseId);
					}
	
					meeting.withMeetingRequestStatuses(new MeetingRequestStatus(toUseId, date, accpeted));
				}
			}
		}



	}

	private Date getDateFromTime(final int hour, final int min)
	{
		final java.util.Calendar cal = GregorianCalendar.getInstance();
		cal.set(0, 0, 0, hour, min, 0);

		return cal.getTime();
	}

	private Integer safeInt(final String string)
	{
		try
		{
			return Integer.valueOf(string);
		}
		catch (final NumberFormatException e)
		{
			return Integer.valueOf(1);
		}
	}

	private String retrieveUserId(String string)
	{
		if(string.matches(LONG_NAME_REGEX))
		{
			int startIdx = string.lastIndexOf('(') + 1;
			int endIdx = string.lastIndexOf(')');

			return string.substring(startIdx, endIdx);
		}
		else if(string.matches(CIRCA_NAME_REGEX))
		{
			return string;
		}
		else
		{
			return string + "@circa";
		}

	}

	/**
	 * @param daoFactory the daoFactory to set
	 */
	public final void setDaoFactory(final CircaDaoFactory daoFactory)
	{
		this.daoFactory = daoFactory;
	}

	public final void setCircaDomainPrefix(String circaDomainPrefix)
	{
		this.circaDomainPrefix = circaDomainPrefix;
	}

	public final void setFileClient(FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	/**
	 * @param userReader the userReader to set
	 */
	public final void setUserReader(UserReader userReader)
	{
		this.userReader = userReader;
	}


}
