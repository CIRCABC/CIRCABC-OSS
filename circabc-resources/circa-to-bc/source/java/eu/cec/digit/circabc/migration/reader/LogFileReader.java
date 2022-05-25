/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.io.OutputStream;

import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for classes that help the reading ointerest group log files.
 *
 * @author Yanick Pignot
 */
public interface LogFileReader
{
	
	/**
	 * Print all interest groups in a given stream to reduce memory consumtion
	 *
	 * @param interestGroup						The interest group to query to get its log entries
	 * @param outputStream						Where to write
	 * @throws ExportationException
	 */
	abstract public void addLogEntries(final InterestGroup interestGroup, final OutputStream outputStream) throws ExportationException;

}
