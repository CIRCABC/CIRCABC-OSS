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
package eu.cec.digit.circabc.web.wai.bean.navigation.news;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.servlet.ExternalAccessServlet;
import eu.cec.digit.circabc.web.servlet.SimpleDownloadServlet;
import eu.cec.digit.circabc.web.wai.bean.navigation.NewsGroupBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.ResolverHelper;
import eu.cec.digit.circabc.web.wai.dialog.customization.EditNavigationPreference;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs the navigation inside the posts of the newsgroup service
 *
 * @author yanick pignot
 */
public class PostBean extends NewsGroupBean {

    public static final String JSP_NAME = "post.jsp";
    public static final String BEAN_NAME = "NewsPostBean";
    public static final String MSG_PAGE_TITLE = "newsgroups_post_title";
    public static final String MSG_PAGE_DESCRIPTION = "newsgroups_post_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "newsgroups_post_icon_tooltip";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499987493L;
    // Added for document preview
    // Resolves the onclick call and parameters for the document preview
    public NodePropertyResolver resolverPreview = new NodePropertyResolver() {

        private static final long serialVersionUID = 9133669045868747541L;

        public Object get(Node node) {
            return "previewDocument(\'" +
                    node.getNodeRef().toString() +
                    "\', \'" +
                    SimpleDownloadServlet.generateBrowseURL(node.getNodeRef(),
                            node.getName()) + "\', \'" +
                    ExternalAccessServlet.generateRelativeWaiExternalURL(
                            "browse", node.getNodeRef().getId()) + "\');";
        }
    };
    private Node currentNode = null;
    private String content;
    private String referenceDate;
    private String referenceCreator;
    private String referenceId;
    private List<Node> attachments;
    private transient AttachementService attachementService;

    public void init(Map<String, String> parameters) {
        // equality test and not equals. If the instance is different, remove cached data
        if (currentNode == null || getNavigator().getCurrentNode() != currentNode) {
            currentNode = getNavigator().getCurrentNode();
            content = (String) getBrowseBean().resolverContent.get(getNavigator().getCurrentNode());
            referenceDate = null;
            referenceCreator = null;
            referenceId = null;
            attachments = null;
            if (currentNode.hasAspect(ContentModel.ASPECT_REFERENCING)) {
                final List<AssociationRef> assocs = getNodeService()
                        .getTargetAssocs(currentNode.getNodeRef(), ContentModel.ASSOC_REFERENCES);

                if (assocs != null && assocs.size() > 0) {
                    final NodeRef referenceRef = assocs.get(0).getTargetRef();
                    final Map<QName, Serializable> props = getNodeService().getProperties(referenceRef);
                    final Date created = (Date) props.get(ContentModel.PROP_CREATED);
                    referenceDate = WebClientHelper
                            .formatLocalizedDate(created, FacesContext.getCurrentInstance(), true, true);
                    referenceCreator = (String) props.get(ContentModel.PROP_CREATOR);
                    referenceId = referenceRef.getId();
                }
            }

            if (currentNode.hasAspect(DocumentModel.ASPECT_ATTACHABLE)) {
                final List<NodeRef> attachmentRefs = getAttachementService()
                        .getAttachements(currentNode.getNodeRef());
                attachments = new ArrayList<>(attachmentRefs.size());

                final NavigationPreference configuration = getNavigationPreferencesService()
                        .getServicePreference(
                                getCurrentNode().getNodeRef(),
                                NavigationPreferencesService.NEWSGROUP_SERVICE,
                                NavigationPreferencesService.FORUM_TYPE);

                ServiceConfig serviceConfig = configuration.getService();
                String previewAction = configuration.getPreviewAction();

                for (final NodeRef attach : attachmentRefs) {
                    final MapNode node = new MapNode(attach);
                    final QName type = node.getType();

                    if (getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
                        node.addPropertyResolver(ResolverHelper.SMALL_ICON, getBrowseBean().resolverSmallIcon);
                        node.put(ResolverHelper.IS_CONTAINER, Boolean.TRUE);
                    } else {
                        node.addPropertyResolver(ResolverHelper.FILE_TYPE16,
                                getBrowseBean().resolverFileType16);
                        // Migration 3.1 -> 3.4.6 - 11/01/2012 - Resolve URLs with tightened content type

                        if (previewAction == null) { // old IGs & ISs
                            node.addPropertyResolver(ResolverHelper.DOWNLOAD_URL,
                                    getBrowseBean().resolverAttachmentDownload);
                        } else if (
                                "custom-action-list-newsgroup-forum".equals(serviceConfig.getActionConfigName()) &&
                                        (EditNavigationPreference.OPTION_PREVIEW.equals(
                                                previewAction)/* || previewAction == null*/)) { /* Check previewAction == null to enable preview as default */

                            node.addPropertyResolver(ResolverHelper.ON_CLICK, resolverPreview);
                        } else {
                            node.addPropertyResolver(ResolverHelper.DOWNLOAD_URL,
                                    getBrowseBean().resolverAttachmentDownload);
                        }

                        node.put(ResolverHelper.IS_CONTAINER, Boolean.FALSE);
                    }

                    attachments.add(node);
                }
            }
        }
    }

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.NEWSGROUP_POST;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "news/" + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getPageIcon() {
        return "/images/icons/topic.gif";
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    public String getMessage() {
        return content;
    }

    public String getCreator() {
        return (String) getNavigator().getCurrentNode().getProperties()
                .get(ContentModel.PROP_CREATOR.toString());
    }

    public String getCreated() {
        final Date date = (Date) getNavigator().getCurrentNode().getProperties()
                .get(ContentModel.PROP_CREATED.toString());
        final String formatedDate = WebClientHelper
                .formatLocalizedDate(date, FacesContext.getCurrentInstance(), true, true);
        return formatedDate;
    }

    public final String getReferenceCreator() {
        return referenceCreator;
    }

    public final String getReferenceDate() {
        return referenceDate;
    }

    public final String getReferenceId() {
        return referenceId;
    }

    public boolean isReply() {
        return referenceId != null;
    }

    public boolean isAttachementAvailable() {
        return attachments != null && attachments.size() > 0;
    }

    public List<Node> getAttachements() {
        return attachments;
    }

    protected final AttachementService getAttachementService() {
        if (attachementService == null) {
            attachementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getAttachementService();
        }
        return attachementService;
    }

    public final void setAttachementService(AttachementService attachementService) {
        this.attachementService = attachementService;
    }
}
