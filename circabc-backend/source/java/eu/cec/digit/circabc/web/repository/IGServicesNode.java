/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.repository;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Wrap a any iterest group service node for the Client Side.
 *
 * @author Yanick Pignot
 */
public class IGServicesNode extends NavigableNode {

    public static final String IS_LIBRARY = "isLibraryChild";
    public static final String IS_NEWSGROUP = "isNewsgroupChild";
    public static final String IS_SURVEY = "isSurveyChild";
    public static final String IS_DIRECTORY = "isDirectoryChild";
    public static final String IS_EVENT = "isEventChild";
    public static final String IS_INFORMATION = "isInformationChild";
    public static final String SERVICE_ROOT = "serviceRoot";
    public static final String DYNATTR1 = "dynAttr1";
    private static final long serialVersionUID = 4245045930595709474L;
    private static final String DIRECTORY_NODE_NAME = "igroot_members"; //$NON-NLS-1$
    private InterestGroupNode igNode;
    private NodePropertyResolver resolverCurrentServiceRoot = new NodePropertyResolver() {

        private static final long serialVersionUID = -78929753303917323L;

        public Object get(final Node node) {

            final NodeService nonSecuredNodeService = getCircabcServices().getNonSecuredNodeService();
            NodeRef service = node.getNodeRef();
            NodeRef igRoot = null;

            while (true) {
                igRoot = nonSecuredNodeService.getPrimaryParent(service).getParentRef();

                if (igRoot == null) {
                    // we are on top of Alfresco structure !!!
                    service = null;
                    break;
                } else if (nonSecuredNodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)) {
                    // the Service noderef = service
                    break;
                } else {
                    // up to one level
                    service = igRoot;
                }
            }

            return service;
        }
    };
    private NodePropertyResolver resolverIsLibrary = new NodePropertyResolver() {

        private static final long serialVersionUID = -789292026303917323L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.LIBRARY);
        }
    };
    private NodePropertyResolver resolverIsNewsgroup = new NodePropertyResolver() {

        private static final long serialVersionUID = 7317711575384285066L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.NEWSGROUP);
        }
    };
    private NodePropertyResolver resolverIsSurvey = new NodePropertyResolver() {

        private static final long serialVersionUID = 7940437721739957634L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.SURVEY);
        }
    };
    private NodePropertyResolver resolverIsDirectory = new NodePropertyResolver() {

        private static final long serialVersionUID = 495970136988182680L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.DIRECTORY);
        }
    };
    private NodePropertyResolver resolverIsInformation = new NodePropertyResolver() {

        private static final long serialVersionUID = 495970109818182680L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.INFORMATION);
        }
    };
    private NodePropertyResolver resolverIsEvent = new NodePropertyResolver() {

        private static final long serialVersionUID = 495970109818741080L;

        public Object get(final Node node) {
            final IGServicesNode cNode = (IGServicesNode) node;

            return cNode.getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.EVENT);
        }
    };

    /*package*/IGServicesNode(final NodeRef nodeRef, final InterestGroupNode igNode,
                              final NavigableNodeType trustedType) {
        super(nodeRef, trustedType);
        validate(igNode);
    }

    public IGServicesNode(final NodeRef nodeRef, final InterestGroupNode igNode) {
        super(nodeRef);
        validate(igNode);
    }

    public IGServicesNode(final NodeRef nodeRef, final NodeService nodeService,
                          final Map<QName, Serializable> props) {
        super(nodeRef, nodeService, props);
    }

    public IGServicesNode(NodeRef nodeRef) {
        this(nodeRef, null);
    }

    private void validate(final InterestGroupNode igNode) {
        if (getNavigableNodeType().equals(NavigableNodeType.IG_ROOT)) {
            throw new IllegalArgumentException(
                    "NodeRef must be a child of an Interest Group and must not have the  "
                            + CircabcModel.ASPECT_IGROOT + " aspect applied"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (!getNavigableNodeType().isEqualsOrUnder(NavigableNodeType.IG_ROOT)) {
            throw new IllegalArgumentException(
                    "NodeRef must be a Circabc Interest Group child aspect applied"); //$NON-NLS-1$
        }

        this.igNode = igNode;

        this.addPropertyResolver(IS_LIBRARY, resolverIsLibrary);
        this.addPropertyResolver(IS_NEWSGROUP, resolverIsNewsgroup);
        this.addPropertyResolver(IS_SURVEY, resolverIsSurvey);
        this.addPropertyResolver(IS_DIRECTORY, resolverIsDirectory);
        this.addPropertyResolver(IS_EVENT, resolverIsEvent);
        this.addPropertyResolver(IS_INFORMATION, resolverIsInformation);
        this.addPropertyResolver(SERVICE_ROOT, resolverCurrentServiceRoot);
    }

    public String getDynAttr1() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_1.toString());
    }

    public String getDynAttr2() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_2.toString());
    }

    public String getDynAttr3() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_3.toString());
    }

    public String getDynAttr4() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_4.toString());
    }

    public String getDynAttr5() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_5.toString());
    }

    public String getDynAttr6() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_6.toString());
    }

    public String getDynAttr7() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_7.toString());
    }

    public String getDynAttr8() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_8.toString());
    }

    public String getDynAttr9() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_9.toString());
    }

    public String getDynAttr10() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_10.toString());
    }

    public String getDynAttr11() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_11.toString());
    }

    public String getDynAttr12() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_12.toString());
    }

    public String getDynAttr13() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_13.toString());
    }

    public String getDynAttr14() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_14.toString());
    }

    public String getDynAttr15() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_15.toString());
    }

    public String getDynAttr16() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_16.toString());
    }

    public String getDynAttr17() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_17.toString());
    }

    public String getDynAttr18() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_18.toString());
    }

    public String getDynAttr19() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_19.toString());
    }

    public String getDynAttr20() {
        return (String) getProperties().get(DocumentModel.PROP_DYN_ATTR_20.toString());
    }

    @Override
    public List<NavigableNode> getNavigableChilds() {
        return null;
    }

    @Override
    public NavigableNode getNavigableParent() {
        if (igNode == null) {
            final NodeRef ref = getManagementService().getCurrentInterestGroup(this.nodeRef);

            igNode = new InterestGroupNode(ref);

            validate(igNode);
        }

        return igNode;
    }

    @Override
    public String getName() {
        final NavigableNodeType nt = getNavigableNodeType();
        if (nt != null && nt.isEqualsOrUnder(NavigableNodeType.DIRECTORY)) {
            return Application.getMessage(FacesContext.getCurrentInstance(), DIRECTORY_NODE_NAME);
        } else {
            return super.getName();
        }
    }

    @Override
    public void resetCache() {
        igNode = null;
    }
}
