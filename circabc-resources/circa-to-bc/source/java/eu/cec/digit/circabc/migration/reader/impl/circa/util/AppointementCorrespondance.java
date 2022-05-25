/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.util;

import java.util.HashMap;
import java.util.Map;

import eu.cec.digit.circabc.migration.entities.generated.properties.Availability;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventPriority;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventType;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurenceType;
import eu.cec.digit.circabc.migration.entities.generated.properties.MeetingType;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurenceType;
import eu.cec.digit.circabc.service.event.AudienceStatus;
import eu.cec.digit.circabc.service.event.MainOccurence;

/**
 * @author Yanick Pignot
 */
public abstract class AppointementCorrespondance
{
	private AppointementCorrespondance(){}

	// repeatcode
	public static final  Map<String, MainOccurence> REPEAT_CODE = new HashMap<String, MainOccurence>(3);
	static {
		REPEAT_CODE.put("CUSTOM", MainOccurence.EveryTimes);
		REPEAT_CODE.put("ONCE", MainOccurence.OnlyOnce);
		REPEAT_CODE.put("MULTIPLE", MainOccurence.Times);
	}

	// on multicode
	public static final  Map<String, TimesOccurenceType> CUSTOM_MULTICODE = new HashMap<String, TimesOccurenceType>(3);
	static {
		CUSTOM_MULTICODE.put("1", TimesOccurenceType.DAILY);
		CUSTOM_MULTICODE.put("2", TimesOccurenceType.WEEKLY);
		CUSTOM_MULTICODE.put("3", TimesOccurenceType.EVERY_TWO_WEEKS);
		CUSTOM_MULTICODE.put("4", TimesOccurenceType.MONTHLY_BY_DATE);
		CUSTOM_MULTICODE.put("5", TimesOccurenceType.MONTHLY_BY_WEEKDAY);
		CUSTOM_MULTICODE.put("6", TimesOccurenceType.YEARLY);
		CUSTOM_MULTICODE.put("7", TimesOccurenceType.MONDAY_TO_FRIDAY);
		CUSTOM_MULTICODE.put("8", TimesOccurenceType.MONDAY_WEDNSEY_FRIDAY);
		CUSTOM_MULTICODE.put("9", TimesOccurenceType.TUESDAY_THURSDAY);
	}

	// on custom period
	public static final  Map<String, EveryTimesOccurenceType> CUSTOM_PERIOD = new HashMap<String, EveryTimesOccurenceType>(3);
	static {
		CUSTOM_PERIOD.put("1", EveryTimesOccurenceType.DAYS);
		CUSTOM_PERIOD.put("2", EveryTimesOccurenceType.WEEKS);
		CUSTOM_PERIOD.put("3", EveryTimesOccurenceType.MONTHS);
	}

	// on circa_status
	public static final  Map<String, Availability> AVAILABILITY = new HashMap<String, Availability>(3);
	static{
		AVAILABILITY.put("Public", Availability.PUBLIC);
		AVAILABILITY.put("Private", Availability.PRIVATE);
	}

	// on audience
	public static final  Map<String, AudienceStatus> AUDIENCE = new HashMap<String, AudienceStatus>(3);
	static{
		AUDIENCE.put("1", AudienceStatus.Open);
		AUDIENCE.put("2", AudienceStatus.Closed);
	}

	// on dcterms_medium
	public static final  Map<String, MeetingType> MEETING_TYPE = new HashMap<String, MeetingType>(3);
	static{
		MEETING_TYPE.put("1", MeetingType.FACE_TO_FACE);
		MEETING_TYPE.put("2", MeetingType.VIRTUAL_MEETING);
		MEETING_TYPE.put("3", MeetingType.ELECTRONIC_WITH_CONNECTIX_VIDEO_PHONE);
		MEETING_TYPE.put("4", MeetingType.ELECTRONIC_WITH_ENHANCED_SEE_YOU_SEE_ME);
		MEETING_TYPE.put("5", MeetingType.ELECTRONIC_WITH_INTERNET_VIDEO_PHONE);
		MEETING_TYPE.put("6", MeetingType.ELECTRONIC_WITH_INTEL_PROSHARE);
		MEETING_TYPE.put("7", MeetingType.ELECTRONIC_WITH_MICROSOFT_NET_MEETING);
		MEETING_TYPE.put("8", MeetingType.ELECTRONIC_WITH_NETSCAPE_CONFERENCE);
		MEETING_TYPE.put("9", MeetingType.ELECTRONIC_WITH_NETSCAPE_COOLTALK);
		MEETING_TYPE.put("10", MeetingType.ELECTRONIC_WITH_VD_ONET_VDO_PHONE);
		MEETING_TYPE.put("11", MeetingType.ELECTRONIC_WITHOTHER_SOFTWARE);
	}

	//on dcterms_medium
	public static final  Map<String, EventType> EVENT_TYPE = new HashMap<String, EventType>(3);
	static{
		EVENT_TYPE.put("1", EventType.TASK);
		EVENT_TYPE.put("2", EventType.APPOINTMENT);
		EVENT_TYPE.put("3", EventType.OTHER);
	}

	//on circa_priority
	public static final  Map<String, EventPriority> EVENT_PRIORITY = new HashMap<String, EventPriority>(3);
	static{
		EVENT_PRIORITY.put("1", EventPriority.LOW);
		EVENT_PRIORITY.put("2", EventPriority.MEDIUM);
		EVENT_PRIORITY.put("3", EventPriority.HIGH);
		EVENT_PRIORITY.put("3", EventPriority.URGENT);
	}
}
