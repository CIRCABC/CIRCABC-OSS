/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.validation;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.Unmarshaller;

import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;

/**
 * Handle the validation for a migration file
 *
 * @author Yanick Pignot
 */
public interface ValidationHandler
{

	/**
	 * Take a xml resources and process validation on it. If no error occurs, return the root binded root element.
	 *
	 * @param resource					The xml file to validate
	 * @param unmarshaller				The unmarshaller used for the validation
	 * @param journal					The journal to fill results
	 * @throws ValidationException		If an blocking error occurs
	 */
	public void validateXMLFile(final Unmarshaller unmarshaller, final InputStream resource, final MigrationTracer<ImportRoot> journal) throws ValidationException;

	/**
	 * Take a xml resources and process validation on it. If no error occurs, return the root binded root element.
	 *
	 * @param unmarshaller				The unmarshaller used for the validation
	 * @param resource					The xml file to validate
	 * @param schemaResource			The xsd to use for validation
	 * @param journal					The journal to fill results
	 * @throws ValidationException		If an blocking error occurs
	 */
	public void validateXMLFile(final Unmarshaller unmarshaller, final InputStream resource, final Resource schemaResource, final MigrationTracer<ImportRoot> journal) throws ValidationException;

	/**
	 * Take a ImportRoot element objetc graph and apply validation on it.
	 * Log, warns, errors, ... will be returned in a list of validation message
	 *
	 * @see eu.cec.digit.circabc.migration.journal.MigrationLog
	 *
	 * @param journal					The journal to fill results
	 * @throws ValidationException		If an blocking error occurs
	 */
	public void validateDataStructure(final MigrationTracer<ImportRoot> journal) throws ValidationException;


	/**
	 * Set validators that must applied to the Import root object
	 *
	 * @see eu.cec.digit.circabc.migration.validation.Validator
	 *
	 *@param validators				A valid list of validator class name
	 **/
	public void setValidators(final List<String> validators);
}
