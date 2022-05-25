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
package eu.cec.digit.circabc.business.helper;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.DossierModel;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Business manager that perform checking of the type and state of nodes. (ie: is a folder? is the
 * document locked? is a post?)
 *
 * @author Yanick Pignot
 */
public class NodeTypeManager {

    private static final List<QName> DIRECTORY_CHILDREN_TYPE = Arrays.asList(
            CircabcModel.TYPE_INTEREST_GROUP_PROFILE,
            ContentModel.TYPE_PERSON);

    private static final List<QName> IG_SERVICES_ROOT_ASPECTS = Arrays.asList(
            CircabcModel.ASPECT_LIBRARY_ROOT,
            CircabcModel.ASPECT_INFORMATION_ROOT,
            CircabcModel.ASPECT_NEWSGROUP_ROOT,
            CircabcModel.ASPECT_EVENT_ROOT,
            CircabcModel.ASPECT_SURVEY_ROOT);

    private static final List<QName> IG_SERVICES_CHILD_ASPECTS = Arrays.asList(
            CircabcModel.ASPECT_LIBRARY,
            CircabcModel.ASPECT_INFORMATION,
            CircabcModel.ASPECT_NEWSGROUP,
            CircabcModel.ASPECT_EVENT,
            CircabcModel.ASPECT_SURVEY);

    private static final List<QName> LINKS_TYPE = Arrays.asList(
            ApplicationModel.TYPE_FILELINK,
            ApplicationModel.TYPE_FOLDERLINK);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private LockService lockService;

    //--------------
    //-- public methods

    public boolean isContainer(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_CONTAINER, true);
    }

    public boolean isContent(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_CONTENT, true);
    }

    public boolean isLink(final NodeRef nodeRef) {
        return isFromAnyType(nodeRef, LINKS_TYPE, false);
    }

    public boolean isPerson(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_PERSON);
    }

    public boolean isMultilingualContent(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_MULTILINGUAL_CONTAINER);
    }

    public boolean isFolder(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_FOLDER);
    }

    public boolean isDossier(final NodeRef nodeRef) {
        return isFromType(nodeRef, DossierModel.TYPE_DOSSIER_SPACE);
    }

    public boolean isTopic(final NodeRef nodeRef) {
        return isFromType(nodeRef, ForumModel.TYPE_TOPIC);
    }

    public boolean isForum(final NodeRef nodeRef) {
        return isFromType(nodeRef, ForumModel.TYPE_FORUM);
    }

    public boolean isDocument(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_CONTENT) && !hasAspect(nodeRef,
                DocumentModel.ASPECT_URLABLE);
    }

    public boolean isUrl(final NodeRef nodeRef) {
        return isFromType(nodeRef, ContentModel.TYPE_CONTENT) && hasAspect(nodeRef,
                DocumentModel.ASPECT_URLABLE);
    }

    public boolean isPost(final NodeRef nodeRef) {
        return isFromType(nodeRef, ForumModel.TYPE_POST);
    }

    public boolean isLockedDocument(final NodeRef nodeRef) {
        return isDocument(nodeRef) && isLocked(nodeRef);
    }

    public boolean isWorkingCopyDocument(final NodeRef nodeRef) {
        return isDocument(nodeRef) && hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
    }

    public boolean isSharedSpace(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_SHARED_SPACE);
    }

    public boolean isCircabcRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT);
    }

    public boolean isManagedByCircabc(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT) || isMultilingualContent(
                nodeRef);
    }

    public boolean isHeader(final NodeRef nodeRef) {
        return isFromType(nodeRef, CircabcModel.TYPE_CATEGORY_HEADER);
    }

    public boolean isCategory(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY);
    }

    public boolean isInterestGroup(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT);
    }

    public boolean isInterestGroupChild(final NodeRef nodeRef) {
        return hasAnyAspect(nodeRef, IG_SERVICES_CHILD_ASPECTS) || isDirectoryChild(nodeRef);
    }

    public boolean isInterestGroupService(final NodeRef nodeRef) {
        return hasAnyAspect(nodeRef, IG_SERVICES_ROOT_ASPECTS) || isDirectoryRoot(nodeRef);
    }

    public boolean isInformationRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION_ROOT);
    }

    public boolean isInformationChild(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION);
    }

    public boolean isLibraryRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY_ROOT);
    }

    public boolean isLibraryChild(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY);
    }

    public boolean isDirectoryRoot(final NodeRef nodeRef) {
        return isFromType(nodeRef, CircabcModel.TYPE_DIRECTORY_SERVICE);
    }

    public boolean isDirectoryChild(final NodeRef nodeRef) {
        return isFromAnyType(nodeRef, DIRECTORY_CHILDREN_TYPE, false);
    }

    public boolean isNewsgroupRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT);
    }

    public boolean isNewsgroupChild(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP);
    }

    public boolean isSurveyRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_SURVEY_ROOT);
    }

    public boolean isSurveyChild(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_SURVEY);
    }

    public boolean isCalendarRoot(final NodeRef nodeRef) {
        return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_EVENT_ROOT);
    }

    public boolean isCalendarChild(final NodeRef nodeRef) {
        return hasAspect(nodeRef, CircabcModel.ASPECT_EVENT);
    }

    //--------------
    //-- private helpers

    private LockStatus getLockStatus(final NodeRef nodeRef) {
        final LockStatus lockStatus = getLockService().getLockStatus(nodeRef);
        return lockStatus;
    }

    private boolean isLocked(final NodeRef nodeRef) {
        final boolean hasAspect = hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
        boolean locked = false;
        if (hasAspect) {
            final LockStatus lockStatus = getLockStatus(nodeRef);
            if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER) {
                locked = true;
            }
        }
        return locked;
    }

    private boolean hasAspect(final NodeRef nodeRef, final QName aspectQname) {
        if (nodeRef == null) {
            return false;
        } else {
            return hasAspect(aspectQname, getNodeService().getAspects(nodeRef));
        }
    }

    private boolean hasAspect(final QName aspectQname, final Set<QName> nodeAspects) {
        return nodeAspects != null && nodeAspects.contains(aspectQname);
    }

    private boolean hasAnyAspect(final NodeRef nodeRef, final List<QName> aspectQnames) {
        if (nodeRef == null) {
            return false;
        } else {
            return hasAnyAspect(aspectQnames, getNodeService().getAspects(nodeRef));
        }
    }

    private boolean hasAnyAspect(final List<QName> aspectQnames, final Set<QName> nodeAspects) {
        if (nodeAspects == null || nodeAspects.size() < 1) {
            return false;
        } else {
            boolean response = false;

            for (final QName qname : aspectQnames) {
                if (nodeAspects.contains(qname)) {
                    response = true;
                    break;
                }
            }

            return response;
        }
    }

    private boolean isFromType(final NodeRef nodeRef, final QName typeQname) {
        return isFromType(nodeRef, typeQname, false);
    }

    private boolean isFromType(final NodeRef nodeRef, final QName typeQname,
                               final boolean orSubtype) {
        if (nodeRef == null) {
            return false;
        } else {
            return isFromType(typeQname, orSubtype, getNodeService().getType(nodeRef));
        }
    }

    private boolean isFromType(final QName typeQname, final boolean orSubtype, final QName nodeType) {
        if (typeQname.isMatch(nodeType)) {
            return true;
        } else if (orSubtype) {
            return getDictionaryService().isSubClass(nodeType, typeQname);
        } else {
            return false;
        }
    }

    private boolean isFromAnyType(final NodeRef nodeRef, final List<QName> typeQnames,
                                  final boolean orSubtype) {
        if (nodeRef == null) {
            return false;
        } else {
            return isFromAnyType(typeQnames, orSubtype, getNodeService().getType(nodeRef));
        }
    }

    private boolean isFromAnyType(final List<QName> typeQnames, boolean orSubtype,
                                  final QName nodeType) {
        if (nodeType == null) {
            return false;
        } else if (typeQnames.contains(nodeType)) {
            return true;
        } else if (orSubtype == false) {
            return false;
        } else {
            boolean response = false;

            for (final QName qname : typeQnames) {
                if (getDictionaryService().isSubClass(nodeType, qname)) {
                    response = true;
                    break;
                }
            }

            return response;
        }
    }

    //--------------
    //-- IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the dictionaryService
     */
    protected final DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the dictionaryService
     */
    protected final LockService getLockService() {
        return lockService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setLockService(final LockService lockService) {
        this.lockService = lockService;
    }
}
