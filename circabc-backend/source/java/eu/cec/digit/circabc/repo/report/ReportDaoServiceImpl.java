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
package eu.cec.digit.circabc.repo.report;

import eu.cec.digit.circabc.service.report.ReportDaoService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.*;

/** @author beaurpi */
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
    private static final Log logger = LogFactory.getLog(ReportDaoServiceImpl.class);
    private SqlSessionTemplate sqlSessionTemplate = null;
    private Integer idContent;

    private Integer idCmObject;

    private Integer idVersionHistory;

    private Integer typeQnameId;

    private Integer storeID;

    private Integer propQnameId;

    private Integer versionStoreID;

    private void init() {
        if (idContent == null) {
            idContent = getQNameID(CONTENT, HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0);
            idCmObject = getQNameID(CMOBJECT, HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0);
            idVersionHistory = getQNameID(VERSION_HISTORY, HTTP_WWW_ALFRESCO_ORG_MODEL_VERSIONSTORE_2_0);

            typeQnameId =
                    getQNameID(INVITED_INTEREST_GROUP, HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0);
            propQnameId = getQNameID(IGNODEREF, HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0);
            storeID = getStoreID(WORKSPACE, SPACES_STORE);
            versionStoreID = getStoreID(WORKSPACE, VERSION_STORE);
        }
    }

    public Integer queryDbForNumberOfDocuments() {

        init();
        ContentNumberParametersDAO parameter =
                new ContentNumberParametersDAO(idContent, versionStoreID);
        return (Integer)
                sqlSessionTemplate.selectOne(CIRCABC_REPORTING_SELECT_COUNT_DOCUMENTS, parameter);
    }

    @Override
    public List<NodeRef> getAvailibleShareSpaces(NodeRef igNodeRef) {
        init();
        if (typeQnameId == null || propQnameId == null || storeID == null) {
            // try to init again and return empty list
            typeQnameId =
                    getQNameID(INVITED_INTEREST_GROUP, HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0);
            propQnameId = getQNameID(IGNODEREF, HTTP_WWW_CC_CEC_CIRCABC_MODEL_SHARESPACE_1_0);
            storeID = getStoreID(WORKSPACE, SPACES_STORE);
            return Collections.emptyList();
        }
        ShareSpaceQueryParameter parameter =
                new ShareSpaceQueryParameter(typeQnameId, propQnameId, storeID, igNodeRef.toString());
        List<NodeRef> result = new ArrayList<>();
        final List<String> selectList =
                (List<String>)
                        sqlSessionTemplate.selectList(CIRCABC_REPORTING_SELECT_SHARED_SPACES_BY_IG, parameter);
        for (String value : selectList) {
            result.add(new NodeRef(value));
        }
        return result;
    }

    private Integer getQNameID(String localName, String uri) {
        Integer result = 0;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(LOCAL_NAME, localName);
        paramMap.put(URI, uri);
        result =
                (Integer)
                        sqlSessionTemplate.selectOne(
                                CIRCABC_REPORTING_SELECT_QNAME_ID_BY_LOCAL_NAME_AND_URI, paramMap);
        return result;
    }

    private Integer getStoreID(String protocol, String identifier) {
        Integer result = 0;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(PROTOCOL2, protocol);
        paramMap.put(IDENTIFIER2, identifier);
        result =
                (Integer)
                        sqlSessionTemplate.selectOne(
                                CIRCABC_REPORTING_SELECT_STORE_ID_BY_PROTOCOL_PROTOCOL, paramMap);
        return result;
    }

    /** @param sqlSessionTemplate the sqlSessionTemplate to set */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
}
