/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.aida.circa;

import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.service.migration.AidaException;

/**
 * Voter that reject 'europa.eu' email adresses.
 *
 * @author Yanick Pignot
 */
public class EuropaMailVoter extends BaseVoter
{

	private static final String REJECTED_DOMAIN = "europa.eu";

	/**
	 *
	 */
	public EuropaMailVoter()
	{
		super();
	}


	public boolean addUser(final User user) throws AidaException
	{
		final String email = user.getEmail();

		if(email != null)
		{
			return email.contains(REJECTED_DOMAIN) == false;
		}
		else
		{
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.AidaMigrationService.UserVoter#getRejectMessage()
	 */
	public String getRejectMessage()
	{
		return "The user has an email that contains the " + REJECTED_DOMAIN + " domain";
	}

}
