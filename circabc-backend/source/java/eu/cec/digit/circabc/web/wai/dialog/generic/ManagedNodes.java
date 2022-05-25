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
package eu.cec.digit.circabc.web.wai.dialog.generic;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.DossierModel;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

import java.util.Set;

/**
 * Util enum that help the management of generic actions on node by centralizing the logic to manage any kind of node.
 *
 * @author Yanick Pignot
 */
/* package */public enum ManagedNodes {
    CIRCABC_NODE,
    CATEGORY_NODE,
    CATEGORY_HEADER_NODE,
    IG_NODE,
    LIBRARY_NODE,
    NEWSGROUP_NODE,
    INFO_SERVICE_NODE,
    SURVEY_NODE,
    EVENTS_NODE,
    //TODO the other one...
    CONTENT,
    SPACE,
    DOSSIER,
    ML_CONTAINER,
    TRANSLATION,
    EMPTY_TRANSLATION,
    FORUMS,
    FORUM,
    TOPIC,
    POST,
    LINK,
    EXTERNAL_LINK;


    /* package */
    public static ManagedNodes resolve(Node node) {
        ManagedNodes managedNodes = null;

        QName type = node.getType();
        Set<QName> aspects = node.getAspects();

        if (type.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            managedNodes = ManagedNodes.ML_CONTAINER;
        } else if (type.equals(ContentModel.TYPE_FOLDER)) {
            if (aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)) {
                managedNodes = ManagedNodes.CIRCABC_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_CATEGORY)) {
                managedNodes = ManagedNodes.CATEGORY_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_IGROOT)) {
                managedNodes = ManagedNodes.IG_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_LIBRARY_ROOT)) {
                managedNodes = ManagedNodes.LIBRARY_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
                managedNodes = ManagedNodes.NEWSGROUP_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_INFORMATION_ROOT)) {
                managedNodes = ManagedNodes.INFO_SERVICE_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_EVENT_ROOT)) {
                managedNodes = ManagedNodes.EVENTS_NODE;
            } else if (aspects.contains(CircabcModel.ASPECT_SURVEY_ROOT)) {
                managedNodes = ManagedNodes.SURVEY_NODE;
            } else {
                managedNodes = ManagedNodes.SPACE;
            }
        } else if (aspects.contains(CircabcModel.ASPECT_SURVEY_ROOT)) {
            managedNodes = ManagedNodes.SURVEY_NODE;
        } else if (type.equals(DossierModel.TYPE_DOSSIER_SPACE)) {
            managedNodes = ManagedNodes.DOSSIER;
        } else if (type.equals(ForumModel.TYPE_FORUMS)) {
            managedNodes = ManagedNodes.FORUMS;
        } else if (type.equals(ForumModel.TYPE_FORUM)) {
            managedNodes = ManagedNodes.FORUM;
        } else if (type.equals(ForumModel.TYPE_TOPIC)) {
            managedNodes = ManagedNodes.TOPIC;
        } else if (type.equals(ForumModel.TYPE_POST)) {
            managedNodes = ManagedNodes.POST;
        } else if (type.equals(ApplicationModel.TYPE_FILELINK) || type
                .equals(ApplicationModel.TYPE_FOLDERLINK)) {
            managedNodes = ManagedNodes.LINK;
        } else if (aspects.contains(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
            managedNodes = ManagedNodes.EMPTY_TRANSLATION;
        } else if (aspects.contains(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            managedNodes = ManagedNodes.TRANSLATION;
        } else if (aspects.contains(DocumentModel.ASPECT_URLABLE)) {
            managedNodes = ManagedNodes.EXTERNAL_LINK;
        } else if (type.equals(ContentModel.TYPE_CONTENT)) {
            managedNodes = ManagedNodes.CONTENT;
        } else if (type.equals(ContentModel.TYPE_CATEGORY)) {
            managedNodes = ManagedNodes.CATEGORY_HEADER_NODE;
        }
        return managedNodes;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
