/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog.system;

import eu.cec.digit.circabc.model.SystemMessageModel;
import eu.cec.digit.circabc.web.Beans;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade to read from the System Message node. The bean is initialized as a managed application
 * bean.
 *
 * @author makz
 */
public class SystemMessageBean {

    private static final Log logger = LogFactory.getLog(SystemMessageBean.class);

    //***********************************************************************
    //                                                      GETTER AND SETTER
    //***********************************************************************
    transient private NodeService nodeService;
    transient private SearchService searchService;
    private NodeRef messageNodeRef;

    public boolean isShowMessage() {
        boolean result = false;
        try {
            NodeRef systemMessageNode = this.getSystemMessageNode();
            if (systemMessageNode != null) {
                Object msgEnabled = this.getNodeService().getProperty(
                        systemMessageNode,
                        SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED);
                if (msgEnabled != null && msgEnabled.getClass().equals(Boolean.class)) {
                    return (Boolean) msgEnabled;
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Cannot get value for isShowMessage.", e);
            }
        }
        return result;
    }

    public String getMessage() {
        String result = "";
        try {
            NodeRef systemMessageNode = this.getSystemMessageNode();
            if (systemMessageNode != null) {
                Object msgText = this.getNodeService().getProperty(
                        systemMessageNode,
                        SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT);
                if (msgText != null) {
                    Whitelist basicsAndStyle = new Whitelist();
                    basicsAndStyle.addTags("p", "span", "strong", "b", "i", "u", "br", "sub", "sup", "a");
                    basicsAndStyle.addAttributes(":all", "style", "href");
                    result = Jsoup.clean(msgText.toString(), basicsAndStyle);
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Cannot get value for getMessage.");
            }
        }
        return result;
    }

    public void updateProperties(boolean showMessage, String message) {
        final Map<QName, Serializable> properties = new HashMap<>(2);
        properties.put(SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED, showMessage);
        properties.put(SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT, message);
        this.getNodeService().setProperties(this.getSystemMessageNode(), properties);
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    //***********************************************************************
    //                                                         PRIVATE HELPER
    //***********************************************************************

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private NodeRef getSystemMessageNode() {
        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();
        List<ChildAssociationRef> children = nodeService.getChildAssocs(rootRef, Collections.singleton(SystemMessageModel.TYPE_SYSTEMMESSAGE));
        if (children.size() == 0) {
            try {
                // there is no system message node, create it
                ChildAssociationRef childAssoc = getNodeService().createNode(
                        rootRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(SystemMessageModel.CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
                                "System Message"),
                        SystemMessageModel.TYPE_SYSTEMMESSAGE,
                        null);
                messageNodeRef = childAssoc.getChildRef();
            } catch (Exception e) {
                logger.error("Error when create system message,  user " + AuthenticationUtil.getFullyAuthenticatedUser(), e);
            }
        } else {
            messageNodeRef = children.get(0).getChildRef();
        }
        return messageNodeRef;
    }

    private NodeRef getSystemMessageNodeLucene() {
        if (messageNodeRef == null) {

            ChildAssociationRef childAssoc = null;

            // search for the system message node
            NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();
            String query = String.format("PARENT:\"%s\" AND TYPE:\"%s\"",
                    rootRef.toString(),
                    SystemMessageModel.TYPE_SYSTEMMESSAGE);
            ResultSet results = null;
            try {
                results = getSearchService().query(
                        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        SearchService.LANGUAGE_LUCENE,
                        query);

                if (results.length() > 0) {
                    // get the system message node from the results
                    childAssoc = results.getChildAssocRef(0);
                    messageNodeRef = childAssoc.getChildRef();
                } else {
                    try {
                        // there is no system message node, create it
                        childAssoc = getNodeService().createNode(
                                rootRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(SystemMessageModel.CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
                                        "System Message"),
                                SystemMessageModel.TYPE_SYSTEMMESSAGE,
                                null);
                        messageNodeRef = childAssoc.getChildRef();
                    } catch (Exception ade) {
                        //user has no right to create the node, do nothing
                        logger.warn("User " + AuthenticationUtil.getFullyAuthenticatedUser()
                                + " has no right to access the System Message Node", ade);
                    }
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }
        return messageNodeRef;
    }
}
