/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

import static eu.cec.digit.circabc.migration.entities.ElementsHelper.getReferencedElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;

import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.properties.Availability;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventPriority;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventType;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.MeetingType;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimeZone;
import eu.cec.digit.circabc.repo.dynamic.property.DynamicPropertyImpl;
import eu.cec.digit.circabc.repo.event.EventImpl;
import eu.cec.digit.circabc.repo.event.MeetingImpl;
import eu.cec.digit.circabc.repo.keywords.KeywordImpl;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import eu.cec.digit.circabc.service.event.AudienceStatus;
import eu.cec.digit.circabc.service.event.MainOccurence;
import eu.cec.digit.circabc.service.event.OccurenceRate;
import eu.cec.digit.circabc.service.keyword.Keyword;

/**
 * Util class that perfrom some usefull transformation for some elements.
 *
 * Principaly use to hide dirty object to object convertion.
 *
 * @author Yanick Pignot
 */
public abstract class ElementsConverter
{

	private ElementsConverter(){};

	/**
	 * Helper that adpat a list of XML I8Nproperty in a repository MLText
	 *
	 * @param properties
	 * @return
	 */
	public static MLText adpatI18NProperties(final List<I18NProperty> properties)
	{
		final MLText mlText = new MLText();
		for (final I18NProperty property : properties)
		{
			mlText.put(property.getLang(), property.getValue());
		}

		return mlText;
	}

	/**
	 * Helper that adpat a list of XML I8Nproperty in a repository MLText
	 *
	 * @param properties
	 * @return
	 */
	public static List<I18NProperty> adpatMLText(final MLText mlText)
	{
		final List<I18NProperty> properties = new ArrayList<I18NProperty>(mlText.size());
		for (final Map.Entry<Locale, String> property : mlText.entrySet())
		{
			if(property.getValue() != null && property.getValue().length() > 0)
			{
				properties.add(new I18NProperty(property.getKey(), property.getValue()));
			}
		}

		return properties;
	}

	/**
	 * Convert an XML meeting to a repository meeting
	 *
	 * @see eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting
	 * @see eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting
	 *
	 * @param circabc				The mandatory circabc root element to retreive library section nodeid
	 * @param meeting
	 * @return
	 */
	public static eu.cec.digit.circabc.service.event.Meeting convertMeeting(final Circabc circabc, final Meeting meeting)
	{
		final eu.cec.digit.circabc.service.event.Meeting repoMeeting = new MeetingImpl();
		convertAppoitement(repoMeeting, meeting);

		if(meeting.getType() == null)
		{
			meeting.setType(MeetingType.FACE_TO_FACE);
		}
		repoMeeting.setMeetingType(
				eu.cec.digit.circabc.service.event.MeetingType.valueOf(meeting.getType().value())
			);

		if(meeting.getAvailability() == null)
		{
			meeting.setAvailability(Availability.PRIVATE);
		}
		repoMeeting.setAvailability(
				eu.cec.digit.circabc.service.event.MeetingAvailability.valueOf(meeting.getAvailability().value())
			);

		repoMeeting.setOrganization(safeValue(meeting.getOrganization()));
		repoMeeting.setAgenda(safeValue(meeting.getAgenda()));

		if(meeting.getLibrarySection() != null && meeting.getLibrarySection().length() > 0)
		{
			final Node tagetSpace = (Node) getReferencedElement(circabc, meeting.getLibrarySection());
			if(tagetSpace != null)
			{
				repoMeeting.setLibrarySection(tagetSpace.getNodeReference());
			}
		}

		return repoMeeting;
	}

	/**
	 * Convert an XML event to a repository event
	 *
	 * @see eu.cec.digit.circabc.migration.entities.generated.nodes.Event
	 * @see eu.cec.digit.circabc.migration.entities.generated.nodes.Event
	 *
	 * @param event
	 * @return
	 */
	public static eu.cec.digit.circabc.service.event.Event convertEvent(final Event event)
	{
		final eu.cec.digit.circabc.service.event.Event repoEvent = new EventImpl();

		convertAppoitement(repoEvent, event);

		if(event.getType() == null)
		{
			event.setType(EventType.TASK);
		}
		repoEvent.setEventType(
				eu.cec.digit.circabc.service.event.EventType.valueOf(event.getType().value())
			);

		if(event.getPriority() == null)
		{
			event.setPriority(EventPriority.LOW);
		}
		repoEvent.setPriority(
				eu.cec.digit.circabc.service.event.EventPriority.valueOf(event.getPriority().value())
			);

		return repoEvent;
	}

	/**
	 * Convert a migration typed Dynamic Property definition to a repository Dynamic Property pojo
	 *
	 * @param propertyDefinition
	 * @return
	 */
	public static DynamicProperty convertDynamicProperty(final DynamicPropertyDefinition propertyDefinition)
	{
		final DynamicPropertyType type = DynamicPropertyType.valueOf(propertyDefinition.getType().value());
		final List<I18NProperty> i18nValues = propertyDefinition.getI18NValues();
		final Integer index = propertyDefinition.getId();
		MLText labels = null;
		String selection = null;

		if(i18nValues != null && i18nValues.size() > 0)
		{
			labels = adpatI18NProperties(i18nValues);
		}
		else
		{
			labels = new MLText(propertyDefinition.getValue());
		}

		if(DynamicPropertyType.SELECTION.equals(type) && propertyDefinition.getSelectionCases() != null)
		{
			final StringBuffer buff = new StringBuffer("");
			for(final String select : propertyDefinition.getSelectionCases())
			{
				buff
					.append(select)
					.append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
			}

			selection = buff.toString();
		}

		final DynamicProperty property = new DynamicPropertyImpl(index.longValue(), labels, type, selection);
		return property;
	}

	/**
	 * Convert a migration typed keyword definition to a repository keyword pojo
	 *
	 * @param keywordDefinition
	 * @return
	 */
	public static Keyword convertKeywords(final KeywordDefinition keywordDefinition)
	{
		final List<I18NProperty> i18nValues = keywordDefinition.getI18NValues();
		MLText labels = null;

		if(i18nValues != null && i18nValues.size() > 0)
		{
			labels = adpatI18NProperties(i18nValues);
		}
		else
		{
			labels = new MLText(keywordDefinition.getValue());
		}

		final Keyword keyword = new KeywordImpl(labels);

		return keyword;
	}

	private static String safeValue(final Serializable value)
	{
		if(value == null)
		{
			return "";
		}
		else
		{
			return value.toString();
		}
	}

	private static void convertAppoitement(final eu.cec.digit.circabc.service.event.Appointment repoAppointment, final Appointment appointment)
	{
		repoAppointment.setEventAbstract(safeValue(appointment.getAbstract()));
		repoAppointment.setTitle(safeValue(appointment.getAppointmentTitle()));

		if(appointment.getLanguage() != null)
		{
			repoAppointment.setLanguage(appointment.getLanguage().getLanguage());
		}
		else
		{
			repoAppointment.setLanguage("en");
		}

		repoAppointment.setLocation(safeValue(appointment.getLocation()));


		repoAppointment.setDateAsDate(appointment.getDate());

		if(appointment.getTimeZoneId() == null)
		{
			appointment.withTimeZoneId(TimeZone.VALUE_14);
		}

		repoAppointment.setTimeZoneId(appointment.getTimeZoneId().value());

		if(appointment.getEveryTimesOccurence() != null )
		{
			repoAppointment.setOccurenceRate(
						new OccurenceRate(MainOccurence.EveryTimes,
								eu.cec.digit.circabc.service.event.EveryTimesOccurence.valueOf(appointment.getEveryTimesOccurence().getType().value()),
								appointment.getEveryTimesOccurence().getEvery(),
								appointment.getEveryTimesOccurence().getForTimes()
						));

		}
		else if(appointment.getTimesOccurence() != null)
		{
			repoAppointment.setOccurenceRate(
						new OccurenceRate(MainOccurence.Times,
								eu.cec.digit.circabc.service.event.TimesOccurence.valueOf(appointment.getTimesOccurence().getType().value()),
								appointment.getTimesOccurence().getForTimes()
							));
		}
		else
		{
			repoAppointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));
		}

		repoAppointment.setStartDateAsDate(appointment.getStartDate());
		repoAppointment.setStartTimeAsDate(appointment.getStartTime());
		repoAppointment.setEndTimeAsDate(appointment.getEndTime());

		if(appointment.getAudienceClosed() != null)
		{
			repoAppointment.setAudienceStatus(AudienceStatus.Closed);
			final List<String> invitedUsers = appointment.getAudienceClosed().getInvitedUsers();
			repoAppointment.setInvitedUsers(invitedUsers);
		}
		else
		{
			repoAppointment.setAudienceStatus(AudienceStatus.Open);
		}

		repoAppointment.setInvitationMessage("");
		repoAppointment.setName(safeValue(appointment.getContact().getName()));
		repoAppointment.setEmail(safeValue(appointment.getContact().getEmail()));
		repoAppointment.setPhone(safeValue(appointment.getContact().getPhone()));
		repoAppointment.setUrl(safeValue(appointment.getContact().getUrl()));
	}

}
