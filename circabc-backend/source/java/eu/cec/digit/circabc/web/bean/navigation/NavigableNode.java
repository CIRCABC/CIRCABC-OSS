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
package eu.cec.digit.circabc.web.bean.navigation;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.servlet.ExternalAccessServlet;
import eu.cec.digit.circabc.web.servlet.SimpleDownloadServlet;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The base node representation of each Circabc navigable node type
 *
 * @author yanick pignot
 * @author Stephane Clinckart
 */
public abstract class NavigableNode extends MapNode implements Comparable<NavigableNode> {

    public static final String BEST_TITLE_RESOLVER_NAME = "bestTitle";
    public static final String TITLE_OR_NAME = "titleOrName";
    public static final String PARENT_ID = "parentId";
    public static final String ONCLICK_PREVIEW_BEHAVIOUR = "onClickBehaviour";
    public static final String ONCLICK_EDIT_IN_OFFICE = "editInOffice";
    public static final String NEW_TICKET_PREFIX = "/4bac";
    private static final long serialVersionUID = -8494188639400816217L;
    private static final String NAVIGABLE_PARENT_RESOLVER_NAME = "navigableParent";
    private static final String NAVIGABLE_CHILDS_RESOLVER_NAME = "navigableChilds";
    private static final String HAS_CHILDS_RESOLVER_NAME = "hasChilds";
    private static final String ALTERNATIVE_DOWNLOAD_URL = "alternativeDownloadUrl";
    private static final String ALTERNATIVE_BROWSE_URL = "alternativeBrowseUrl";
    private final NavigableNodeType navigableNodeType;
    public NodePropertyResolver resolverAlternativeDownload = new NodePropertyResolver() {
        private static final long serialVersionUID = -5583174774838740436L;

        public Object get(Node node) {
            return SimpleDownloadServlet.generateDownloadURL(node.getNodeRef(), node.getName());
        }
    };
    public NodePropertyResolver resolverAlternativeBrowse = new NodePropertyResolver() {

        private static final long serialVersionUID = 9133660645868747741L;

        public Object get(Node node) {
            return SimpleDownloadServlet.generateBrowseURL(node.getNodeRef(), node.getName());
        }
    };
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
    /**
     * Resolves the link to Edit in Office
     */
    public NodePropertyResolver resolverEditInOffice = new NodePropertyResolver() {

        private static final long serialVersionUID = 9133669045868847541L;

        private AuthenticationService authenticationService =
                getServiceRegistry().getAuthenticationService();

        public Object get(Node node) {

            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext()
                    .getRequest();

            ScriptNode scriptNode = new ScriptNode(node.getNodeRef(),
                    getServiceRegistry());

            String webdavUrl = scriptNode.getWebdavUrl();

            String ticket = authenticationService.getCurrentTicket();

            int posEndWebdav = webdavUrl.indexOf("/webdav") +
                    "/webdav".length();

            webdavUrl = webdavUrl.substring(0, posEndWebdav) +
                    NEW_TICKET_PREFIX + ticket.substring(
                    InMemoryTicketComponentImpl.
                            GRANTED_AUTHORITY_TICKET_PREFIX.length()) +
                    webdavUrl.substring(posEndWebdav,
                            webdavUrl.length());

            return "openDoc(\'" + request.getContextPath() + webdavUrl + "\');";
        }
    };
    /**
     * Resolver to get the title or the name if not exists.
     */
    public NodePropertyResolver resolverBestTitle = new NodePropertyResolver() {
        private static final long serialVersionUID = -8543304786599792888L;

        public Object get(final Node node) {
            final String title = (String) node.getProperties().get(ContentModel.PROP_TITLE.toString());

            if (title == null || title.trim().length() < 1) {
                return node.getName();
            } else {
                return title;
            }
        }
    };
    /**
     * Resolver to get the reference.
     */
    public NodePropertyResolver resolverReference = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279605454785L;

        public Object get(final Node node) {
            final String reference = (String) node.getProperties()
                    .get(DocumentModel.PROP_REFERENCE.toString());

            return reference;
        }
    };
    /**
     * Resolver to get the version label.
     */
    public NodePropertyResolver resolverVersionLabel = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279605454785L;

        public Object get(final Node node) {
            final String value = (String) node.getProperties()
                    .get(ContentModel.PROP_VERSION_LABEL.toString());

            return value;
        }
    };
    /**
     * Resolver to get the auto version.
     */
    public NodePropertyResolver resolverAutoVersion = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279605454785L;

        public Object get(final Node node) {
            final String value = (String) node.getProperties()
                    .get(ContentModel.PROP_AUTO_VERSION.toString());

            return value;
        }
    };
    /**
     * Resolver to get the language.
     */
    public NodePropertyResolver resolverLanguage = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279789454785L;

        public Object get(final Node node) {
            final String value = (String) node.getProperties()
                    .get(EventModel.PROP_EVENT_LANGUAGE.toString());

            return value;
        }
    };
    /**
     * Resolver to get the status.
     */
    public NodePropertyResolver resolverStatus = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856234605454785L;

        public Object get(final Node node) {
            final String value = (String) node.getProperties().get(DocumentModel.PROP_STATUS.toString());

            return value;
        }
    };
    /**
     * Resolver to get the issue date.
     */
    public NodePropertyResolver resolverIssueDate = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856223455454785L;

        public Object get(final Node node) {

            final Object value = node.getProperties().get(DocumentModel.PROP_ISSUE_DATE.toString());

            if (value != null && value instanceof Date) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                return simpleDateFormat.format(value);
            }
            return value;
        }
    };
    /**
     * Resolver to get the security ranking.
     */
    public NodePropertyResolver resolverSecurityRanking = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279111454785L;

        public Object get(final Node node) {
            final String value = (String) node.getProperties()
                    .get(DocumentModel.PROP_SECURITY_RANKING.toString());

            return value;
        }
    };
    /**
     * Resolver to get the expiration date.
     */
    public NodePropertyResolver resolverExpirationDate = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856279111454445L;

        public Object get(final Node node) {
            final Object value = node.getProperties().get(DocumentModel.PROP_EXPIRATION_DATE.toString());

            if (value != null && value instanceof Date) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                return simpleDateFormat.format(value);
            }

            return value;
        }
    };
    /**
     * Resolver to get the keyword. We may have one keyword or more than one stored in a list.
     */
    public NodePropertyResolver resolverKeyword = new NodePropertyResolver() {
        private static final long serialVersionUID = -5587856278711454785L;

        public Object get(final Node node) {
            final Serializable value = (Serializable) node.getProperties()
                    .get(DocumentModel.PROP_KEYWORD.toString());

            // If there are many keywords, concatenate them into one for sorting
            if (value instanceof Collection) {

                FacesContext fc = FacesContext.getCurrentInstance();
                NodeService nodeService = Services.getAlfrescoServiceRegistry(fc).getNodeService();

                StringBuilder titles = new StringBuilder(126);

                for (NodeRef nodeRef : (Collection<NodeRef>) value) {
                    String title = (String) nodeService.getProperty(nodeRef,
                            ContentModel.PROP_TITLE);
                    titles.append(title);
                }

                return titles.toString().trim().toLowerCase();
            }

            return value;
        }
    };
    boolean selected = false;
    public NodePropertyResolver resolverSelected = new NodePropertyResolver() {
        public Object get(final Node node) {
            return ((NavigableNode) node).selected;
        }
    };
    private transient ManagementService managementService;
    private transient CircabcServiceRegistry circabcServiceRegistry;
    private NodePropertyResolver resolverParentId = new NodePropertyResolver() {
        private static final long serialVersionUID = 9133660645868747741L;

        public Object get(final Node node) {
            FacesContext fc = FacesContext.getCurrentInstance();
            NodeService nodeService = Services.getAlfrescoServiceRegistry(fc).getNodeService();
            ChildAssociationRef assoc = nodeService.getPrimaryParent(node.getNodeRef());
            return assoc.getParentRef().getId();
        }
    };
    private NodePropertyResolver resolverNavigableParent = new NodePropertyResolver() {

        private static final long serialVersionUID = -5583174774838740436L;

        public Object get(final Node node) {
            return ((NavigableNode) node).getNavigableParent();
        }
    };
    private NodePropertyResolver resolverNavigableChilds = new NodePropertyResolver() {

        private static final long serialVersionUID = 9133660645868747741L;

        public Object get(final Node node) {
            return ((NavigableNode) node).getNavigableChilds();
        }
    };
    private NodePropertyResolver resolverHasChilds = new NodePropertyResolver() {

        private static final long serialVersionUID = -7940464414075882820L;

        public Object get(final Node node) {
            final List<NavigableNode> childs = ((NavigableNode) node).getNavigableChilds();

            return childs != null && childs.size() > 0;
        }
    };

    public NavigableNode(final NodeRef nodeRef) {
        super(nodeRef);
        this.navigableNodeType = AspectResolver.resolveType(this);
        addResolvers();
    }

    protected NavigableNode(final NodeRef nodeRef, final NavigableNodeType trustedType) {
        super(nodeRef);
        this.navigableNodeType = trustedType;
        addResolvers();
    }

    public NavigableNode(final NodeRef nodeRef, final NodeService nodeService,
                         final Map<QName, Serializable> props) {
        super(nodeRef, nodeService, props);
        this.navigableNodeType = AspectResolver.resolveType(this);
        addResolvers();
    }

    private void addResolvers() {
        this.addPropertyResolver(NAVIGABLE_PARENT_RESOLVER_NAME, resolverNavigableParent);
        this.addPropertyResolver(NAVIGABLE_CHILDS_RESOLVER_NAME, resolverNavigableChilds);
        this.addPropertyResolver(HAS_CHILDS_RESOLVER_NAME, resolverHasChilds);
        this.addPropertyResolver(BEST_TITLE_RESOLVER_NAME, resolverBestTitle);
        this.addPropertyResolver("selected", resolverSelected);
        this.addPropertyResolver("reference", resolverReference);
        this.addPropertyResolver("version_label", resolverVersionLabel);
        this.addPropertyResolver("auto_version", resolverAutoVersion);
        this.addPropertyResolver("language", resolverLanguage);
        this.addPropertyResolver("status", resolverStatus);
        this.addPropertyResolver("issue_date", resolverIssueDate);
        this.addPropertyResolver("security_ranking", resolverSecurityRanking);
        this.addPropertyResolver("expiration_date", resolverExpirationDate);
        this.addPropertyResolver("keyword", resolverKeyword);
        this.addPropertyResolver(TITLE_OR_NAME, resolverBestTitle);
        this.addPropertyResolver(PARENT_ID, resolverParentId);
        this.addPropertyResolver(ALTERNATIVE_DOWNLOAD_URL, this.resolverAlternativeDownload);
        this.addPropertyResolver(ALTERNATIVE_BROWSE_URL, this.resolverAlternativeBrowse);

        this.addPropertyResolver(ONCLICK_PREVIEW_BEHAVIOUR, this.resolverPreview);

        this.addPropertyResolver(ONCLICK_EDIT_IN_OFFICE, this.resolverEditInOffice);
    }

    public String getTitle() {
        return (String) this.get(NavigableNode.BEST_TITLE_RESOLVER_NAME);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the navigableNodeType
     */
    public NavigableNodeType getNavigableNodeType() {
        return navigableNodeType;
    }

    public ManagementService getManagementService() {
        if (managementService == null) {
            managementService = getCircabcServices().getManagementService();
        }
        return managementService;
    }

    public CircabcServiceRegistry getCircabcServices() {
        if (circabcServiceRegistry == null) {
            circabcServiceRegistry = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance());
        }
        return circabcServiceRegistry;
    }

    @Override
    public void reset() {
        super.reset();
        this.resetCache();
    }

    public abstract NavigableNode getNavigableParent();

    public abstract List<NavigableNode> getNavigableChilds();

    public abstract void resetCache();

    public int compareTo(NavigableNode o) {
        return this.getTitle().compareToIgnoreCase(o.getTitle());
    }
}
