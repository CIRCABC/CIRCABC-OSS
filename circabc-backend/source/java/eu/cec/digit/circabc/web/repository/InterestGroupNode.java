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
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.*;

/**
 * Wrap a an interest group node for the Client Side.
 *
 * @author Yanick Pignot
 */
public class InterestGroupNode extends NavigableNode {

    public static final String LIBRARY_SERVICE = "library";
    public static final String NEWSGROUP_SERVICE = "newsgroup";
    public static final String SURVEY_SERVICE = "survey";
    public static final String DIRECTORY_SERVICE = "directory";
    public static final String INFORMATION_SERVICE = "information";
    public static final String EVENT_SERVICE = "event";
    public static final List<String> VALID_SERVICES = Collections.unmodifiableList(Arrays
            .asList(LIBRARY_SERVICE, NEWSGROUP_SERVICE, SURVEY_SERVICE, DIRECTORY_SERVICE,
                    INFORMATION_SERVICE, EVENT_SERVICE));
    public static final String MSG_NO_CONTACT_INFO = "igroot_home_no_contact_information";
    public static final String MSG_NO_DESCRIPTION = "igroot_home_no_description";
    /**
     * The library aspect root
     */
    public static final QName LIBRARY_ASPECT = CircabcModel.ASPECT_LIBRARY_ROOT;
    /**
     * The newsgroup aspect root
     */
    public static final QName NEWSGROUP_ASPECT = CircabcModel.ASPECT_NEWSGROUP_ROOT;
    /**
     * The surveys aspect root
     */
    public static final QName SURVEY_ASPECT = CircabcModel.ASPECT_SURVEY_ROOT;
    /**
     * The directory aspect root
     */
    public static final QName DIRECTORY_TYPE = CircabcModel.TYPE_DIRECTORY_SERVICE;
    /**
     * The information aspect root
     */
    public static final QName INFORMATION_ASPECT = CircabcModel.ASPECT_INFORMATION_ROOT;
    /**
     * The event aspect root
     */
    public static final QName EVENT_ASPECT = CircabcModel.ASPECT_EVENT_ROOT;
    private static final int CHARACTER_NUMBER_LIMIT = 165;
    private static final Log logger = LogFactory.getLog(InterestGroupNode.class);
    private static final long serialVersionUID = 4245045930595700474L;
    private CategoryNode categoryNode = null;
    /**
     * The list of childs by aspect
     */
    private Map<QName, IGServicesNode> childsByAspectNotSecured = null;
    private Map<QName, IGServicesNode> childsByAspect = null;
    private NodePropertyResolver resolverLibrary = new NodePropertyResolver() {

        private static final long serialVersionUID = 4722219116510860675L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;

            return cNode.getLibrary();
        }
    };
    private NodePropertyResolver resolverNewsgroup = new NodePropertyResolver() {

        private static final long serialVersionUID = -1532603564472181758L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;

            return cNode.getNewsgroup();
        }
    };
    private NodePropertyResolver resolverSurvey = new NodePropertyResolver() {

        private static final long serialVersionUID = -2790931686852053223L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;

            return cNode.getSurvey();
        }
    };
    private NodePropertyResolver resolverDirectory = new NodePropertyResolver() {

        private static final long serialVersionUID = -2244027600494721864L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;
            return cNode.getDirectory();
        }
    };
    private NodePropertyResolver resolverInformation = new NodePropertyResolver() {

        private static final long serialVersionUID = -2159327600494721864L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;
            return cNode.getInformation();
        }
    };
    private NodePropertyResolver resolverEvent = new NodePropertyResolver() {

        private static final long serialVersionUID = -2244027600479141864L;

        public Object get(Node node) {
            InterestGroupNode cNode = (InterestGroupNode) node;
            return cNode.getEvent();
        }
    };
    private NodePropertyResolver resolverContactInfo = new NodePropertyResolver() {

        private static final long serialVersionUID = 9088056683022871229L;

        public Object get(Node node) {
            Object contactInfo = node.getProperties()
                    .get(CircabcModel.PROP_CONTACT_INFORMATION.toString());
            return (contactInfo == null || contactInfo.toString().length() < 1)
                    ? "<i>" + Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO_CONTACT_INFO)
                    + "</i>"
                    : contactInfo.toString();
        }
    };
    private NodePropertyResolver resolverSafeDescription = new NodePropertyResolver() {

        private static final long serialVersionUID = -4936070094338967058L;

        public Object get(Node node) {
            final Object desc = node.getProperties().get(ContentModel.PROP_DESCRIPTION.toString());

            if (desc == null || desc.toString().length() < 1) {
                return "<i>" + Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO_DESCRIPTION)
                        + "</i>";
            } else {

                return desc;
            }

        }
    };
    private NodePropertyResolver resolverNoHtmlDescription = new NodePropertyResolver() {

        private static final long serialVersionUID = -5141066212483817911L;

        public Object get(Node node) {
            final Object lDesc = node.getProperties().get(CircabcModel.PROP_LIGHT_DESCRIPTION.toString());

            if (lDesc == null || lDesc.toString().length() < 1) {
                return "<i>" + Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO_DESCRIPTION)
                        + "</i>";
            } else {

                return lDesc;
            }

        }
    };
    private NodePropertyResolver resolverNeedModeDetails = new NodePropertyResolver() {

        private static final long serialVersionUID = 1137394965899978881L;

        public Object get(Node node) {
            final Object lDesc = node.getProperties().get(CircabcModel.PROP_LIGHT_DESCRIPTION.toString());

            if (lDesc == null || lDesc.toString().length() < CHARACTER_NUMBER_LIMIT) {
                return false;
            } else {

                return true;
            }

        }
    };

    public InterestGroupNode(final NodeRef nodeRef, final CategoryNode categoryNode) {
        super(nodeRef);

        if (!NavigableNodeType.IG_ROOT.equals(getNavigableNodeType())) {
            if (logger.isErrorEnabled()) {
                logger.error("Wrong navigable nodeType for nodeRef: " + nodeRef.toString());
                logger.error("Expected nodeType: " + NavigableNodeType.IG_ROOT.toString());
                logger.error(
                        "Actual nodeType: " + getNavigableNodeType() != null ? getNavigableNodeType().toString()
                                : null);
            }
            throw new IllegalArgumentException(
                    "NodeRef must be a Circabc Interest Group and have the " + CircabcModel.ASPECT_IGROOT
                            + " aspect applied");
        }

        this.categoryNode = categoryNode;

        this.addPropertyResolver(VALID_SERVICES.get(0), resolverLibrary);
        this.addPropertyResolver(VALID_SERVICES.get(1), resolverNewsgroup);
        this.addPropertyResolver(VALID_SERVICES.get(2), resolverSurvey);
        this.addPropertyResolver(VALID_SERVICES.get(3), resolverDirectory);
        this.addPropertyResolver(VALID_SERVICES.get(4), resolverInformation);
        this.addPropertyResolver(VALID_SERVICES.get(5), resolverEvent);
        this.addPropertyResolver("contactInfo", resolverContactInfo);
        this.addPropertyResolver("safeDescription", resolverSafeDescription);
        this.addPropertyResolver("noHtmlDescription", resolverNoHtmlDescription);
        this.addPropertyResolver("needMoreDetails", resolverNeedModeDetails);
    }

    public InterestGroupNode(final NodeRef nodeRef) {
        this(nodeRef, null);
    }

    public IGServicesNode getLibrary() {
        return getChildsByAspectNotSecured().get(LIBRARY_ASPECT);
    }

    public String getLibid() {
        return getChildsByAspectNotSecured().get(LIBRARY_ASPECT).getId();
    }

    public IGServicesNode getNewsgroup() {
        return getChildsByAspectNotSecured().get(NEWSGROUP_ASPECT);
    }

    public IGServicesNode getSurvey() {
        return getChildsByAspectNotSecured().get(SURVEY_ASPECT);
    }

    public IGServicesNode getDirectory() {
        return getChildsByAspect().get(DIRECTORY_TYPE);
    }

    public IGServicesNode getInformation() {
        return getChildsByAspectNotSecured().get(INFORMATION_ASPECT);
    }

    public IGServicesNode getEvent() {
        return getChildsByAspectNotSecured().get(EVENT_ASPECT);
    }

    /**
     * Helper method to fill,  if needed, the list of Services of this interest group
     *
     * @return the initialized ChildsByAspect Map
     */
    private Map<QName, IGServicesNode> getChildsByAspectNotSecured() {
        String userName = AuthenticationUtil.getRunAsUser();
        try {
            AuthenticationUtil.setRunAsUserSystem();
            if (childsByAspectNotSecured == null) {

                final List<ChildAssociationRef> assocs = getServiceRegistry().getNodeService()
                        .getChildAssocs(this.nodeRef);
                childsByAspectNotSecured = new HashMap<>(assocs.size());

                for (final ChildAssociationRef assoc : assocs) {
                    final Node node = new Node(assoc.getChildRef());

                    final NavigableNodeType type = AspectResolver.resolveType(node);

                    if (type != null) {
                        switch (type) {
                            case LIBRARY:
                                childsByAspectNotSecured.put(LIBRARY_ASPECT,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.LIBRARY));
                                break;
                            case NEWSGROUP:
                                childsByAspectNotSecured.put(NEWSGROUP_ASPECT,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.NEWSGROUP));
                                break;
                            case SURVEY:
                                childsByAspectNotSecured.put(SURVEY_ASPECT,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.SURVEY));
                                break;
                            case INFORMATION:
                                childsByAspectNotSecured.put(INFORMATION_ASPECT,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.INFORMATION));
                                break;
                            case EVENT:
                                childsByAspectNotSecured.put(EVENT_ASPECT,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.EVENT));
                                break;

                        }
                    }
                }
            }

        } finally {
            AuthenticationUtil.setRunAsUser(userName);
        }
        return childsByAspectNotSecured;
    }

    /**
     * Helper method to fill,  if needed, the list of Services of this interest group
     *
     * @return the initialized ChildsByAspect Map
     */
    private Map<QName, IGServicesNode> getChildsByAspect() {
        if (childsByAspect == null) {
            final List<ChildAssociationRef> assocs = getServiceRegistry().getNodeService()
                    .getChildAssocs(this.nodeRef);
            childsByAspect = new HashMap<>(assocs.size());

            for (final ChildAssociationRef assoc : assocs) {
                final Node node = new Node(assoc.getChildRef());

                final NavigableNodeType type = AspectResolver.resolveType(node);

                if (type != null) {
                    switch (type) {
                        case DIRECTORY:
                            if (hasPermission(DirectoryPermissions.DIRACCESS.toString())) {
                                childsByAspect.put(DIRECTORY_TYPE,
                                        new IGServicesNode(node.getNodeRef(), this, NavigableNodeType.DIRECTORY));
                            }
                            break;

                    }
                }
            }
        }

        return childsByAspect;
    }

    @Override
    public List<NavigableNode> getNavigableChilds() {

        List<NavigableNode> childs = new ArrayList<>();
        childs.addAll(getChildsByAspectNotSecured().values());

        return childs;
    }

    @Override
    public NavigableNode getNavigableParent() {
        if (categoryNode == null) {
            NodeRef parent = services.getNodeService().getPrimaryParent(this.nodeRef).getParentRef();
            categoryNode = new CategoryNode(parent);
        }

        return categoryNode;
    }

    @Override
    public void resetCache() {
        childsByAspectNotSecured = null;
        categoryNode = null;
    }
}
