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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.DocumentDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Document;

/**
 * Implementation of the circa document dao using ibatis sql mapper.
 *
 * @author Yanick Pignot
 */
public class DocumentDaoImpl implements DocumentDao
{
	private static final String QUERY_ALL_DOCUMENTS = "getAllLibraryDocuments";
	private static final String QUERY_ALL_DOCUMENTS_BY_TYPE = "getAllLibraryDocumentsByType";
	private static final String QUERY_DOCUMENTS_FROM_POOL = "getLibraryDocumentFromPool";
	private static final String QUERY_SPECIFIC_DOCUMENT = "getLibraryDocument";
	private static final String QUERY_DOCUMENTS_LIKE = "getLibraryDocumentWhereIdentifierLike";

	private static final String PARAM_IDENTIFIER = "identifier";
	private static final String PARAM_PARENT_URL = "parentUrl";
	private static final String PARAM_DOC_POOL = "docPool";
	private static final String PARAM_TYPE = "type";

	public List<Document> getAllDocuments(final String virtualCirca, final String interestGroup) throws SQLException, IOException
	{
		final List<Document> documents = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_DOCUMENTS);
		return documents;
	}
	
	public List<Document> getAllDocumentsByType(final String virtualCirca, final String interestGroup, final String type) throws SQLException, IOException
	{
		final List<Document> documents = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_DOCUMENTS_BY_TYPE, Collections.singletonMap(PARAM_TYPE, type));
		return documents;
	}

	public Document getDocumentByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException
	{
		final Document document = IbatisDaoManager.querySingle(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_DOCUMENT, Collections.singletonMap(PARAM_IDENTIFIER, identifier));
		return document;
	}

	public List<Document> getDocumentLikeIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException
	{
		final List<Document> documents = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_DOCUMENTS_LIKE, Collections.singletonMap(PARAM_IDENTIFIER, identifier));
		return documents;
	}

	public List<Document> getDocumentsFromPool(final String virtualCirca, final String interestGroup, final String parentUrl, final String docPool) throws SQLException, IOException
	{
		Map<String, String> params = new HashMap<String, String>(2);
		params.put(PARAM_PARENT_URL, parentUrl);
		params.put(PARAM_DOC_POOL, docPool);
		final List<Document> documents = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_DOCUMENTS_FROM_POOL, params);
		return documents;
	}

}
