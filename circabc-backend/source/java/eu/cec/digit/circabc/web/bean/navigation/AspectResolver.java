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

import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;

import static eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType.*;

/**
 * Util class that resolve the cicabc node type of a given node.
 *
 * @author yanick pignot
 * @see eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType
 */
public abstract class AspectResolver {

    private static final Log LOGGER = LogFactory.getLog(AspectResolver.class);
    private static final StoreRef SPACE_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,
            "SpacesStore");

    private AspectResolver() {

    }

    public static boolean isNodeManagedByCircabc(final Node node) {
        if (!SPACE_STORE.equals(node.getNodeRef().getStoreRef())) {
            return false;
        } else if (CIRCABC_CHILD.isNodeFromType(node) || CATEGORY_HEADER.isNodeFromType(node)) {
            return true;
        } else if (LIBRARY_ML_CONTENT.isNodeFromType(node)) {
            final MultilingualContentService mlService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();

            return isNodeManagedByCircabc(new Node(mlService.getPivotTranslation(node.getNodeRef())));
        } else if (LIBRARY_ML_CONTENT_FORUM.isNodeFromType(node) || LIBRARY_ML_CONTENT_POST
                .isNodeFromType(node) || LIBRARY_ML_CONTENT_TOPIC.isNodeFromType(node)) {
            final NodeService nodeService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
            return isNodeManagedByCircabc(
                    new Node(nodeService.getPrimaryParent(node.getNodeRef()).getParentRef()));
        } else if (LIBRARY_EMPTY_TRANSLATION.isNodeFromType(node)) {
            final MultilingualContentService mlService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
            return isNodeManagedByCircabc(new Node(mlService.getPivotTranslation(node.getNodeRef())));
        } else {
            return false;
        }
    }

    public static NavigableNodeType resolveType(final Node node) {

        long startTime = 0;
        if (LOGGER.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final NavigableNodeType nodeType = resolveTypeImpl(node);

        if (LOGGER.isDebugEnabled()) {
            final long endTime = System.currentTimeMillis();
            LOGGER.debug("Time to resolve a circabc node type: " + (endTime - startTime) + "ms. ");
            LOGGER.debug(
                    "Type returned : " + nodeType + " for the node : " + node.getName() + "\n\tAspects: "
                            + node.getAspects());
        }

        return nodeType;
    }

    public static NavigableNodeType resolveType(final NodeRef noderef) {
        return resolveType(new MapNode(noderef));
    }

    private static NavigableNodeType resolveTypeImpl(final Node node) {
        if (node == null) {
            throw new IllegalArgumentException("The node reference is mandatory.");
        } else if (CIRCABC_CHILD.isNodeFromType(node, false)) {
            if (CIRCABC_ROOT.isNodeFromType(node, false)) {
                return CIRCABC_ROOT;
            } else if (CATEGORY.isNodeFromType(node, false)) {
                return CATEGORY;
            } else if (IG_ROOT.isNodeFromType(node, false)) {
                return IG_ROOT;
            } else if (LIBRARY.isNodeFromType(node, false)) {
                return LIBRARY;
            } else if (NEWSGROUP.isNodeFromType(node, false)) {
                return NEWSGROUP;
            } else if (SURVEY.isNodeFromType(node, false)) {
                return SURVEY;
            } else if (DIRECTORY.isNodeFromType(node, false)) {
                return DIRECTORY;
            } else if (INFORMATION.isNodeFromType(node, false)) {
                return INFORMATION;
            } else if (EVENT.isNodeFromType(node, false)) {
                return EVENT;
            } else if (LIBRARY_CHILD.isNodeFromType(node, false)) {
                if (LIBRARY_FORUM.isNodeFromType(node, false)) {
                    return LIBRARY_FORUM;
                } else if (LIBRARY_POST.isNodeFromType(node, false)) {
                    return LIBRARY_POST;
                } else if (LIBRARY_TOPIC.isNodeFromType(node, false)) {
                    return LIBRARY_TOPIC;
                } else if (LIBRARY_DOSSIER.isNodeFromType(node, false)) {
                    return LIBRARY_DOSSIER;
                } else if (LIBRARY_SPACE.isNodeFromType(node, false)) {
                    return LIBRARY_SPACE;
                } else if (LIBRARY_EMPTY_TRANSLATION.isNodeFromType(node, false)) {
                    return LIBRARY_EMPTY_TRANSLATION;
                } else if (LIBRARY_CONTENT.isNodeFromType(node, false)) {
                    return LIBRARY_CONTENT;
                } else if (LIBRARY_FILE_LINK.isNodeFromType(node, false)) {
                    return LIBRARY_FILE_LINK;
                }
            } else if (NEWSGROUP_CHILD.isNodeFromType(node, false)) {
                if (NEWSGROUP_FORUM.isNodeFromType(node, false)) {
                    return NEWSGROUP_FORUM;
                } else if (NEWSGROUP_TOPIC.isNodeFromType(node, false)) {
                    return NEWSGROUP_TOPIC;
                } else if (NEWSGROUP_POST.isNodeFromType(node, false)) {
                    return NEWSGROUP_POST;
                }
            } else if (SURVEY_CHILD.isNodeFromType(node, false)) {
                if (SURVEY_SPACE.isNodeFromType(node, false)) {
                    return SURVEY_SPACE;
                } else if (SURVEY_ELEMENT.isNodeFromType(node, false)) {
                    return SURVEY_ELEMENT;
                }
            } else if (EVENT_CHILD.isNodeFromType(node, false)) {
                if (EVENT_APPOINTMENT.isNodeFromType(node, false)) {
                    return EVENT_APPOINTMENT;
                } else {
                    return EVENT_CHILD;
                }
            } else if (INFORMATION_CHILD.isNodeFromType(node, false)) {
                if (INFORMATION_SPACE.isNodeFromType(node, false)) {
                    return INFORMATION_SPACE;
                } else if (INFORMATION_CONTENT.isNodeFromType(node, false)) {
                    return INFORMATION_CONTENT;
                } else if (INFORMATION_FILE_LINK.isNodeFromType(node, false)) {
                    return INFORMATION_FILE_LINK;
                }
            }
        } else if (CATEGORY_HEADER.isNodeFromType(node, false)) {
            return CATEGORY_HEADER;
        } else if (isNodeManagedByCircabc(node)) {
            if (LIBRARY_ML_CONTENT.isNodeFromType(node, false)) {
                return LIBRARY_ML_CONTENT;
            } else if (LIBRARY_EMPTY_TRANSLATION.isNodeFromType(node, false)) {
                return LIBRARY_EMPTY_TRANSLATION;
            } else if (LIBRARY_ML_CONTENT_FORUM.isNodeFromType(node, false)) {
                return LIBRARY_ML_CONTENT_FORUM;
            } else if (LIBRARY_ML_CONTENT_POST.isNodeFromType(node, false)) {
                return LIBRARY_ML_CONTENT_POST;
            } else if (LIBRARY_ML_CONTENT_TOPIC.isNodeFromType(node, false)) {
                return LIBRARY_ML_CONTENT_TOPIC;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Circabc attempt to manage an No Circabc node or a not managed node " + node);
        }

        return null;
    }
}
