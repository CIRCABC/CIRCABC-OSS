/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.util.List;

import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.service.migration.ExportationException;


/**
 * Base interface for a user reader
 *
 * @author Yanick Pignot
 */
public interface UserReader
{

	/**
	 * Get all users that are invited in the interest group
	 *
	 * @param interestGroup
	 * @return
	 */
	public abstract List<Person> getInvitedPersons(final InterestGroup interestGroup) throws ExportationException;

	/**
	 * Get all users that are invited in the category
	 *
	 * @param category
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<Person> getInvitedPersons(final Category category) throws ExportationException;

	/**
	 * Build a single person with its uid
	 *
	 * @param uid
	 * @return
	 * @throws ExportationException
	 */
	public abstract Person getPerson( final String uid) throws ExportationException;

	/**
	 * Get the id of a user with its common name.
	 *
	 * @param cn
	 * @return
	 * @throws ExportationException
	 */
	public abstract String getPersonidWithCommonName( final String cn) throws ExportationException;
	
	/**
	 * Return true if the user exists
	 *
	 * @param userId
	 * @return
	 */
	public abstract boolean isPersonExists(final String userId) throws ExportationException;

	/**
	 * Get all users that are invited in the category
	 *
	 * @param emails					The list of emails
	 * @param conjunction				If the query is a conjunction or not
	 * @param negation					If the query is a negation or not
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<Person> getPersonsWithEmails(final List<String> emails, final boolean conjunction, final boolean negation) throws ExportationException;

}