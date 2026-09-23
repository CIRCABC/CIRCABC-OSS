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
package eu.europa.ec.digit.circabc.rest.service.helper;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.DossierModel;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Business manager that inspects the type and state of Alfresco nodes.
 *
 * <p>This helper centralises all the "is a ...?" predicates used throughout the CIRCABC REST layer
 * to classify a {@link NodeRef} against the CIRCABC content model. It answers questions such as
 * whether a node is a folder, a document, a forum post, an interest group service root, or whether
 * a document is currently locked. Classification is performed by matching the node's content type
 * (optionally including subtypes) via the {@link DictionaryService} or by checking for the presence
 * of specific aspects, using the injected {@link NodeService} and {@link LockService}.
 *
 * <p>All predicate methods are null-safe: a {@code null} {@link NodeRef} always yields
 * {@code false}.
 *
 * @author Yanick Pignot
 */
public class NodeTypeManager {

  /** Content types considered direct children of a directory (profile) service node. */
  private static final List<QName> DIRECTORY_CHILDREN_TYPE = Arrays.asList(
    CircabcModel.TYPE_INTEREST_GROUP_PROFILE,
    ContentModel.TYPE_PERSON
  );

  /** Aspects marking the root node of each interest group service (library, information, etc.). */
  private static final List<QName> IG_SERVICES_ROOT_ASPECTS = Arrays.asList(
    CircabcModel.ASPECT_LIBRARY_ROOT,
    CircabcModel.ASPECT_INFORMATION_ROOT,
    CircabcModel.ASPECT_NEWSGROUP_ROOT,
    CircabcModel.ASPECT_EVENT_ROOT,
    CircabcModel.ASPECT_SURVEY_ROOT
  );

  /** Aspects marking a node as content belonging to one of the interest group services. */
  private static final List<QName> IG_SERVICES_CHILD_ASPECTS = Arrays.asList(
    CircabcModel.ASPECT_LIBRARY,
    CircabcModel.ASPECT_INFORMATION,
    CircabcModel.ASPECT_NEWSGROUP,
    CircabcModel.ASPECT_EVENT,
    CircabcModel.ASPECT_SURVEY
  );

  /** Content types representing link nodes (file link or folder link). */
  private static final List<QName> LINKS_TYPE = Arrays.asList(
    ApplicationModel.TYPE_FILELINK,
    ApplicationModel.TYPE_FOLDERLINK
  );

  /** Repository service used to read a node's type and aspects. */
  @Autowired
  private NodeService nodeService;

  /** Dictionary service used to resolve type hierarchy (subtype) relationships. */
  @Autowired
  private DictionaryService dictionaryService;

  /** Service used to determine whether a node is locked. */
  @Autowired
  private LockService lockService;

  //--------------
  //-- public methods

  /**
   * Checks whether the node is a container (or a subtype of container).
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a container type or subtype, {@code false} otherwise
   */
  public boolean isContainer(final NodeRef nodeRef) {
    return isFromType(nodeRef, ContentModel.TYPE_CONTAINER, true);
  }

  /**
   * Checks whether the node is content (or a subtype of content).
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a content type or subtype, {@code false} otherwise
   */
  public boolean isContent(final NodeRef nodeRef) {
    return isFromType(nodeRef, ContentModel.TYPE_CONTENT, true);
  }

  /**
   * Checks whether the node is a link (file link or folder link).
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a file or folder link, {@code false} otherwise
   */
  public boolean isLink(final NodeRef nodeRef) {
    return isFromAnyType(nodeRef, LINKS_TYPE, false);
  }

  /**
   * Checks whether the node is a person.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is of the person type, {@code false} otherwise
   */
  public boolean isPerson(final NodeRef nodeRef) {
    return isFromType(nodeRef, ContentModel.TYPE_PERSON);
  }

  /**
   * Checks whether the node is a multilingual content container.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a multilingual container, {@code false} otherwise
   */
  public boolean isMultilingualContent(final NodeRef nodeRef) {
    return isFromType(nodeRef, ContentModel.TYPE_MULTILINGUAL_CONTAINER);
  }

  /**
   * Checks whether the node is a folder.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is of the folder type, {@code false} otherwise
   */
  public boolean isFolder(final NodeRef nodeRef) {
    return isFromType(nodeRef, ContentModel.TYPE_FOLDER);
  }

  /**
   * Checks whether the node is a dossier space.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a dossier space, {@code false} otherwise
   */
  public boolean isDossier(final NodeRef nodeRef) {
    return isFromType(nodeRef, DossierModel.TYPE_DOSSIER_SPACE);
  }

  /**
   * Checks whether the node is a forum topic.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a topic, {@code false} otherwise
   */
  public boolean isTopic(final NodeRef nodeRef) {
    return isFromType(nodeRef, ForumModel.TYPE_TOPIC);
  }

  /**
   * Checks whether the node is a forum.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a forum, {@code false} otherwise
   */
  public boolean isForum(final NodeRef nodeRef) {
    return isFromType(nodeRef, ForumModel.TYPE_FORUM);
  }

  /**
   * Checks whether the node is a plain document, i.e. content that is not a URL.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is content without the URL-able aspect, {@code false}
   *     otherwise
   */
  public boolean isDocument(final NodeRef nodeRef) {
    return (
      isFromType(nodeRef, ContentModel.TYPE_CONTENT) &&
      !hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)
    );
  }

  /**
   * Checks whether the node is a URL, i.e. content carrying the URL-able aspect.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is content with the URL-able aspect, {@code false} otherwise
   */
  public boolean isUrl(final NodeRef nodeRef) {
    return (
      isFromType(nodeRef, ContentModel.TYPE_CONTENT) &&
      hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)
    );
  }

  /**
   * Checks whether the node is a forum post.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a post, {@code false} otherwise
   */
  public boolean isPost(final NodeRef nodeRef) {
    return isFromType(nodeRef, ForumModel.TYPE_POST);
  }

  /**
   * Checks whether the node is a document that is currently locked.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a document and is locked, {@code false} otherwise
   */
  public boolean isLockedDocument(final NodeRef nodeRef) {
    return isDocument(nodeRef) && isLocked(nodeRef);
  }

  /**
   * Checks whether the node is the working copy of a document.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a document carrying the working-copy aspect, {@code false}
   *     otherwise
   */
  public boolean isWorkingCopyDocument(final NodeRef nodeRef) {
    return (
      isDocument(nodeRef) &&
      hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    );
  }

  /**
   * Checks whether the node is a shared space folder.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the shared-space aspect, {@code false}
   *     otherwise
   */
  public boolean isSharedSpace(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_SHARED_SPACE)
    );
  }

  /**
   * Checks whether the node is the CIRCABC root folder.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the CIRCABC-root aspect, {@code false}
   *     otherwise
   */
  public boolean isCircabcRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)
    );
  }

  /**
   * Checks whether the node is managed by CIRCABC, i.e. it carries the CIRCABC management aspect or
   * is multilingual content.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is managed by CIRCABC, {@code false} otherwise
   */
  public boolean isManagedByCircabc(final NodeRef nodeRef) {
    return (
      hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT) ||
      isMultilingualContent(nodeRef)
    );
  }

  /**
   * Checks whether the node is a category header.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a category header, {@code false} otherwise
   */
  public boolean isHeader(final NodeRef nodeRef) {
    return isFromType(nodeRef, CircabcModel.TYPE_CATEGORY_HEADER);
  }

  /**
   * Checks whether the node is a category.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the category aspect, {@code false}
   *     otherwise
   */
  public boolean isCategory(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)
    );
  }

  /**
   * Checks whether the node is an interest group root.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the interest-group-root aspect,
   *     {@code false} otherwise
   */
  public boolean isInterestGroup(final NodeRef nodeRef) {
    return isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT);
  }

  /**
   * Checks whether the node is content belonging to an interest group service, i.e. it carries one
   * of the service child aspects or is a directory child.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is an interest group service child, {@code false} otherwise
   */
  public boolean isInterestGroupChild(final NodeRef nodeRef) {
    return (
      hasAnyAspect(nodeRef, IG_SERVICES_CHILD_ASPECTS) ||
      isDirectoryChild(nodeRef)
    );
  }

  /**
   * Checks whether the node is the root of an interest group service, i.e. it carries one of the
   * service root aspects or is a directory root.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is an interest group service root, {@code false} otherwise
   */
  public boolean isInterestGroupService(final NodeRef nodeRef) {
    return (
      hasAnyAspect(nodeRef, IG_SERVICES_ROOT_ASPECTS) ||
      isDirectoryRoot(nodeRef)
    );
  }

  /**
   * Checks whether the node is the root of the information service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the information-root aspect, {@code false}
   *     otherwise
   */
  public boolean isInformationRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) &&
      hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION_ROOT)
    );
  }

  /**
   * Checks whether the node belongs to the information service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node carries the information aspect, {@code false} otherwise
   */
  public boolean isInformationChild(final NodeRef nodeRef) {
    return hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION);
  }

  /**
   * Checks whether the node is the root of the library service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the library-root aspect, {@code false}
   *     otherwise
   */
  public boolean isLibraryRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)
    );
  }

  /**
   * Checks whether the node belongs to the library service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node carries the library aspect, {@code false} otherwise
   */
  public boolean isLibraryChild(final NodeRef nodeRef) {
    return hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY);
  }

  /**
   * Checks whether the node is the root of the directory (profiles) service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a directory service node, {@code false} otherwise
   */
  public boolean isDirectoryRoot(final NodeRef nodeRef) {
    return isFromType(nodeRef, CircabcModel.TYPE_DIRECTORY_SERVICE);
  }

  /**
   * Checks whether the node is a child of the directory service (an interest group profile or a
   * person).
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a directory child, {@code false} otherwise
   */
  public boolean isDirectoryChild(final NodeRef nodeRef) {
    return isFromAnyType(nodeRef, DIRECTORY_CHILDREN_TYPE, false);
  }

  /**
   * Checks whether the node is the root of the newsgroup service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the newsgroup-root aspect, {@code false}
   *     otherwise
   */
  public boolean isNewsgroupRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) &&
      hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT)
    );
  }

  /**
   * Checks whether the node belongs to the newsgroup service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node carries the newsgroup aspect, {@code false} otherwise
   */
  public boolean isNewsgroupChild(final NodeRef nodeRef) {
    return hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP);
  }

  /**
   * Checks whether the node is the root of the survey service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the survey-root aspect, {@code false}
   *     otherwise
   */
  public boolean isSurveyRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_SURVEY_ROOT)
    );
  }

  /**
   * Checks whether the node belongs to the survey service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node carries the survey aspect, {@code false} otherwise
   */
  public boolean isSurveyChild(final NodeRef nodeRef) {
    return hasAspect(nodeRef, CircabcModel.ASPECT_SURVEY);
  }

  /**
   * Checks whether the node is the root of the calendar (events) service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node is a folder carrying the event-root aspect, {@code false}
   *     otherwise
   */
  public boolean isCalendarRoot(final NodeRef nodeRef) {
    return (
      isFolder(nodeRef) && hasAspect(nodeRef, CircabcModel.ASPECT_EVENT_ROOT)
    );
  }

  /**
   * Checks whether the node belongs to the calendar (events) service.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node carries the event aspect, {@code false} otherwise
   */
  public boolean isCalendarChild(final NodeRef nodeRef) {
    return hasAspect(nodeRef, CircabcModel.ASPECT_EVENT);
  }

  //--------------
  //-- private helpers

  private LockStatus getLockStatus(final NodeRef nodeRef) {
    return lockService.getLockStatus(nodeRef);
  }

  private boolean isLocked(final NodeRef nodeRef) {
    final boolean hasAspect = hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
    boolean locked = false;
    if (hasAspect) {
      final LockStatus lockStatus = getLockStatus(nodeRef);
      if (
        lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER
      ) {
        locked = true;
      }
    }
    return locked;
  }

  private boolean hasAspect(final NodeRef nodeRef, final QName aspectQname) {
    if (nodeRef == null) {
      return false;
    } else {
      return hasAspect(aspectQname, nodeService.getAspects(nodeRef));
    }
  }

  private boolean hasAspect(
    final QName aspectQname,
    final Set<QName> nodeAspects
  ) {
    return nodeAspects != null && nodeAspects.contains(aspectQname);
  }

  private boolean hasAnyAspect(
    final NodeRef nodeRef,
    final List<QName> aspectQnames
  ) {
    if (nodeRef == null) {
      return false;
    } else {
      return hasAnyAspect(aspectQnames, nodeService.getAspects(nodeRef));
    }
  }

  private boolean hasAnyAspect(
    final List<QName> aspectQnames,
    final Set<QName> nodeAspects
  ) {
    if (nodeAspects == null || nodeAspects.isEmpty()) {
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

  private boolean isFromType(
    final NodeRef nodeRef,
    final QName typeQname,
    final boolean orSubtype
  ) {
    if (nodeRef == null) {
      return false;
    } else {
      return isFromType(typeQname, orSubtype, nodeService.getType(nodeRef));
    }
  }

  private boolean isFromType(
    final QName typeQname,
    final boolean orSubtype,
    final QName nodeType
  ) {
    if (typeQname.isMatch(nodeType)) {
      return true;
    } else if (orSubtype) {
      return dictionaryService.isSubClass(nodeType, typeQname);
    } else {
      return false;
    }
  }

  private boolean isFromAnyType(
    final NodeRef nodeRef,
    final List<QName> typeQnames,
    final boolean orSubtype
  ) {
    if (nodeRef == null) {
      return false;
    } else {
      return isFromAnyType(typeQnames, orSubtype, nodeService.getType(nodeRef));
    }
  }

  private boolean isFromAnyType(
    final List<QName> typeQnames,
    boolean orSubtype,
    final QName nodeType
  ) {
    if (nodeType == null) {
      return false;
    } else if (typeQnames.contains(nodeType)) {
      return true;
    } else if (!orSubtype) {
      return false;
    } else {
      boolean response = false;

      for (final QName qname : typeQnames) {
        if (dictionaryService.isSubClass(nodeType, qname)) {
          response = true;
          break;
        }
      }

      return response;
    }
  }
}
