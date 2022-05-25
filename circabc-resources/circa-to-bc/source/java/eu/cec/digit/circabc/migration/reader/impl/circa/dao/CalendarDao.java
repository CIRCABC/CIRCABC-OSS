/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Calendar;


/**
 * Service layer accessing persistent circa <b>Calendar</b> and <b>CalendarLinguistic</b> entities directly
 *
 * @author Yanick Pignot
 */
public interface CalendarDao
{
	/**
	 * Get all calendars of an interest group
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Calendar> getAllCalendars(final String virtualCirca, final String interestGroup) throws SQLException, IOException;

	/**
	 * Get a specified calendar by its id
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param id						The unique id of the calendar
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract Calendar getCalendarById(final String virtualCirca, final String interestGroup, final String id)throws SQLException, IOException;
}
