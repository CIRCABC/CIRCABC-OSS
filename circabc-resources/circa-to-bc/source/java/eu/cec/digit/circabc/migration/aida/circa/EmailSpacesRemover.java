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
 * @author Yanick Pignot
 *
 */
public class EmailSpacesRemover extends BaseConverter
{

	/**
	 *
	 */
	public EmailSpacesRemover()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.AidaMigrationService.UserConverter#convert(eu.cec.digit.circabc.migration.entities.generated.aida.Users.User)
	 */
	public void convert(User user) throws AidaException
	{
		final String email = user.getEmail();
		user.setEmail(convert(email));
	}

	public String convert(String email)
	{
		if(email != null)
		{
			return email.trim().replaceAll(" ", "");
		}
		else
		{
			return "";
		}
	}
}
