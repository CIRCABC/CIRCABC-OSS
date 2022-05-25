/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ParameterCheck;

/**
 * Util class to perform queries against the circa database usin Ibatis
 *
 * @author Yanick Pignot
 */
final /*package*/ class IbatisDaoManager
{
	private static final String PARAM_VIRTUAL_CIRCA = "virtualCirca";
	private static final String PARAM_INTEREST_GROUP = "interestGroup";

	private IbatisDaoManager(){}

	/*package*/ static <T> List<T> query(final String virtualCirca, final String interestGroup, final String queryName) throws SQLException, IOException	{
		return query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), queryName, null);

	}

	@SuppressWarnings("unchecked")
	/*package*/ static <T> List<T> query(final String virtualCirca, final String interestGroup, final String queryName, final Map<String, String> parameters) throws SQLException, IOException
	{
		final Map<String, String> newParameters = enhanceParameters(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), queryName, parameters);
		
		try
		{
			return MapClient.getInstance().selectList(queryName, newParameters);	
		}
		catch(final Throwable t)
		{
			// try a second time to prevent non managed socket error. 
			return MapClient.getInstance().selectList(queryName, newParameters);
		}
		
	}

	@SuppressWarnings("unchecked")
	/*package*/ static <T> T querySingle(final String virtualCirca, final String interestGroup, final String queryName) throws SQLException, IOException
	{
		return (T) querySingle(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), queryName, null);
	}

	@SuppressWarnings("unchecked")
	/*package*/ static <T> T querySingle(final String virtualCirca, final String interestGroup, final String queryName, final Map<String, String> parameters) throws SQLException, IOException
	{
		final Map<String, String> newParameters = enhanceParameters(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), queryName, parameters);
		try
		{
			return (T) MapClient.getInstance().selectOne(queryName, newParameters);
		}
		catch(final Throwable t)
		{
			// try a second time to prevent non managed socket error. 
			return (T) MapClient.getInstance().selectOne(queryName, newParameters);
		}
	}

	private static final Map<String, String> enhanceParameters(final String virtualCirca, final String interestGroup, final String queryName, final Map<String, String> parameters)
	{
		ParameterCheck.mandatoryString("The virtual circa", virtualCirca);
		ParameterCheck.mandatoryString("The interest group", interestGroup);
		ParameterCheck.mandatoryString("The query name", queryName);

		final Map<String, String> enhancedParameters = new HashMap<String, String>();
		if(parameters != null)
		{
			enhancedParameters.putAll(parameters);
		}

		enhancedParameters.put(PARAM_VIRTUAL_CIRCA, DBUtil.getValidName(virtualCirca));
		enhancedParameters.put(PARAM_INTEREST_GROUP, DBUtil.getValidName(interestGroup));

		return enhancedParameters;

	}

}


