/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import eu.cec.digit.circabc.util.CircabcUserDataBean;



/**
 * Validate the users setted in the given object graph
 *
 * @author yanick pignot
 */
public class LdapUserValidator extends BaseUserValidator 
{

	public LdapUserValidator()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.validation.impl.UserValidator#isUserExists(java.lang.String)
	 */
	public boolean isUserExists(String user)
	{
		final CircabcUserDataBean userData = getRegistry().getUserService().getLDAPUserDataByUid(user);
	
		return userData != null;
	}

}
