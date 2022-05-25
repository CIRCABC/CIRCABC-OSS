/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.etl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.ParameterCheck;

import eu.cec.digit.circabc.migration.entities.TypedProperty.EmailProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.FirstNameProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.LastNameProperty;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.util.CircabcUserDataBean;

/**
 * @author Yanick Pignot
 **/
public class PathologicUser implements Serializable
{
	/** */
	private static final long serialVersionUID = -123459804374289072L;

	private final Person person;
	private final String message;
	private final Map<String, User> propositions = new HashMap<String, User>();
	private final Boolean personWasNull;

	/**
	 * @param person
	 * @param message
	 * @param propositions
	 */
	public PathologicUser(final Person person, final String message, final Map<String, User> propositions, final Boolean personWasNull)
	{
		super();
		this.person = person;
		this.message = message;
		this.personWasNull = personWasNull;
		if(propositions != null )
		{
			this.propositions.putAll(propositions);
		}

		ParameterCheck.mandatory("The person exported data", person);

		for(final Map.Entry<String, User> entry: this.propositions.entrySet())
		{
			final String uid = entry.getKey();
			final User data = entry.getValue();

			ParameterCheck.mandatoryString("The person ldap userId ", uid);
			ParameterCheck.mandatory("The person ldap data for user id " + uid, data);
			ParameterCheck.mandatoryString("The person ldap moniker for user id " + uid, data.getMoniker());
			ParameterCheck.mandatoryString("The person ldap email for user id " + uid, data.getEmail());
		}
	}


	/**
	 * @return the message
	 */
	public final String getMessage()
	{
		return message;
	}
	/**
	 * @return the person
	 */
	public final Person getPerson()
	{
		return person;
	}
	/**
	 * @return the propositions
	 */
	public final Map<String, User> getPropositions()
	{
		return propositions;
	}

	/**
	 * @return the propositions
	 */
	public final void resetPropositions(final Map<String, User> newPropositions)
	{
		propositions.clear();
		if(newPropositions != null)
		{
			propositions.putAll(newPropositions);
		}
	}

	/**
	 * @return the personWasNull
	 */
	public final Boolean getPersonWasNull()
	{
		return personWasNull;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(" + person.getUserId().getValue() + " with message '" + message + "')";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((message == null) ? 0 : message.hashCode());
		result = PRIME * result + ((person == null) ? 0 : person.hashCode());
		result = PRIME * result + ((propositions == null) ? 0 : propositions.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PathologicUser other = (PathologicUser) obj;
		if (message == null)
		{
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (person == null)
		{
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		if (propositions == null)
		{
			if (other.propositions != null)
				return false;
		} else if (!propositions.equals(other.propositions))
			return false;
		return true;
	}


	public CircabcUserDataBean createUserDataBean()
	{
		if (this.person == null)
		{
			throw new IllegalStateException("Person can not be null!");
		}
		CircabcUserDataBean result = new CircabcUserDataBean(); 
		final String userName = (String) this.person.getUserId().getValue();
		result.setUserName(userName);
		result.setEcasUserName(null); 
		final FirstNameProperty firstName = this.person.getFirstName();
		if (firstName != null)
		{
			result.setFirstName((String) firstName.getValue());
		}
		else
		{
			result.setFirstName(userName);
		}
		final LastNameProperty lastName = this.person.getLastName();
		if (lastName != null)
		{
			result.setLastName((String) lastName.getValue());
		}
		else
		{
			result.setFirstName(userName);
		}
		final EmailProperty email = this.person.getEmail();
		if (email != null)
		{
			result.setEmail((String) email.getValue());
		}
		else
		{
			if (userName.indexOf('@') > -1)
			{
				result.setFirstName(userName);
			}
			else
			{
				result.setFirstName(userName + "@circa" );
			}
				
		}
		return result;
	}
}