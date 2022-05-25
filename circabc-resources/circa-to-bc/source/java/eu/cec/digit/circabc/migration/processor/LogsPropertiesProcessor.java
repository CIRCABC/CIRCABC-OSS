/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor;

import java.io.InputStream;

import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Interface of a class that processes the migration.
 *
 * @author Yanick Pignot
 */
public interface LogsPropertiesProcessor extends Processor
{

	/**
	 * Proccess the special case of ig logs files as a property map.
	 *
	 * @param igLogProperties
	 * @param importRoot
	 * @return						Return the report of the process
	 */
	public abstract Report process(final CircabcServiceRegistry registry, final InputStream logInputStream, final MigrationTracer importationJournal) throws ImportationException;

}