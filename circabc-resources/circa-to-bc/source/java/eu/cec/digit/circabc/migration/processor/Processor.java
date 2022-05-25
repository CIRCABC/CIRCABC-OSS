/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Interface of a class that porcesses the migration.
 *
 * @author Yanick Pignot
 */
public interface Processor
{

	/**
	 * Apply process on import root element to make it persistent, to report it, ...
	 *
	 * @param importationJournal
	 * @param importRoot
	 * @return						Return the report of the process
	 */
	public abstract Report process(final CircabcServiceRegistry registry, final ImportRoot importRoot, final MigrationTracer importationJournal) throws ImportationException;

}