/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.util.Collections;
import java.util.List;

import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support CalendarReader
 *
 * @author Yanick Pignot
 */
public class CalendarReaderNOOPImpl implements CalendarReader
{

	public List<Appointment> getAllAppointments(Events events) throws ExportationException
	{
		return Collections.<Appointment>emptyList();
	}


}
