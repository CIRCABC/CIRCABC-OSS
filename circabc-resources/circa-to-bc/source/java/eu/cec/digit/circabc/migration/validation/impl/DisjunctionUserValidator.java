package eu.cec.digit.circabc.migration.validation.impl;

import eu.cec.digit.circabc.util.CircabcUserDataBean;



public class DisjunctionUserValidator extends   BaseUserValidator 
{

	public DisjunctionUserValidator()
	{
		super();
	}
	@Override
	boolean isUserExists(String user)
	{
		boolean result;
		result = getRegistry().getNonSecuredPersonService().personExists(user);
		if (result == false)
		{
			final CircabcUserDataBean userData = getRegistry().getUserService().getLDAPUserDataByUid(user);
			
			result =  (userData != null );
		}
		return result;
	}

	
	

}
