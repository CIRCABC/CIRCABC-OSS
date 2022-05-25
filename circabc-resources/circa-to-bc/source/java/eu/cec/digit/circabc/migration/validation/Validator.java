/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;

/**
 * Base interface used to validate any element in the given unmarshalled import file
 *
 * @author Yanick Pignot
 */
public interface Validator
{

	/**
	 * Perform the validation and return the list of messages
	 *
	 * @param registry				The circabc service registry needed for services calls
	 * @param journal				The journal to fill
	 * @throws Exception			Any problem that can occurs when validating
	 */
	public abstract void performValidation(final CircabcServiceRegistry registry,  final MigrationTracer<ImportRoot> journal) throws Exception;

}