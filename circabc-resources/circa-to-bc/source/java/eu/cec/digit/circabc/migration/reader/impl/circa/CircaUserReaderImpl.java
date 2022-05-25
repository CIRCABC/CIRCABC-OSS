/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.ldap.CircaLdapClient;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concrete implementation of a user reader for circa using LDAP.
 *
 * @author Yanick Pignot
 */
public class CircaUserReaderImpl implements UserReader
{
	private static final Log logger = LogFactory.getLog(CircaUserReaderImpl.class);

	private static final String POSITIVE_NOTIFCATION_VALUE = "enabled";
	private static final String ATTR_MY_NOTIFICATIONS = "mynotifications";
	private static final String ATTR_LABELED_URI = "labeledURI";
	private static final String ATTR_FAX = "fax";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_POSTAL_ADDRESS = "postalAddress";
	private static final String ATTR_TELEPHONE_NUMBER = "telephoneNumber";
	private static final String ATTR_ORG = "o";
	private static final String ATTR_TITLE = "title";
	private static final String ATTR_SN = "sn";
	private static final String ATTR_CN = "cn";
	private static final String ATTR_MYLANGUAGE = "mylanguage";
	private static final String ATTR_MAIL = "mail";
	private static final String ATTR_UID = "uid";
	private static final String ATTR_USER_PASSWORD = "userPassword";

	private CircaLdapClient ldapClient;

	private static final String UID_EQUALS = "uid=";
	private static final String QUERY_SINGLE_USER = "(" + ATTR_UID + "={0})";	
	private static final String QUERY_SINGLE_EMAIL = "(" + ATTR_MAIL + "={0})";
	private static final String QUERY_SINGLE_CN = "(" + ATTR_CN + "={0})";


	public boolean isPersonExists(final String userId) throws ExportationException
	{
		return getUser(userId)!= null;
	}

	public List<Person> getInvitedPersons(final InterestGroup interestGroup) throws ExportationException
	{
		final Category category = ElementsHelper.getElementCategory(interestGroup);
		final String categoryName = (String) category.getName().getValue();
		final String interestGroupName = (String) interestGroup.getName().getValue();

		final List<String> userIds = getIGUsers(categoryName, interestGroupName);
		final List<Person> users = buildPersons(userIds);

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Users invited in the interest group:");
			logger.debug("  -----  Category:                       " + categoryName);
			logger.debug("  -----  Interest Group:                 " + interestGroupName);
			logger.debug("  -----  Number:                         " + userIds.size());
			logger.debug("  -----  Already added in the structure: " + (userIds.size() - users.size()));
		}
		return users ;
	}

	public List<Person> getInvitedPersons(final Category category) throws ExportationException
	{
		final String categoryName = (String) category.getName().getValue();
		final List<String> userIds = getCategoryUsers(categoryName);
		final List<Person> users = buildPersons(userIds);

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Users invited in the Category:");
			logger.debug("  -----  Category:                       " + categoryName);
			logger.debug("  -----  Number:                         " + userIds.size());
			logger.debug("  -----  Already added in the structure: " + (userIds.size() - users.size()));
		}
		return users ;
	}

	public List<Person> getPersonsWithEmails(final List<String> emails, final boolean conjunction, final boolean negation) throws ExportationException
	{
		final List<Person> persons;
		final StringBuilder builder = new StringBuilder("");

		final int size = emails == null ? 0 : emails.size();
		if(size < 1)
		{
			persons = Collections.<Person>emptyList();
		}
		else
		{
			if(negation)
			{
				builder
					.append("(")
					.append("!");
			}

			if(size != 1)
			{
				builder
					.append("(")
					.append(conjunction ? "&" : "|");
			}

			for(final String email: emails)
			{
				builder.append(MessageFormat.format(QUERY_SINGLE_EMAIL, email));
			}

			if(size != 1)
			{
				builder
					.append(")");
			}

			if(negation)
			{
				builder
					.append(")");
			}

			if(logger.isDebugEnabled())
			{
				logger.debug("Start to launch query against ldap: " + builder.toString() + "...");
			}

			final List<Attributes> users = getUserWithQuery(builder.toString());
			persons = new ArrayList<Person>(users.size());

			try
			{
				for(final Attributes attr: users)
				{
					persons.add(buildPerson(attr));
				}
			}
			catch (NamingException e)
			{
				throw new ExportationException("Error accessing to the ldap user data. With query: " + builder.toString());
			}
		}

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Users found:");
			logger.debug("  -----  Number:                         " + persons.size());
			logger.debug("  -----  Emails: 						   " + emails + " (conjunction: " + conjunction + ")");
			logger.debug("  -----  Query: 						   " + builder.toString());
		}

		return persons;
	}

	/**
	 * @param userIds
	 * @return
	 * @throws ExportationException
	 * @throws NamingException
	 */
	private List<Person> buildPersons(final List<String> userIds) throws ExportationException
	{
		final List<Person> users = new ArrayList<Person>();

		for(final String uid: userIds)
		{
			users.add(getPerson(uid));
		}
		return users;
	}

	/**
	 * @param users
	 * @param uid
	 * @throws ExportationException
	 */
	public Person getPerson( final String uid) throws ExportationException
	{
		Attributes attributes;
		try
		{
			attributes = getUser(uid);
			final Person person;

			if(attributes != null)
			{
				person = buildPerson(attributes);
			}
			else
			{
				person = null;
			}

			return person;
		}
		catch (NamingException e)
		{
			throw new ExportationException("Error accessing to the ldap user data. With user id: " + uid, e);
		}
	}
	
	public String getPersonidWithCommonName( final String cn) throws ExportationException
	{
		try
		{
			final List<Attributes> result = ldapClient.queryUser(MessageFormat.format(QUERY_SINGLE_CN, cn));
	
			if(result.size() == 1)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Users data successfully found in LDAP for CN : " + cn);
				}
	
				return LdapHelper.mandatoryValue(result.get(0), ATTR_UID, ldapClient.getSystemEncoding());
			}
			if(result.size() == 0)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Users data successfully found in LDAP for CN : " + cn);
				}
	
				return null;
			}
			else
			{
				if(logger.isDebugEnabled())
				{
					logger.warn("To many user found for CN : " + cn);
				}
	
				return null;
			}
		}
		catch (NamingException e)
		{
			throw new ExportationException("Error accessing to the ldap user data. With user cn: " + cn, e);
		}
		
	}

	private Person buildPerson(final Attributes attributes) throws NamingException, ExportationException
	{
		final Person person;
		final String systemEncoding = ldapClient.getSystemEncoding();

		person = new Person()
			.withUserId(new TypedProperty.UserIdProperty(LdapHelper.mandatoryValue(attributes, ATTR_UID, systemEncoding)));

		final String lastName = LdapHelper.safeValue(attributes, ATTR_SN, systemEncoding);
		String firstName = LdapHelper.safeValue(attributes, ATTR_CN, systemEncoding);

		if(firstName != null && firstName.contains(lastName))
		{
			final int idx =  firstName.lastIndexOf(lastName);

			if(idx == 0)
			{
				firstName = firstName.substring(lastName.length()).trim();
			}
			else
			{
				firstName = firstName.substring(0, idx).trim();
			}
		}

		final String notification = LdapHelper.safeValue(attributes, ATTR_MY_NOTIFICATIONS, systemEncoding);
		final boolean isNotifiable = notification == null || notification.startsWith(POSITIVE_NOTIFCATION_VALUE);

		person
			.withPassword(new TypedProperty.PasswordProperty(LdapHelper.safeValue(attributes, ATTR_USER_PASSWORD, systemEncoding)))
			.withEmail(new TypedProperty.EmailProperty(LdapHelper.safeValue(attributes, ATTR_MAIL, systemEncoding)))
			.withLastName(new TypedProperty.LastNameProperty(lastName))
			.withFirstName(new TypedProperty.FirstNameProperty(firstName))
			.withTitle(new TypedProperty.UserTitleProperty(LdapHelper.safeValue(attributes, ATTR_TITLE, systemEncoding)) )
			.withOrganisation(new TypedProperty.OrganisationProperty(LdapHelper.safeValue(attributes, ATTR_ORG, systemEncoding)))
			.withPhone(new TypedProperty.PhoneProperty(LdapHelper.safeValue(attributes, ATTR_TELEPHONE_NUMBER, systemEncoding)))
			.withPostalAddress(new TypedProperty.PostalAddressProperty(LdapHelper.safeValue(attributes, ATTR_POSTAL_ADDRESS, systemEncoding)))
			.withDescription(new TypedProperty.UserDescriptionProperty(LdapHelper.safeValue(attributes, ATTR_DESCRIPTION, systemEncoding)))
			.withFax(new TypedProperty.FaxProperty(LdapHelper.safeValue(attributes, ATTR_FAX, systemEncoding)))
			.withUrlAddress(new TypedProperty.URLAddressProperty(LdapHelper.safeValue(attributes, ATTR_LABELED_URI, systemEncoding)))
			//.withContentLanguageFilter(new TypedPreference.ContentFilterLanguagePreference(LdapHelper.safeValue(attributes, "")))
			.withInterfaceLanguage(new TypedPreference.InterfaceLanguagePreference(LdapHelper.safeValue(attributes, ATTR_MYLANGUAGE, systemEncoding)))
			.withGlobalNotification(new TypedProperty.GlobalNotificationProperty(isNotifiable))
			.withPersonalInformation(new TypedProperty.PersonalInformationProperty(true));
		return person;
	}



	/**
	 * @param category
	 * @param interestGroup
	 * @return
	 * @throws ExportationException
	 */
	private List<String> getInvitedUserUid(final List<Attributes> nodes) throws NamingException
	{
		final List<String> userIds = new ArrayList<String>();

		for(Attributes attributes: nodes)
		{
			final List<String> invitedUsers = LdapHelper.safeValues(attributes, CircaLdapClient.KEY_INVITED_PERSONS, true, ldapClient.getSystemEncoding());

			if(invitedUsers != null)
			{
				for(final String user: invitedUsers)
				{
					final String userId = LdapHelper.retrieveValueWithIdInList(user, UID_EQUALS);

					if(userIds.contains(userId) == false)
					{
						userIds.add(userId);
					}
				}
			}
		}
		return userIds;
	}

	/**
	 * @param category
	 * @param interestGroup
	 * @return
	 * @throws ExportationException
	 */
	private List<String> getIGUsers(final String categoryName, final String interestGroupName) throws ExportationException
	{
		final List<Attributes> result = ldapClient.queryInterestGroup(categoryName, interestGroupName, CircaLdapClient.QUERY_PROFILES);

		try
		{
			return getInvitedUserUid(result);
		}
		catch (NamingException e)
		{
			throw new ExportationException("Error accessing to the ldap Interest Group data. With name: " + interestGroupName);
		}
	}

	/**
	 * @param categoryName
	 * @return
	 * @throws ExportationException
	 */
	private List<String> getCategoryUsers(final String categoryName) throws ExportationException
	{
		final String query = MessageFormat.format(CircaLdapClient.QUERY_SPECIFIC_CATEGORY,categoryName );

		final List<Attributes> result = ldapClient.queryCircaRoot(query);

		try
		{
			return getInvitedUserUid(result);
		}
		catch (NamingException e)
		{
			throw new ExportationException("Error accessing to the ldap Category data. With name: " + categoryName);
		}
	}

	private List<Attributes> getUserWithQuery(final String query) throws ExportationException
	{
		final List<Attributes> result = ldapClient.queryUser(query);

		if(result.size() > 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Users data successfully found in LDAP for query : " + query);
			}

			return result;
		}
		else
		{
			if(logger.isWarnEnabled())
			{
				logger.debug("User data NOT FOUND found in LDAP for query : " + query);
			}

			return Collections.<Attributes>emptyList();
		}
	}

	private Attributes getUser(final String userId) throws ExportationException
	{
		final List<Attributes> result = ldapClient.queryUser(MessageFormat.format(QUERY_SINGLE_USER, userId));

		if(result.size() > 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Users data successfully found in LDAP for UID : " + userId);
			}

			return result.get(0);
		}
		else
		{
			if(logger.isWarnEnabled())
			{
				logger.warn("Users data NOT FOUND found in LDAP for UID : " + userId);
			}

			return null;
		}
	}

	/**
	 * @param circaLdapClient the circaLdapClient to set
	 */
	public final void setLdapClient(CircaLdapClient ldapClient)
	{
		this.ldapClient = ldapClient;
	}


}