package eu.cec.digit.circabc.migration.validation.impl;


public class CircabcUserValidator extends BaseUserValidator 
{
	public CircabcUserValidator()
	{
		super();
	}
	

	public boolean isUserExists(String user)
	{
		return  getRegistry().getNonSecuredPersonService().personExists(user);
	}

}
