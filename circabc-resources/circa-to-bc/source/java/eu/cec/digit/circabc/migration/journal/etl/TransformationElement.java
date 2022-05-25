/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.etl;

import java.io.Serializable;

import org.alfresco.util.ParameterCheck;

import eu.cec.digit.circabc.migration.entities.TypedProperty.EmailProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.FirstNameProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.LastNameProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.UserIdProperty;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;

/**
 * @author Yanick Pignot
 **/
public class TransformationElement implements Serializable
{
	/** */
	private static final long serialVersionUID = -881889804374289072L;

	final Person originalPerson;
	private final String validUserId;
	private final User validUserData;

	/**
	 * @param exportName
	 * @param importName
	 * @param paths
	 */
	public TransformationElement(final Person originalPerson, final String validUserId, final User user)
	{
		super();
		this.originalPerson = originalPerson;
		this.validUserId = validUserId;
		this.validUserData = user;

		ParameterCheck.mandatory("The person exported data", originalPerson);
		ParameterCheck.mandatoryString("The person ldap userId ", validUserId);
		ParameterCheck.mandatory("The person ldap data", validUserData);
		ParameterCheck.mandatoryString("The person ldap moniker for user id " + validUserId, validUserData.getMoniker());
		ParameterCheck.mandatoryString("The person ldap email for user id " + validUserId, validUserData.getEmail());
	}

	public TransformationElement(PathologicUser pathologicUser)
	{
		super();
		final Person person = pathologicUser.getPerson();
		this.originalPerson = person;
		final UserIdProperty userIdProperty = person.getUserId();
		final EmailProperty emailProperty = person.getEmail();
		final LastNameProperty lastNameProperty = person.getLastName();
		final FirstNameProperty firstNameProperty = person.getFirstName();
		
		this.validUserId = (String) userIdProperty .getValue();
		
		String email = null;
		String lastname = null;
		String firstname = null;
		String moniker = null;
		if (userIdProperty != null) 
		{
			moniker = (String) userIdProperty.getValue();
		}
		if (emailProperty != null)
		{
			email = (String) emailProperty.getValue();
			if (email.trim().equals(""))
			{
				if (this.validUserId.indexOf('@') > 0)
				{
					this.originalPerson.setEmail(new EmailProperty(this.validUserId));
					email = this.validUserId;
				}
			}
		}
		else
		{
			if (this.validUserId.indexOf('@') > 0)
			{
				this.originalPerson.setEmail(new EmailProperty(this.validUserId));
				email = this.validUserId;
			}
			
		}
		if (lastNameProperty != null)
		{
			lastname = (String) lastNameProperty.getValue();
		}
		if (firstNameProperty != null)
		{
			firstname = (String) firstNameProperty.getValue();
			
		}
		
	
		
		this.validUserData = new  User (moniker,firstname,lastname,email) ;
	}

	/**
	 * @return the originalName
	 */
	public final Person getOriginalPerson()
	{
		return originalPerson;
	}

	/**
	 * @return the validUserData
	 */
	public final User getValidUserData()
	{
		return validUserData;
	}

	/**
	 * @return the validUserId
	 */
	public final String getValidUserId()
	{
		return validUserId;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(" + originalPerson.getUserId().getValue() + " with id '" + validUserId + "')";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((originalPerson == null) ? 0 : originalPerson.hashCode());
		result = PRIME * result + ((validUserData == null) ? 0 : validUserData.hashCode());
		result = PRIME * result + ((validUserId == null) ? 0 : validUserId.hashCode());
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
		final TransformationElement other = (TransformationElement) obj;
		if (originalPerson == null)
		{
			if (other.originalPerson != null)
				return false;
		} else if (!originalPerson.equals(other.originalPerson))
			return false;
		if (validUserData == null)
		{
			if (other.validUserData != null)
				return false;
		} else if (!validUserData.equals(other.validUserData))
			return false;
		if (validUserId == null)
		{
			if (other.validUserId != null)
				return false;
		} else if (!validUserId.equals(other.validUserId))
			return false;
		return true;
	}
}