/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.report;

import io.swagger.model.db.ContentNumberParametersDAO;
import io.swagger.model.db.ShareSpaceQueryParameter;
import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Default MyBatis-backed implementation of {@link ReportDaoService}.
 *
 * <p>This service issues reporting queries directly against the Alfresco database through a {@link
 * SqlSessionTemplate}, bypassing the higher-level Alfresco repository APIs for performance. It is
 * used to gather statistics for CIRCABC reports, such as the total number of stored documents and
 * the list of shared spaces available to a given Interest Group.
 *
 * <p>Because the queries rely on the numeric identifiers Alfresco assigns to qualified names
 * (QNames) and content stores, these identifiers are resolved lazily on first use (see {@link
 * #init()}) and cached in instance fields to avoid repeated lookups.
 *
 * @author beaurpi
 */
public class ReportDaoServiceImpl implements ReportDaoService {

  private static final String PROTOCOL2 = "protocol";
  private static final String IDENTIFIER2 = "identifier";
  private static final String CIRCABC_REPORTING_SELECT_QNAME_ID_BY_LOCAL_NAME_AND_URI =
    "CircabcReporting.select_qname_id_by_local_name__and_uri";
  private static final String CIRCABC_REPORTING_SELECT_SHARED_SPACES_BY_IG =
    "CircabcReporting.select_shared_spaces_by_ig";
  private static final String CIRCABC_REPORTING_SELECT_COUNT_DOCUMENTS =
    "CircabcReporting.select_count_documents";
  private static final String CIRCABC_REPORTING_SELECT_STORE_ID_BY_PROTOCOL_PROTOCOL =
    "CircabcReporting.select_store_id_by_protocol_protocol";
  private static final String INVITED_INTEREST_GROUP = "invitedInterestGroup";
  private static final String HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0 =
    "http://www.cc.cec/circabc/model/sharespace/1.0";
  private static final String IGNODEREF = "ignoderef";
  private static final String URI = "uri";
  private static final String LOCAL_NAME = "localName";
  private static final String HTTP_WWW_ALFRESCO_ORG_MODEL_VERSIONSTORE_2_0 =
    "http://www.alfresco.org/model/versionstore/2.0";
  private static final String VERSION_HISTORY = "versionHistory";
  private static final String CMOBJECT = "cmobject";
  private static final String HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0 =
    "http://www.alfresco.org/model/content/1.0";
  private static final String CONTENT = "content";
  private static final String SPACES_STORE = "SpacesStore";
  private static final String VERSION_STORE = "version2Store";
  private static final String WORKSPACE = "workspace";
  /* (non-Javadoc)
   * @see eu.cec.digit.circabc.service.report.ReportDaoService#queryDbForNumberOfDocuments()
   */

  private SqlSessionTemplate sqlSessionTemplate = null;
  /** Cached QName id of the {@code cm:content} type, used to count content nodes. */
  private Integer idContent;

  @SuppressWarnings("unused")
  private Integer idCmObject;

  @SuppressWarnings("unused")
  private Integer idVersionHistory;

  /** Cached QName id of the {@code sharespace:invitedInterestGroup} type. */
  private Integer typeQnameId;

  /** Cached numeric id of the {@code workspace://SpacesStore} content store. */
  private Integer storeID;

  /** Cached QName id of the {@code sharespace:ignoderef} property. */
  private Integer propQnameId;

  /** Cached numeric id of the {@code workspace://version2Store} content store. */
  private Integer versionStoreID;

  /**
   * Lazily resolves and caches the QName and store identifiers required by the reporting queries.
   *
   * <p>The lookups are performed only once: subsequent calls are no-ops as long as {@link
   * #idContent} has already been resolved.
   */
  private void init() {
    if (idContent == null) {
      idContent = getQNameID(CONTENT, HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0);
      idCmObject = getQNameID(
        CMOBJECT,
        HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0
      );
      idVersionHistory = getQNameID(
        VERSION_HISTORY,
        HTTP_WWW_ALFRESCO_ORG_MODEL_VERSIONSTORE_2_0
      );

      typeQnameId = getQNameID(
        INVITED_INTEREST_GROUP,
        HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0
      );
      propQnameId = getQNameID(
        IGNODEREF,
        HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0
      );
      storeID = getStoreID(WORKSPACE, SPACES_STORE);
      versionStoreID = getStoreID(WORKSPACE, VERSION_STORE);
    }
  }

  /**
   * Counts the total number of content documents held in the version store.
   *
   * @return the number of document nodes recorded in the {@code version2Store}, or {@code null} if
   *     the query returns no result
   */
  public Integer queryDbForNumberOfDocuments() {
    init();
    ContentNumberParametersDAO parameter = new ContentNumberParametersDAO(
      idContent,
      versionStoreID
    );
    return (Integer) sqlSessionTemplate.selectOne(
      CIRCABC_REPORTING_SELECT_COUNT_DOCUMENTS,
      parameter
    );
  }

  /**
   * Returns the shared spaces that are available to (i.e. invited into) the given Interest Group.
   *
   * <p>If the required QName or store identifiers cannot be resolved, the method attempts to resolve
   * them once more and returns an empty list rather than failing.
   *
   * @param igNodeRef the {@link NodeRef} of the Interest Group whose shared spaces are requested
   * @return the list of shared space {@link NodeRef}s available to the Interest Group; an empty list
   *     if none are found or the identifiers could not be resolved
   */
  @Override
  public List<NodeRef> getAvailibleShareSpaces(NodeRef igNodeRef) {
    init();
    if (typeQnameId == null || propQnameId == null || storeID == null) {
      // try to init again and return empty list
      typeQnameId = getQNameID(
        INVITED_INTEREST_GROUP,
        HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0
      );
      propQnameId = getQNameID(
        IGNODEREF,
        HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0
      );
      storeID = getStoreID(WORKSPACE, SPACES_STORE);
      return Collections.emptyList();
    }
    ShareSpaceQueryParameter parameter = new ShareSpaceQueryParameter(
      typeQnameId,
      propQnameId,
      storeID,
      igNodeRef.toString()
    );
    List<NodeRef> result = new ArrayList<>();
    final List<String> selectList = sqlSessionTemplate.selectList(
      CIRCABC_REPORTING_SELECT_SHARED_SPACES_BY_IG,
      parameter
    );
    for (String value : selectList) {
      result.add(new NodeRef(value));
    }
    return result;
  }

  /**
   * Resolves the numeric database id of a qualified name (QName).
   *
   * @param localName the local part of the QName (e.g. {@code content})
   * @param uri the namespace URI of the QName
   * @return the numeric QName id, or {@code null} if no matching QName exists
   */
  private Integer getQNameID(String localName, String uri) {
    Integer result = 0;
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put(LOCAL_NAME, localName);
    paramMap.put(URI, uri);
    result = (Integer) sqlSessionTemplate.selectOne(
      CIRCABC_REPORTING_SELECT_QNAME_ID_BY_LOCAL_NAME_AND_URI,
      paramMap
    );
    return result;
  }

  /**
   * Resolves the numeric database id of a content store identified by its protocol and identifier.
   *
   * @param protocol the store protocol (e.g. {@code workspace})
   * @param identifier the store identifier (e.g. {@code SpacesStore})
   * @return the numeric store id, or {@code null} if no matching store exists
   */
  private Integer getStoreID(String protocol, String identifier) {
    Integer result = 0;
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put(PROTOCOL2, protocol);
    paramMap.put(IDENTIFIER2, identifier);
    result = (Integer) sqlSessionTemplate.selectOne(
      CIRCABC_REPORTING_SELECT_STORE_ID_BY_PROTOCOL_PROTOCOL,
      paramMap
    );
    return result;
  }

  /** @param sqlSessionTemplate the sqlSessionTemplate to set */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }
}
