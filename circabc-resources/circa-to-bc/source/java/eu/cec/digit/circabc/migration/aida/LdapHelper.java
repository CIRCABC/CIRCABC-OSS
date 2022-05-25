/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.aida;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.repo.migration.EncodingUtils;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.migration.ExportationException;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;

/**
 * Helper classes to deal with ldap Attributes.
 *
 * @author Yanick Pignot
 */
public abstract class LdapHelper
{
	private static final String LIST_ELEMENT_SEPARATOR = ",";
	private static final char DOMAIN_SEPARATOR = '@';
	private static final String IMPORTED_PROFILE_SEPARATOR = ":";
	private static final String REGEX_WITH_DOMAIN = ".+@cec|.+@circa";

	private LdapHelper(){}

	/**
	 * Return the value associated to the key. If the value is not found of it is invalid, throw an excepetion.
	 *
	 * @param attributes
	 * @param key
	 * @return
	 * @throws NamingException
	 * @throws ExportationException
	 */
	public static final String mandatoryValue(final Attributes attributes, final String key, final String ldapEncoding) throws NamingException, ExportationException
	{
		final String value = safeValue(attributes, key, ldapEncoding);

		if(value == null || value.trim().length() < 1)
		{
			throw new ExportationException("Impossible to retreive a valid value with key " + key + ". The value is " + ((value == null) ? "null" : "empty"));
		}
		else
		{
			return value;
		}
	}

	/**
	 * Return the values associated to the key. If the values are not found of it is invalid, throw an excepetion.
	 *
	 * @param attributes
	 * @param key
	 * @param onlyNotEmptyString
	 * @return
	 * @throws NamingException
	 * @throws ExportationException
	 * @throws UnsupportedEncodingException
	 */
	public static final List<String> mandatoryValues(final Attributes attributes, final String key, final boolean onlyNotEmptyString, final String ldapEncoding) throws NamingException, ExportationException
	{
		final List<String> values = safeValues(attributes, key, onlyNotEmptyString, ldapEncoding);

		if(values == null || values.size() < 1)
		{
			throw new ExportationException("Impossible to retreive a valid values with key " + key + ". The value is " + ((values == null) ? "null" : "empty"));
		}
		else
		{
			return values;
		}
	}

	/**
	 * Return a value associated to a key or null if the key doesn't exist
	 *
	 * @param attributes
	 * @param key
	 * @return
	 * @throws NamingException
	 * @throws UnsupportedEncodingException
	 */
	public static final String safeValue(final Attributes attributes, final String key, final String ldapEncoding) throws NamingException
	{
		final Attribute attribute = attributes.get(key);

		if(attribute == null)
		{
			return null;
		}
		else
		{
			final Object object = attributes.get(key).get();
			if(object instanceof String)
			{
				return (String) object;
			}
			else
			{
				try
				{
					return EncodingUtils.changeToUTF8Encoding((byte[]) object, ldapEncoding);
				}
				catch (UnsupportedEncodingException e)
				{
					return new String((byte[]) object);
				}
			}
		}
	}



	/**
	 * Return the values associated to a key or null if the key doesn't exist
	 *
	 * @param attributes
	 * @param key
	 * @param onlyNotEmptyString
	 * @return
	 * @throws NamingException
	 * @throws UnsupportedEncodingException
	 */
	public static final List<String> safeValues(final Attributes attributes, final String key, final boolean onlyNotEmptyString, final String ldapEncoding) throws NamingException
	{
		final Attribute attribute = attributes.get(key);

		if(attribute == null)
		{
			return null;
		}
		else
		{
			final int size = attribute.size();
			final List<String> values = new ArrayList<String>(size);

			Object iterValue;
			String iterString;
			for(int x = 0; x < size; ++x)
			{
				iterValue = attribute.get(x);

				if(iterValue == null)
				{
					iterString = null;
				}
				else if(iterValue instanceof String)
				{
					iterString = (String) iterValue;
				}
				else
				{
					try
					{
						iterString = EncodingUtils.changeToUTF8Encoding((byte[]) iterValue, ldapEncoding);
					}
					catch (UnsupportedEncodingException e)
					{
						iterString = new String((byte[]) iterValue);
					}
				}

				if(!onlyNotEmptyString || (iterString != null && iterString.trim().length() > 0))
				{
					values.add(iterString);
				}
			}

			return values;
		}
	}

	/**
	 * Retrieve the value defined after a key in a String representation of a map.
	 *
	 * @param from
	 * @param idStart
	 * @return
	 */
	public static final String retrieveValueWithIdInList(final String from, final String idStart)
	{
		int indexStart =  from.indexOf(idStart);

		return from.substring(indexStart + idStart.length(), from.indexOf(LIST_ELEMENT_SEPARATOR, indexStart));
	}

	/**
	 * Generate a valid profile id that the size must be greater that 2
	 *
	 * @param interestgroup					Can be null f the direcftopry service is not build yet
	 * @param profileName
	 * @return
	 */
	public static final String generateValidProfileName(final InterestGroup interestgroup, String profileName)
	{
		final int separatorIndex = profileName.indexOf(IMPORTED_PROFILE_SEPARATOR);
		if(separatorIndex > -1)
		{
			// guest and registred are not exportable / importable:  ig can become null!
			return profileName.substring(0, separatorIndex + 1) + generateValidProfileName(null, profileName.substring(separatorIndex + 1));
		}
		else
		{
			final String candidateName;
			switch (profileName.length())
			{
				case 1:
					candidateName = "00" + profileName;
					break;
				case 2:
					candidateName =  "0" + profileName;
					break;
				default:
					candidateName = profileName;
					break;
			}

			if(interestgroup != null && interestgroup.getDirectory() != null)
			{
				final Directory directory = interestgroup.getDirectory();
				String matchName = null;
				for(final AccessProfile profile: directory.getAccessProfiles())
				{
					if(candidateName.equals(profile.getName()))
					{
						// return the first generated name, it is not guest or registred.
						matchName = candidateName;
						break;
					}
				}

				if(matchName == null)
				{
					if(profileName.equals(ElementsHelper.getXPath(directory.getGuest())))
					{
						matchName = CircabcConstant.GUEST_AUTHORITY;
					}
					else if(profileName.equals(ElementsHelper.getXPath(directory.getRegistredUsers())))
					{
						matchName = CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME;
					}
				}

				return matchName == null ? candidateName : matchName;
			}
			else
			{
				// the diretory service is being constructed, don't search if it is guest or registred!
				return candidateName;
			}
		}

	}

	public static final boolean isUserid(final String user)
	{
		return user != null && user.matches(REGEX_WITH_DOMAIN);
	}

	public static final String removeDomainFromUid(String userId)
	{
		if(userId == null)
		{
			return null;
		}

		final int index = userId.indexOf(DOMAIN_SEPARATOR);

		if(index < 0)
		{
			return userId;
		}
		else
		{
			return userId.substring(0, index);
		}
	}

	public static final String retreiveDomainFromUid(String userId)
	{
		final int index = userId.indexOf(DOMAIN_SEPARATOR);

		if(index < 0)
		{
			return "";
		}
		else
		{
			return userId.substring(index + 1);
		}
	}


}
