/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.GlobalDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Global;

/**
 * Implementation of the circa global dao using ibatis sql mapper.
 *
 * @author Yanick Pignot
 */
public class GlobalDaoImpl implements GlobalDao
{
	private static final String QUERY_ALL_GLOBAL_DOCUMENTS = "getAllGlobalDocuments";
	private static final String QUERY_SPECIFIC_GLOBAL_DOCUMENT = "getGlobalDocument";

	private static final String PARAM_IDENTIFIER = "identifier";

	public List<Global> getAllGlobalDocuments(final String virtualCirca, final String interestGroup) throws SQLException, IOException
	{
		final List<Global> globals = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_GLOBAL_DOCUMENTS);
		return globals;
	}

	public Global getGlobalDocumentByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException
	{
		// in this table, some documents can be duplicated.
		final List<Global> globals = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_GLOBAL_DOCUMENT, Collections.singletonMap(PARAM_IDENTIFIER, identifier));

		if(globals == null || globals.size() < 1)
		{
			return null;
		}
		else if(globals.size() == 1)
		{
			// best case, only one document found with the identifier
			return globals.get(0);
		}
		else
		{
			Global lastUpdated = null;
			
			// search for the newest
			for(Global g : globals)
			{
				if(lastUpdated == null || Long.parseLong(g.getModified()) > Long.parseLong(lastUpdated.getModified()))
				{
					lastUpdated = g;
				}
			}
			
			return lastUpdated;
		}



	}

}
