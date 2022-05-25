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

import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Document;

/**
 * Service layer accessing persistent circa <b>Document</b> entities directly
 *
 * @author Yanick Pignot
 */
public interface DocumentDao
{
	/**
	 * Get all documents of an interest group
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Document> getAllDocuments(final String virtualCirca, final String interestGroup) throws SQLException, IOException;
	
	/**
	 * Get all documents of an interest group by type
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param the type				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Document> getAllDocumentsByType(final String virtualCirca, final String interestGroup, final String type) throws SQLException, IOException;

	/**
	 * Get a specified document by its identifier
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param identifier			    The unique identifier of the Document
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract Document getDocumentByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException;


	/**
	 * Get all documents like identifier. Don't forget to place the %.
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param identifier			    The unique identifier of the Document
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<Document> getDocumentLikeIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException;

	/**
	 * Get all documents of an interest group in a secified pool
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Document> getDocumentsFromPool(final String virtualCirca, final String interestGroup, final String parentUrl, final String docPool) throws SQLException, IOException;


}
