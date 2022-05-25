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
package eu.cec.digit.circabc.web.repository;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrap a Category Header for the Client Side.
 *
 * @author Yanick Pignot
 * @author Stephane Clinckart
 */
public class CategoryHeaderNode extends NavigableNode {

    private static final long serialVersionUID = 4045045930595760474L;

    /**
     * The corresponding circabc root node
     */
    private CircabcRootNode circabcRootNode = null;

    /**
     * the list of header (categories Header)
     */
    private List<NavigableNode> headersNode = null;

    private List<NavigableNode> externalLinks = null;

    private String headerCleanPath = null;

    private String path = null;

    public CategoryHeaderNode(final NodeRef nodeRef) {
        this(nodeRef, null);
    }

    public CategoryHeaderNode(final NodeRef nodeRef, final CircabcRootNode circabcRootNode) {
        super(nodeRef);
        this.circabcRootNode = circabcRootNode;

        if (!getNavigableNodeType().equals(NavigableNodeType.CATEGORY_HEADER)) {
            throw new IllegalArgumentException("NodeRef must be a Circabc Header. It's node type must: "
                    + NavigableNodeType.CATEGORY_HEADER.getComparatorQName());
        }
    }

    public List<NavigableNode> getExternalLinks() {
        return externalLinks;
    }

    @Override
    public List<NavigableNode> getNavigableChilds() {
        if (headersNode == null) {
            if (headerCleanPath == null) {
                headerCleanPath = getPath()
                        .replaceAll("\\{http://www\\.alfresco\\.org/model/content/1\\.0\\}", "cm:");
            }

            if (path == null) {
                path = "+PARENT:\"" + getManagementService().getCircabcNodeRef() + "\" +PATH:\""
                        + headerCleanPath + "/*\"";
            }

            final List<NodeRef> categoriesNodeRef = WebClientHelper
                    .getNodesFromPath(path, getCircabcServices().getNonSecuredSearchService());

            headersNode = new ArrayList<>();
            externalLinks = new ArrayList<>();

            final CategoryHeaderNode categoryHeaderNode = this;
            final RetryingTransactionHelper txnHelper = getServiceRegistry().getTransactionService()
                    .getRetryingTransactionHelper();

            final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                public Integer execute() throws Throwable {
                    for (final NodeRef cat : categoriesNodeRef) {
                        if (getCircabcServices().getNonSecuredNodeService().exists(cat)) {
                            // Secure the list of categories. No other kind of spaces can be returned
                            if (getCircabcServices().getNonSecuredNodeService()
                                    .hasAspect(cat, CircabcModel.ASPECT_CATEGORY)) {
                                headersNode.add(new CategoryNode(cat, categoryHeaderNode));
                            } else if (getCircabcServices().getNonSecuredNodeService()
                                    .hasAspect(cat, DocumentModel.ASPECT_URLABLE)) {
                                externalLinks.add(new ExternalLinkNode(cat, categoryHeaderNode));
                            }
                        }
                    }
                    return null;
                }
            };
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                public Object doWork() {
                    return txnHelper.doInTransaction(callback, true, false);
                }
            }, AuthenticationUtil.getSystemUserName());
        }

        return headersNode;
    }

    @Override
    public NavigableNode getNavigableParent() {
        if (circabcRootNode == null) {
            circabcRootNode = new CircabcRootNode(getManagementService().getCircabcNodeRef());
        }
        return circabcRootNode;
    }

    @Override
    public void resetCache() {
        headersNode = null;
        externalLinks = null;
    }

}
