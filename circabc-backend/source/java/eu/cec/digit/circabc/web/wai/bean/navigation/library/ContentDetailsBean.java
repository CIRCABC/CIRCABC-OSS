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
package eu.cec.digit.circabc.web.wai.bean.navigation.library;

import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.InterestGroupBean;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import net.sf.acegisecurity.Authentication;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.content.DocumentDetailsDialog;
import org.alfresco.web.bean.ml.MultilingualManageDialog;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs the navigation inside the content details in the Library Service
 *
 * @author yanick pignot
 */
public class ContentDetailsBean extends InterestGroupBean {

    public static final String BEAN_NAME = "LibContentDetailsBean";
    public static final String JSP_NAME = "content-details.jsp";
    public static final String WEB_INF_CLASSES_SUFFIX = "WEB-INF/classes/";
    public static final String PDF_RENDITION_DEFINITION = "PDFRenditionDefinition";
    public static final QName RENDITION_NAME = QName.createQName(
            NamespaceService.CONTENT_MODEL_1_0_URI, PDF_RENDITION_DEFINITION);
    public static final String MSG_PAGE_TITLE = "details_of";
    public static final String MSG_PAGE_DESCRIPTION = "documentdetails_description";
    public static final String MSG_PAGE_ICON_ALT = "library_content_details_icon_tooltip";
    public static final String MSG_BROWSER_TITLE = "title_manage_content_details";
    /**
     *
     */
    private static final long serialVersionUID = -1967164575499663894L;
    private static final Log logger = LogFactory.getLog(ContentDetailsBean.class);
    /**
     * Delegate the document details bean ... and not inherit ...
     */
    private DocumentDetailsDialog documentDetailsDialog;
    private MultilingualManageDialog multilingualManageDialog;

    /**
     * The ml content service referece
     */
    private transient MultilingualContentService multilingualContentService;

    private ExternalRepositoriesManagementService externalRepositoriesManagementService = null;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_CONTENT;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "library/" + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION, getCurrentNode().getName());
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE) + " \'" + getCurrentNode().getName() + '\'';
    }

    public String getPageIcon() {
        return "/images/icons/details_large.gif";
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    @Override
    public String getBrowserTitle() {
        String titleOrName = null;

        final String propertyTitle = ContentModel.PROP_TITLE.toString();
        final Object title = getCurrentNode().getProperties().get(propertyTitle);
        if (title != null) {
            ESAPI.encoder().encodeForHTML(titleOrName = (title.toString().length() > 0 ? title.toString()
                    : getCurrentNode().getName()));
        }

        return translate(MSG_BROWSER_TITLE, titleOrName);
    }

    public void init(Map<String, String> parameters) {
        getBrowseBean().setupContentAction(getNavigator().getCurrentNodeId(), true);

        NodeRef currentNode = getNavigator().getCurrentNode().getNodeRef();

        // fix for DIGIT-CIRCABC-2139
        if (super.getNodeService().getType(currentNode).equals(ContentModel.TYPE_CONTENT)) {
            Serializable property = super.getNodeService()
                    .getProperty(currentNode, ContentModel.PROP_AUTO_VERSION_PROPS);
            if (property != null && property.equals(true)) {
                Authentication fullAuthentication = AuthenticationUtil.getFullAuthentication();
                try {
                    AuthenticationUtil.setRunAsUserSystem();
                    super.getNodeService()
                            .setProperty(currentNode, ContentModel.PROP_AUTO_VERSION_PROPS, false);
                    AuthenticationUtil.clearCurrentSecurityContext();
                } finally {
                    AuthenticationUtil.setFullAuthentication(fullAuthentication);
                }
            }
        }
    }

    @Override
    public Node getCurrentNode() {
        return getBrowseBean().getDocument();
    }

    @Override
    public void restored() {

        Node document = getBrowseBean().getDocument();
        if (document != null) {
            document.reset();
        }
    }

    public ActionsListWrapper getActionList() {
        if (!getDocument().isLocked()) {
            return new ActionsListWrapper(getDocument(), "doc_details_actions_wai");
        } else {
            return null;
        }
    }

    public boolean isPublishedInExternalRepository() {
        return externalRepositoriesManagementService.wasPublishedTo(null,
                getCurrentNode().getNodeRef().toString());
    }

    public Map<String, Map<String, String>> getRepositoriesInfo() {

        Map<String, Map<String, String>> data = externalRepositoriesManagementService.
                getRepositoryDataForDocument(getCurrentNode().getNodeRef().toString());

        return data;
    }

    public void setDocumentUrl(String documentUrl) {
    }

    public Node getDocument() {
        return this.getCurrentNode();
    }

    public boolean isDocumentLocked() {
        return getDocument().isLocked();
    }

    /**
     * @return the working copy document Node for this document if found and the current has
     * permission or null if not
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#getWorkingCopyDocument()
     */
    public Node getWorkingCopyDocument() {
        return getDocumentDetailsDialog().getWorkingCopyDocument();
    }

    /**
     * @return list of objects representing the translations of the current document
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#getTranslations()
     */
    @SuppressWarnings("unchecked")
    public List<MapNode> getTranslations() {
        return (List<MapNode>) getMultilingualManageDialog().getTranslations();
    }

    /**
     * @return the ml container of the document this bean is currently representing
     */
    public Node getDocumentMlContainer() {
        Node currentNode = getDocument();

        if (ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(currentNode.getType())) {
            return currentNode;
        } else {
            NodeRef nodeRef = currentNode.getNodeRef();

            return new Node(getMultilingualContentService().getTranslationContainer(nodeRef));
        }
    }

    /**
     * @return true if the current node is a multilingual document
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#isMultilingual()
     */
    public boolean isMultilingual() {
        return getDocumentDetailsDialog().isMultilingual();
    }


    /**
     * @return true if the current node is versonable
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#isVersionable()
     */
    public boolean isVersionable() {
        return getDocumentDetailsDialog().isVersionable();
    }

    /**
     * @return true if the current node is a working copy
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#isWorkingCopy()
     */
    public boolean isWorkingCopy() {
        return getDocumentDetailsDialog().isWorkingCopy();
    }


    /**
     * Applies the versionable aspect to the current document
     *
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#applyVersionable()
     */
    public void applyVersionable() {
        getDocumentDetailsDialog().applyVersionable();
    }

    /**
     * @return List of objects representing the versions of the current document
     * @see org.alfresco.web.bean.content.DocumentDetailsDialog#getVersionHistory()
     */
    @SuppressWarnings("unchecked")
    public List<MapNode> getVersionHistory() {
        return (List<MapNode>) getDocumentDetailsDialog().getVersionHistory();
    }

    /**
     * @return the documentDetailsDialog
     */
    protected final DocumentDetailsDialog getDocumentDetailsDialog() {
        if (documentDetailsDialog == null) {
            documentDetailsDialog = Beans.getDocumentDetailsDialog();
        }
        return documentDetailsDialog;
    }

    /**
     * @param documentDetailsDialog the documentDetailsDialog to set
     */
    public final void setDocumentDetailsDialog(DocumentDetailsDialog documentDetailsDialog) {
        this.documentDetailsDialog = documentDetailsDialog;
    }

    /**
     * @return the multilingualContentService
     */
    protected final MultilingualContentService getMultilingualContentService() {
        if (this.multilingualContentService == null) {
            this.multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
        }

        return multilingualContentService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public final void setMultilingualContentService(
            MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    protected final MultilingualManageDialog getMultilingualManageDialog() {
        if (multilingualManageDialog == null) {
            multilingualManageDialog = Beans.getMultilingualManageDialog();
        }
        return multilingualManageDialog;
    }

    public final void setMultilingualManageDialog(MultilingualManageDialog multilingualManageDialog) {
        this.multilingualManageDialog = multilingualManageDialog;
    }

    /**
     * Sets the value of the externalRepositoriesManagementService
     *
     * @param externalRepositoriesManagementService the externalRepositoriesManagementService to set.
     */
    public void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        this.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }
}
