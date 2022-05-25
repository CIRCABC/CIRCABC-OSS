/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.validation;

/**
 * Define the kind of validation that must be performed
 *
 * @author Yanick Pignot
 */
public enum ValidationMode
{

	/**
	 * validation must return an execpetion when an error is found
	 */
	FAIL_ON_ERROR,

	/**
	 * validation must return an execpetion when a warning is found
	 */
	FAIL_ON_WARNING,

	/**
	 * validation just log errors, warnings and logs
	 */
	LOG_ONLY

}
