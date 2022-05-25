/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;

/**
 * Validate if a user is not invited twice in the same Interest group
 *
 * @author yanick pignot
 */
public class InvitedUserProfileValidator extends JXPathValidator
{
	private static final String ALL_INVITED_USERS = ".//users";

	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		for(final InterestGroup interestGroup : getInterestGroups(context))
		{
			checkInvitedUsers(interestGroup, journal);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkInvitedUsers(final InterestGroup interestGroup, final MigrationTracer<ImportRoot> journal)
	{
		debug(journal, "Cheking duplicate invitation in interest group ", interestGroup);

		final Directory directory = interestGroup.getDirectory();

		final JXPathContext context = JXPathContext.newContext(directory);

		//final List<String> users = Collections.checkedList(context.selectNodes(ALL_INVITED_USERS), String.class);
		final List<String> users = (List<String>)context.selectNodes(ALL_INVITED_USERS);

		final List<String> duplicateUsers = new ArrayList<String>();

		for (final String user : users)
		{
			// don't check twice the same user
			if(!duplicateUsers.contains(user))
			{
				int freq = Collections.frequency(users, user) ;

				if(freq > 1)
				{
					duplicateUsers.add(user);
					error(journal, "User invited more than one time in the same interest group", user, freq + " times.", interestGroup);
				}
			}
		}
	}
}
