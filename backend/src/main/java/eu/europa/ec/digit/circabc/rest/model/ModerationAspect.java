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
package eu.europa.ec.digit.circabc.rest.model;

import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import io.swagger.model.alfresco.ModerationModel;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco behaviour bean backing the CIRCABC moderation content model.
 *
 * <p>This class binds a set of Alfresco policy behaviours to the moderation-related aspects declared
 * in {@link ModerationModel} (notably {@link ModerationModel#ASPECT_MODERATED the moderation
 * aspect}, {@link ModerationModel#ASPECT_REJECTED the rejected aspect} and {@link
 * ModerationModel#ASPECT_WAITING_APPROVAL the waiting-for-approval aspect}). Through the {@link
 * PolicyComponent} it reacts to repository events so that moderation rules are enforced
 * automatically inside a moderated newsgroup/forum hierarchy:
 *
 * <ul>
 *   <li>content newly created under a moderated container is either marked as moderated (containers)
 *       or placed in the "waiting for approval" state (posts);
 *   <li>replies to posts that are "waiting for approval" or "rejected" are forbidden;
 *   <li>updating the content of a rejected post moves it back to the "waiting for approval" state.
 * </ul>
 *
 * <p>It implements {@link NodeServicePolicies.OnCreateChildAssociationPolicy} and {@link
 * ContentServicePolicies.OnContentUpdatePolicy} and delegates the actual moderation state
 * transitions to {@link ModerationService}.
 *
 * @author Yanick Pignot
 */
public class ModerationAspect
  implements
    NodeServicePolicies.OnCreateChildAssociationPolicy,
    ContentServicePolicies.OnContentUpdatePolicy
{

  /**
   * Name of the {@code onCreateChildAssociation} policy method, used both as the Alfresco policy
   * QName local part and as the Java method name bound in {@link JavaBehaviour} instances.
   */
  private static final String ON_CREATE_CHILD_ASSOCIATION =
    "onCreateChildAssociation";

  /**
   * Forum node types that are considered containers (as opposed to leaf posts) when deciding how
   * moderation should be applied to a newly created child. See {@link #isContainer(QName)}.
   */
  private static final List<QName> FORUM_CONTAINERS = Arrays.asList(
    ForumModel.TYPE_FORUMS,
    ForumModel.TYPE_FORUM,
    ForumModel.TYPE_TOPIC
  );

  /** Alfresco component used to register (bind) the moderation policy behaviours. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Service that reads and mutates the moderation state of nodes. */
  @Autowired
  private ModerationService moderationService;

  /** Alfresco dictionary service used to resolve type/sub-type relationships. */
  @Autowired
  private DictionaryService dictionaryService;

  /** Alfresco node service used to inspect node types. */
  @Autowired
  private NodeService nodeService;

  /**
   * Registers all moderation behaviours with the {@link PolicyComponent} once the bean has been
   * constructed and its dependencies injected.
   *
   * <p>Binds the {@code onCreateChildAssociation}, {@code onCreateReplyAssociation} and {@code
   * onContentUpdate} policies to the relevant moderation aspects so that moderation rules are
   * enforced automatically by the repository.
   */
  @PostConstruct
  public void init() {
    this.policyComponent.bindAssociationBehaviour(
      QName.createQName(
        NamespaceService.ALFRESCO_URI,
        ON_CREATE_CHILD_ASSOCIATION
      ),
      ModerationModel.ASPECT_MODERATED,
      new JavaBehaviour(this, ON_CREATE_CHILD_ASSOCIATION)
    );

    this.policyComponent.bindClassBehaviour(
      QName.createQName(
        NamespaceService.ALFRESCO_URI,
        "onCreateReplyAssociation"
      ),
      ModerationModel.ASPECT_REJECTED,
      new JavaBehaviour(this, ON_CREATE_CHILD_ASSOCIATION)
    );

    this.policyComponent.bindClassBehaviour(
      QName.createQName(
        NamespaceService.ALFRESCO_URI,
        "onCreateReplyAssociation"
      ),
      ModerationModel.ASPECT_WAITING_APPROVAL,
      new JavaBehaviour(this, ON_CREATE_CHILD_ASSOCIATION)
    );

    this.policyComponent.bindClassBehaviour(
      OnContentUpdatePolicy.QNAME,
      ModerationModel.ASPECT_REJECTED,
      new JavaBehaviour(this, "onContentUpdate")
    );
  }

  /**
   * {@link OnContentUpdatePolicy} handler: when the content of a rejected post is updated, move the
   * post back to the "waiting for approval" state so that it is re-submitted to a moderator.
   *
   * @param nodeRef the node whose content has just been updated
   * @param newContent {@code true} if this update created brand new content, {@code false} if it
   *     updated existing content (as provided by the Alfresco policy contract)
   */
  public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
    if (moderationService.isRejected(nodeRef)) {
      moderationService.waitForApproval(nodeRef);
    }
  }

  /**
   * Prevents replies from being created against posts that are not in a repliable state.
   *
   * <p>For referencing associations ({@link ContentModel#ASPECT_REFERENCING}), this rejects the
   * operation when the parent post is either "waiting for approval" or "rejected".
   *
   * @param childAssocRef the child association being created; its parent is the post being replied
   *     to
   * @param isNewNode {@code true} if the child is a newly created node (required by the Alfresco
   *     {@code OnCreateChildAssociationPolicy} contract; not used here)
   * @throws IllegalStateException if the parent post is waiting for approval or has been rejected
   */
  @SuppressWarnings("java:S1172") // isNewNode required by Alfresco OnCreateChildAssociationPolicy contract
  public void onCreateReplyAssociation(
    final ChildAssociationRef childAssocRef,
    final boolean isNewNode
  ) {
    if (childAssocRef.getQName().equals(ContentModel.ASPECT_REFERENCING)) {
      final NodeRef parentRef = childAssocRef.getParentRef();

      if (moderationService.isWaitingForApproval(parentRef)) {
        throw new IllegalStateException(
          "Reply to a 'waiting for approval' post is not allowed."
        );
      } else if (moderationService.isRejected(parentRef)) {
        throw new IllegalStateException(
          "Reply to a 'rejected' post is not allowed."
        );
      }
    }
  }

  /**
   * {@link NodeServicePolicies.OnCreateChildAssociationPolicy} handler: propagates moderation to
   * children created under a moderated container.
   *
   * <p>If the parent container is moderated, a newly created child container is itself marked as
   * moderated, while a newly created leaf post is placed in the "waiting for approval" state.
   *
   * @param childAssocRef the child association just created; provides both the parent and the child
   *     nodes
   * @param isNewNode {@code true} if the child is a newly created node, {@code false} otherwise (as
   *     provided by the Alfresco policy contract)
   */
  public void onCreateChildAssociation(
    final ChildAssociationRef childAssocRef,
    final boolean isNewNode
  ) {
    final NodeRef parentRef = childAssocRef.getParentRef();
    if (moderationService.isContainerModerated(parentRef)) {
      final NodeRef childRef = childAssocRef.getChildRef();
      final QName childType = nodeService.getType(childRef);

      if (isContainer(childType)) {
        moderationService.applyModeration(childRef, false);
      } else {
        moderationService.waitForApproval(childRef);
      }
    }
  }

  /**
   * Determines whether a given forum node type should be treated as a container (rather than a leaf
   * post) for moderation purposes.
   *
   * @param childType the content model type of the child node to classify
   * @return {@code true} if the type is not a plain forum post, is one of the known forum container
   *     types, or is a sub-type of {@link ContentModel#TYPE_FOLDER}; {@code false} otherwise
   */
  private boolean isContainer(final QName childType) {
    return (
      !childType.equals(ForumModel.TYPE_POST) ||
      FORUM_CONTAINERS.contains(childType) ||
      dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER)
    );
  }
}
