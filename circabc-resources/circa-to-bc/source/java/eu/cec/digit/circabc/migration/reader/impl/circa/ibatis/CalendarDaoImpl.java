/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.CalendarDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Calendar;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.CalendarLinguistic;


/**
 * Implementation of the circa calendar dao using ibatis sql mapper.
 *
 * @author Yanick Pignot
 */
public class CalendarDaoImpl implements CalendarDao
{
	private static final String QUERY_ALL_CALENDARS = "getAllCalendars";
	private static final String QUERY_SPECIFIC_CALENDAR = "getCalendar";
	private static final String QUERY_SPECIFIC_CALENDAR_LINGUISTIC = "getCalendarLinguistic";

	private static final String PARAM_ID = "id";

	public List<Calendar> getAllCalendars(final String virtualCirca, final String interestGroup) throws SQLException, IOException
	{
		final List<Calendar> calendars = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_CALENDARS) ;

		for(final Calendar calendar: calendars)
		{
			fillLinguistic(virtualCirca, interestGroup, calendar);
		}

		return calendars;
	}

	public Calendar getCalendarById(final String virtualCirca, final String interestGroup, final String id) throws SQLException, IOException
	{
		final Calendar calendar = IbatisDaoManager.querySingle(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_CALENDAR, Collections.singletonMap(PARAM_ID, id));
		fillLinguistic(virtualCirca, interestGroup, calendar);
		return calendar;
	}

	private void fillLinguistic(final String virtualCirca, final String interestGroup, final Calendar calendar) throws SQLException, IOException
	{
		final Map<String, String>param = Collections.singletonMap(PARAM_ID, calendar.getId());
		final List<CalendarLinguistic> linguistic = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_CALENDAR_LINGUISTIC, param);
		calendar.setCalendarLinguistics(linguistic);
	}

}
