/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.io.OutputStream;

import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.reader.LogFileReader;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support LogFileReader
 *
 * @author Yanick Pignot
 */
public class LogFileReaderNOOPImpl implements LogFileReader
{


	public void addLogEntries(final InterestGroup interestGroup, final OutputStream outputStream) throws ExportationException
	{

	}

}
