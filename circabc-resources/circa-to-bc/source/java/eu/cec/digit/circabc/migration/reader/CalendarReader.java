/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.util.List;

import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for classes that help the reading of a remote events and meeting structure for an exportation.
 *
 * @author Yanick Pignot
 */
public interface CalendarReader
{
	/**
	 * Return all Appointments (meetings and events) defined in an interest group
	 *
	 * @param events
	 * @return
	 * @throws ExportationException
	 */
	abstract public List<Appointment> getAllAppointments(final Events events) throws ExportationException;

}
