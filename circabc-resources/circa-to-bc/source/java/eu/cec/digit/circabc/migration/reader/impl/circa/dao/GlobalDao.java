/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Global;

/**
 * Service layer accessing persistent circa <b>Global</b> entities directly
 *
 * @author Yanick Pignot
 */
public interface GlobalDao
{
	/**
	 * Get all global documents of an interest group
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Global> getAllGlobalDocuments(final String virtualCirca, final String interestGroup) throws SQLException, IOException;

	/**
	 * Get a specified global document by its identifier
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param iddentifier			    The unique identifier of the Document
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract Global getGlobalDocumentByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException;
}
