/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

/**
 * @author Yanick Pignot
 *
 */
public abstract class ReflexionUtils
{
	@SuppressWarnings("unchecked")
	public static final <T> T buildValidator(final String objectClassName) throws Exception
	{
		Class<T> clazz;

		try
		{
			clazz = (Class<T>) Class.forName(objectClassName);
		}
		catch (ClassNotFoundException e)
		{
			throw new Exception("Unexisting class name: " + objectClassName);
		}

		try
		{
			return clazz.newInstance();
		}
		catch (InstantiationException e)
		{
			throw new Exception("Impossible to instanciate class, is '" + objectClassName + "' class? Message : " + e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new Exception("Impossible to instanciate class, is '" + objectClassName + "' and its constructor accessible? Message : " + e.getMessage());
		}
	}
}
