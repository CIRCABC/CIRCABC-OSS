/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.newsgroup;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import io.swagger.model.AbuseReport;
import io.swagger.model.AbuseReportImpl;
import io.swagger.model.alfresco.ModerationModel;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ModerationService} for newsgroup (forum) content.
 *
 * <p>Moderation is expressed through Alfresco aspects defined in {@link ModerationModel}. A
 * container (forums/forum/topic node or folder) can be flagged as moderated, in which case its
 * contained posts must be explicitly approved before they become visible. Each post can therefore
 * be in one of several moderation states: waiting for approval, approved or rejected. This service
 * also supports abuse reporting on individual posts.
 *
 * <p>State transitions are implemented by adding/removing the relevant moderation aspects and by
 * toggling permission inheritance so that only moderators can see posts that are still waiting for
 * approval.
 *
 * @author Yanick Pignot
 */
public class ModerationServiceImpl implements ModerationService {

  /**
   * Cache of the well-known forum container types (forums, forum and topic). Used to quickly decide
   * whether a node is a moderation container. Declared as a constant for performance purposes.
   */
  private static final List<QName> FORUM_CONTAINERS = Arrays.asList(
    ForumModel.TYPE_FORUMS,
    ForumModel.TYPE_FORUM,
    ForumModel.TYPE_TOPIC
  );

  /** Action name used to accept all pending posts when moderation is stopped. */
  private static final String MODERATION_ACTION_ACCEPT = "accept";
  /** Action name used to reject all pending posts when moderation is stopped. */
  private static final String MODERATION_ACTION_REFUSE = "refuse";
  /** Rejection message automatically applied to pending posts when moderation is refused. */
  private static final String MODERATION_ACTION_REFUSE_AUTO_MESSAGE =
    "This post has been automatically refused, because this forum is not anymore moderated";
  /** Label used for the mandatory content-node parameter checks. */
  private static final String CONTENT_REFERENCE = "Content reference";

  /** Alfresco node service used to read/write aspects, properties and child associations. */
  @Autowired
  private NodeService nodeService;

  /** Dictionary service used to test node type hierarchy (e.g. subclass of folder). */
  @Autowired
  private DictionaryService dictionaryService;

  /** Permission service used to toggle inheritance so pending posts stay hidden from non-moderators. */
  @Autowired
  private PermissionService permissionService;

  /** Content service used to overwrite the content of rejected posts. */
  @Autowired
  private ContentService contentService;

  /** Policy behaviour filter used to temporarily disable aspect behaviours during rejection. */
  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  /**
   * Enables moderation on the given container node and, recursively, on all of its child
   * containers.
   *
   * @param container the container node (forum container or folder) on which moderation must be
   *     applied; must not be {@code null}
   * @param makeContentWaiting when {@code true}, every non-container child is put into the
   *     "waiting for approval" state as moderation is applied
   * @throws IllegalArgumentException if {@code container} is not a container node
   */
  public void applyModeration(
    final NodeRef container,
    final boolean makeContentWaiting
  ) {
    ParameterCheck.mandatory("The container", container);

    if (!isContainer(container)) {
      throw new IllegalArgumentException(
        "Impossible to apply moderation on a non-container node."
      );
    }

    applyModerationImpl(container, makeContentWaiting);
  }

  private void applyModerationImpl(
    final NodeRef container,
    final boolean makeContentWaiting
  ) {
    if (!isContainerModerated(container)) {
      nodeService.addAspect(
        container,
        ModerationModel.ASPECT_MODERATED,
        Collections.singletonMap(
          ModerationModel.PROP_IS_MODERATED,
          (Serializable) Boolean.TRUE
        )
      );

      for (final ChildAssociationRef assoc : nodeService.getChildAssocs(
        container
      )) {
        final NodeRef childRef = assoc.getChildRef();
        if (isContainer(childRef)) {
          applyModerationImpl(childRef, makeContentWaiting);
        } else if (makeContentWaiting) {
          waitForApproval(childRef);
        }
      }
    }
  }

  /**
   * Approves a post that is currently waiting for approval, making it visible again.
   *
   * <p>Permission inheritance is re-enabled and the {@code approved} aspect (with the current user
   * and timestamp) is applied while the {@code waiting for approval} aspect is removed.
   *
   * @param content the post to accept; must not be {@code null}
   * @throws IllegalArgumentException if {@code content} is a container or is not waiting for
   *     approval
   */
  public void accept(final NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    if (isContainer(content)) {
      throw new IllegalArgumentException(
        "Impossible to accept a container. The moderation is possible only on a content or a post"
      );
    } else if (!isWaitingForApproval(content)) {
      throw new IllegalArgumentException(
        "Impossible to accept a post not defined being waiting for approval!"
      );
    }

    permissionService.setInheritParentPermissions(content, true);

    final Map<QName, Serializable> moderationProperties =
      getCommonModerationProperties(false);
    nodeService.addAspect(
      content,
      ModerationModel.ASPECT_APPROVED,
      moderationProperties
    );

    nodeService.removeAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL);
  }

  /**
   * Rejects a post that is currently waiting for approval.
   *
   * <p>The {@code rejected} aspect (with the current user, timestamp and the given reject message)
   * is applied and the post content is cleared to an empty value, creating a new version. Related
   * behaviours (rejection and content-notify aspects) are temporarily disabled while the content is
   * overwritten. The {@code waiting for approval} aspect is finally removed.
   *
   * @param content the post to reject; must not be {@code null}
   * @param message the reject reason to store; a {@code null} value is stored as an empty string
   * @throws IllegalArgumentException if {@code content} is a container or is not waiting for
   *     approval
   */
  public void reject(final NodeRef content, final String message) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    if (isContainer(content)) {
      throw new IllegalArgumentException(
        "Impossible to reject a container. The moderation is possible only on a content or a post"
      );
    } else if (!isWaitingForApproval(content)) {
      throw new IllegalArgumentException(
        "Impossible to reject a post not defined being waiting for approval!"
      );
    }

    final Map<QName, Serializable> moderationProperties =
      getCommonModerationProperties(true);
    moderationProperties.put(
      ModerationModel.PROP_REJECT_MESSAGE,
      message == null ? "" : message
    );
    nodeService.addAspect(
      content,
      ModerationModel.ASPECT_REJECTED,
      moderationProperties
    );

    boolean wasEnable = false;

    policyBehaviourFilter.disableBehaviour(
      content,
      ModerationModel.ASPECT_REJECTED
    );
    try {
      wasEnable = !policyBehaviourFilter.isEnabled();
      policyBehaviourFilter.disableBehaviour(
        content,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
      // make a version with the rejected content and put an empty message.
      contentService
        .getWriter(content, ContentModel.PROP_CONTENT, true)
        .putContent("");
    } finally {
      policyBehaviourFilter.enableBehaviour(
        content,
        ModerationModel.ASPECT_REJECTED
      );

      if (wasEnable) {
        policyBehaviourFilter.enableBehaviour(
          content,
          ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
        );
      }
    }

    nodeService.removeAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL);
  }

  /**
   * Puts a post into the "waiting for approval" state so it is hidden from non-moderators.
   *
   * <p>Any existing {@code rejected} aspect is removed, the {@code waiting for approval} aspect is
   * added and permission inheritance is disabled so that only users with moderation permissions can
   * see the post.
   *
   * @param content the post to put on hold; must not be {@code null}
   * @throws IllegalArgumentException if {@code content} is a container
   */
  public void waitForApproval(final NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    if (isContainer(content)) {
      throw new IllegalArgumentException(
        "Impossible to make a container waiting for approval. The moderation is possible only on a content or a post"
      );
    }

    if (isRejected(content)) {
      nodeService.removeAspect(content, ModerationModel.ASPECT_REJECTED);
    }

    nodeService.addAspect(
      content,
      ModerationModel.ASPECT_WAITING_APPROVAL,
      null
    );
    /* Only NwsModerate permissions (transgress inheritance) will be allowed to see the post */
    permissionService.setInheritParentPermissions(content, false);
  }

  /**
   * Records an abuse report against a post.
   *
   * <p>The new report is appended to any existing reports; the {@code abuse signaled} aspect is
   * added (or its property updated) to store the full list of reports.
   *
   * @param content the post being reported; must not be {@code null}
   * @param message the abuse description supplied by the reporter
   * @return the newly created {@link AbuseReport}
   * @throws IllegalArgumentException if {@code content} is a container
   */
  public AbuseReport signalAbuse(NodeRef content, String message) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    if (isContainer(content)) {
      throw new IllegalArgumentException(
        "Impossible to make a container signaled for abuse. The moderation is possible only on a content or a post"
      );
    }

    final List<AbuseReport> abuses = getAbuses(content);
    final AbuseReportImpl newAbuse = new AbuseReportImpl(message);
    abuses.add(newAbuse);

    if (nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)) {
      nodeService.setProperty(
        content,
        ModerationModel.PROP_ABUSE_MESSAGES,
        (Serializable) abuses
      );
    } else {
      nodeService.addAspect(
        content,
        ModerationModel.ASPECT_ABUSE_SIGNALED,
        Collections.singletonMap(
          ModerationModel.PROP_ABUSE_MESSAGES,
          (Serializable) abuses
        )
      );
    }

    return newAbuse;
  }

  /**
   * Clears all abuse reports from a post, removing the {@code abuse signaled} aspect and its
   * associated property.
   *
   * @param content the post whose abuse reports must be cleared; must not be {@code null}
   * @throws IllegalArgumentException if {@code content} is a container or has no abuse reports
   */
  public void signalNotAbuse(NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    if (isContainer(content)) {
      throw new IllegalArgumentException(
        "Impossible to make a container signaled for abuse. The moderation is possible only on a content or a post"
      );
    } else if (
      !nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)
    ) {
      throw new IllegalArgumentException("No abuse defined on this node!");
    }

    nodeService.removeAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED);
    nodeService.removeProperty(content, ModerationModel.PROP_ABUSE_MESSAGES);
  }

  /**
   * Returns the abuse reports recorded against a post.
   *
   * @param content the post to inspect; must not be {@code null}
   * @return the list of {@link AbuseReport}s, or an empty (never {@code null}) list if none exist
   */
  @SuppressWarnings("unchecked")
  public List<AbuseReport> getAbuses(NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);
    final List<AbuseReport> reports;

    if (nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)) {
      reports = (List<AbuseReport>) nodeService.getProperty(
        content,
        ModerationModel.PROP_ABUSE_MESSAGES
      );
    } else {
      reports = null;
    }

    return (reports == null) ? new ArrayList<>() : reports;
  }

  /**
   * Indicates whether the container of the given node is currently moderated.
   *
   * <p>If {@code nodeRef} is itself a container it is tested directly; otherwise its primary parent
   * container is tested.
   *
   * @param nodeRef the container node, or a post whose parent container must be tested; must not be
   *     {@code null}
   * @return {@code true} if the resolved container carries the {@code moderated} aspect with the
   *     moderation flag set to {@code true}, {@code false} otherwise
   */
  public boolean isContainerModerated(NodeRef nodeRef) {
    ParameterCheck.mandatory("Container reference", nodeRef);

    final NodeRef container;

    if (isContainer(nodeRef)) {
      container = nodeRef;
    } else {
      container = nodeService.getPrimaryParent(nodeRef).getParentRef();
    }

    final Boolean hasAspect = nodeService.hasAspect(
      container,
      ModerationModel.ASPECT_MODERATED
    );

    if (Boolean.TRUE.equals(hasAspect)) {
      final Boolean isModerated = (Boolean) nodeService.getProperty(
        container,
        ModerationModel.PROP_IS_MODERATED
      );

      return isModerated != null && isModerated;
    } else {
      return false;
    }
  }

  /**
   * Indicates whether a post is currently waiting for approval.
   *
   * @param content the post to test; must not be {@code null}
   * @return {@code true} if the post carries the {@code waiting for approval} aspect
   */
  public boolean isWaitingForApproval(NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);

    return nodeService.hasAspect(
      content,
      ModerationModel.ASPECT_WAITING_APPROVAL
    );
  }

  /**
   * Indicates whether a post has been approved.
   *
   * @param content the post to test; must not be {@code null}
   * @return {@code true} if the post carries the {@code approved} aspect
   */
  public boolean isApproved(final NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);

    return nodeService.hasAspect(content, ModerationModel.ASPECT_APPROVED);
  }

  /**
   * Indicates whether a post has been rejected.
   *
   * @param content the post to test; must not be {@code null}
   * @return {@code true} if the post carries the {@code rejected} aspect
   */
  public boolean isRejected(NodeRef content) {
    ParameterCheck.mandatory(CONTENT_REFERENCE, content);

    return nodeService.hasAspect(content, ModerationModel.ASPECT_REJECTED);
  }

  private boolean isContainer(final NodeRef nodeRef) {
    final QName type = nodeService.getType(nodeRef);
    return (
      FORUM_CONTAINERS.contains(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)
    );
  }

  private Map<QName, Serializable> getCommonModerationProperties(
    boolean reject
  ) {
    final Map<QName, Serializable> moderationProps = HashMap.newHashMap(3);
    final Date date = new Date();
    final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

    if (reject) {
      moderationProps.put(ModerationModel.PROP_REJECT_ON, date);
      moderationProps.put(ModerationModel.PROP_REJECT_BY, currentUser);
    } else {
      moderationProps.put(ModerationModel.PROP_APPROVED_ON, date);
      moderationProps.put(ModerationModel.PROP_APPROVED_BY, currentUser);
    }

    return moderationProps;
  }

  /**
   * Stops moderation on a container and, recursively, on all of its child containers, resolving any
   * still-pending posts according to the requested action.
   *
   * <p>Pending posts are accepted (made visible) or rejected (with an automatic message) depending
   * on {@code action}, after which the {@code moderated} aspect and flag are removed from every
   * container.
   *
   * @param container the container node on which moderation must be stopped; must not be
   *     {@code null}
   * @param action the action to apply to pending posts; must be either {@code "accept"} or
   *     {@code "refuse"} and must not be {@code null}
   * @throws IllegalArgumentException if {@code container} is not a container node or if
   *     {@code action} is not a supported value
   */
  @Override
  public void stopModeration(NodeRef container, String action) {
    ParameterCheck.mandatory("The container", container);
    ParameterCheck.mandatory("The action", action);

    if (!isContainer(container)) {
      throw new IllegalArgumentException(
        "Impossible to apply moderation on a non-container node."
      );
    }

    if (
      !(action.equals(MODERATION_ACTION_ACCEPT) ||
        action.equals(MODERATION_ACTION_REFUSE))
    ) {
      throw new IllegalArgumentException(
        "Impossible to stop moderation, wrong action specified."
      );
    }

    stopModerationImpl(container, action);
  }

  private void stopModerationImpl(NodeRef container, String action) {
    if (isContainerModerated(container)) {
      for (final ChildAssociationRef assoc : nodeService.getChildAssocs(
        container
      )) {
        final NodeRef childRef = assoc.getChildRef();

        if (isContainer(childRef)) {
          stopModerationImpl(childRef, action);

          nodeService.removeAspect(childRef, ModerationModel.ASPECT_MODERATED);
          nodeService.removeProperty(
            childRef,
            ModerationModel.PROP_IS_MODERATED
          );
        } else if (action.equals(MODERATION_ACTION_ACCEPT)) {
          if (
            nodeService.hasAspect(
              childRef,
              ModerationModel.ASPECT_WAITING_APPROVAL
            )
          ) {
            accept(childRef);
          }
        } else if (
          action.equals(MODERATION_ACTION_REFUSE) &&
          nodeService.hasAspect(
            childRef,
            ModerationModel.ASPECT_WAITING_APPROVAL
          )
        ) {
          reject(childRef, MODERATION_ACTION_REFUSE_AUTO_MESSAGE);
        }
      }

      nodeService.removeAspect(container, ModerationModel.ASPECT_MODERATED);
      nodeService.removeProperty(container, ModerationModel.PROP_IS_MODERATED);
    }
  }
}
