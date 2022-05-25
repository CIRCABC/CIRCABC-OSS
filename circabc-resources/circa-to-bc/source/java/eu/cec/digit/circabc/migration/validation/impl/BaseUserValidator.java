package eu.cec.digit.circabc.migration.validation.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;

public abstract class BaseUserValidator extends JXPathValidator
{

	private static final String QUERY_USER= ".//user";
	private static final String QUERY_USERS= ".//users";
	
	abstract boolean isUserExists(String user);

	public BaseUserValidator()
	{
		super();
	}

	@Override
	protected void validateCircabcImpl(final JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		Set<String> users = getUsers(context, QUERY_USER);
		users.addAll(getUsers(context, QUERY_USERS));
		for(final String user: users)
		{
			if(user == null || user.trim().length() < 1)
			{
				debug(journal, "Empty user name found. ", user);
			}
			else if(!isUserExists(user))
			{
				error(journal, "User doesn't exists, please to create it before!", user);
			}
			else
			{
				debug(journal, "User found", user);
			}
		}
	
	}

	
	

	@SuppressWarnings("unchecked")
	private Set<String> getUsers(final JXPathContext context, String query)
	{
		final List<String> originalUsers = (List<String>) context.selectNodes(query);
	
		final Set<String> uniqueUsers = new HashSet<String>();
	
		for(final String user: originalUsers)
		{
			uniqueUsers.add(user);
		}
	
		return uniqueUsers;
	}

}