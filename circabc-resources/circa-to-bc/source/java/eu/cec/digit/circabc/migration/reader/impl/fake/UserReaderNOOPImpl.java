/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.util.Collections;
import java.util.List;

import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.service.migration.ExportationException;


/**
 * A simple implementation that does not support UserReader
 *
 * @author Yanick Pignot
 */
public class UserReaderNOOPImpl implements UserReader
{

	public List<Person> getInvitedPersons(InterestGroup interestGroup) throws ExportationException
	{
		return Collections.<Person>emptyList();
	}

	public List<Person> getInvitedPersons(Category category) throws ExportationException
	{
		return Collections.<Person>emptyList();
	}

	public Person getPerson(String uid) throws ExportationException
	{
		return null;
	}

	public String getPersonidWithCommonName(String cn) throws ExportationException
	{
		return null;
	}

	public List<Person> getPersonsWithEmails(List<String> emails, boolean conjunction, boolean negation) throws ExportationException
	{
		return Collections.<Person>emptyList();
	}

	public boolean isPersonExists(String userId) throws ExportationException
	{
		return false;
	}



}