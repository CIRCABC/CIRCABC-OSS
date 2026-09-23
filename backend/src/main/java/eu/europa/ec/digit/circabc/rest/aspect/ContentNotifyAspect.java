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
package eu.europa.ec.digit.circabc.rest.aspect;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.NotifiableUser;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Alfresco content model behaviour (aspect) that triggers e-mail notifications
 * to subscribed users when content carrying the {@code circaContentNotify}
 * aspect is created, updated or moved within the CIRCABC repository.
 * <p>
 * The aspect binds three Alfresco policy behaviours during Spring
 * initialisation:
 * <ul>
 *   <li>{@code onAddAspect} (fired on the first event) &mdash; records nodes
 *       that were newly created and need notification;</li>
 *   <li>{@code onContentUpdate} (fired at transaction commit) &mdash; records
 *       nodes whose content changed and need notification;</li>
 *   <li>{@code onMoveNode} &mdash; immediately schedules a notification for the
 *       moved node.</li>
 * </ul>
 * Created and updated node references are accumulated on the transaction via
 * {@link org.alfresco.util.transaction.TransactionSupportUtil} and processed
 * after the transaction commits by an internal
 * {@link org.alfresco.util.transaction.TransactionListener}. The actual
 * notification work is delegated to background {@link Runnable} tasks executed
 * on a dedicated {@link java.util.concurrent.ThreadPoolExecutor}, each running
 * in its own retrying transaction as the system user.
 * <p>
 * Notification dispatch can be suppressed for the current thread through
 * {@link io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal}, and
 * the post-commit merging strategy differs depending on whether the platform is
 * configured for ECHA (see {@link io.swagger.config.CircabcConfig#isECHA()}).
 */
public class ContentNotifyAspect
  implements
    ContentServicePolicies.OnContentUpdatePolicy,
    NodeServicePolicies.OnAddAspectPolicy,
    NodeServicePolicies.OnMoveNodePolicy
{

  /**
   * Aspect name
   */
  public static final QName ASPECT_CONTENT_NOTIFY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaContentNotify"
  );
  private static final Log logger = LogFactory.getLog(
    ContentNotifyAspect.class
  );
  /**
   * A key that keeps track of nodes that are created and need to notify users
   */
  private static final String KEY_CONTENT_NOTIFY_CREATE =
    ContentNotifyAspect.class.getName() + ".created";
  /**
   * A key that keeps track of nodes that are updated and need to notify users
   */
  private static final String KEY_CONTENT_NOTIFY_UPDATE =
    ContentNotifyAspect.class.getName() + ".updated";

  /**
   * Service responsible for building and sending the actual notification
   * e-mails for a node and its recipients.
   */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /**
   * Service that resolves the set of {@link NotifiableUser}s subscribed to a
   * given node.
   */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Filter used to temporarily disable this aspect's behaviour on a node while
   * it is being processed, preventing the policy from re-firing recursively.
   */
  @Autowired
  @Qualifier("policyBehaviourFilter")
  private BehaviourFilter policyFilter;

  /**
   * Provides the retrying transaction helper used to run notification work in
   * its own transaction.
   */
  @Autowired
  private TransactionService transactionService;

  /**
   * Dedicated thread pool on which notification tasks are executed
   * asynchronously, off the request/commit thread.
   */
  @Autowired
  @Qualifier("notificationAspectThreadPoolExecutor")
  private ThreadPoolExecutor threadExecutor;

  /**
   * Alfresco component used to bind this aspect's policy behaviours during
   * initialisation.
   */
  @Autowired
  private PolicyComponent policyComponent;

  /**
   * CIRCABC configuration, used here mainly to select the ECHA-specific
   * notification merging strategy.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Transaction listener that, after commit, processes the created and updated
   * node references accumulated during the transaction.
   */
  private TransactionListener transactionListener;

  /**
   * Spring initilaise method used to register the policy behaviours
   */
  @PostConstruct
  public void initialise() {
    transactionListener = new ContentNotifyTransactionListener(circabcConfig);
    // Register the policy behaviours
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
      ASPECT_CONTENT_NOTIFY,
      new JavaBehaviour(this, "onAddAspect", NotificationFrequency.FIRST_EVENT)
    );

    this.policyComponent.bindClassBehaviour(
      OnContentUpdatePolicy.QNAME,
      ASPECT_CONTENT_NOTIFY,
      new JavaBehaviour(
        this,
        "onContentUpdate",
        NotificationFrequency.TRANSACTION_COMMIT
      )
    );

    this.policyComponent.bindClassBehaviour(
      NodeServicePolicies.OnMoveNodePolicy.QNAME,
      ASPECT_CONTENT_NOTIFY,
      new JavaBehaviour(this, "onMoveNode")
    );
  }

  /**
   * onAddAspect policy behaviour.
   * <p>
   * Save node reference that will be processed after commit
   * <p>
   * when aspect was first applied (content is created).
   *
   * @param nodeRef         the node reference
   * @param aspectTypeQName the qname of the aspect being applied
   */
  @Override
  public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
    if (isNotificationDisabled()) {
      return;
    }
    // Bind the listener to the transaction
    AlfrescoTransactionSupport.bindListener(transactionListener);
    // Get the set of nodes written
    @SuppressWarnings("unchecked")
    Set<NodeRef> createNodeRefs = (Set<
      NodeRef
    >) TransactionSupportUtil.getResource(KEY_CONTENT_NOTIFY_CREATE);

    if (createNodeRefs == null) {
      createNodeRefs = HashSet.newHashSet(5);
      TransactionSupportUtil.bindResource(
        KEY_CONTENT_NOTIFY_CREATE,
        createNodeRefs
      );
    }
    createNodeRefs.add(nodeRef);
  }

  /**
   * onContentUpdate policy behaviour.
   * <p>
   * Save node reference that will be processed after commit.
   *
   * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef,
   *      boolean)
   */
  @Override
  public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
    if (isNotificationDisabled()) {
      return;
    }
    // Bind the listener to the transaction
    AlfrescoTransactionSupport.bindListener(transactionListener);
    // Get the set of nodes written
    @SuppressWarnings("unchecked")
    Set<NodeRef> updateNodeRefs = (Set<
      NodeRef
    >) TransactionSupportUtil.getResource(KEY_CONTENT_NOTIFY_UPDATE);

    if (updateNodeRefs == null) {
      updateNodeRefs = HashSet.newHashSet(5);
      TransactionSupportUtil.bindResource(
        KEY_CONTENT_NOTIFY_UPDATE,
        updateNodeRefs
      );
    }

    updateNodeRefs.add(nodeRef);
  }

  /**
   * onMoveNode policy behaviour.
   * <p>
   * Immediately schedules a notification task for the moved node on the
   * notification thread pool (the move is not deferred to transaction commit).
   *
   * @param oldChildAssocRef the child association reference before the move
   * @param newChildAssocRef the child association reference after the move;
   *                         its child node is the one for which subscribers are
   *                         notified
   */
  @Override
  public void onMoveNode(
    ChildAssociationRef oldChildAssocRef,
    ChildAssociationRef newChildAssocRef
  ) {
    if (isNotificationDisabled()) {
      return;
    }
    NodeRef nodeRef = newChildAssocRef.getChildRef();
    Runnable runnable;
    runnable = new ContentNotifyer(nodeRef);
    threadExecutor.execute(runnable);
  }

  private boolean isNotificationDisabled() {
    DisableNotificationThreadLocal isNotificationDisabled =
      new DisableNotificationThreadLocal();
    return Boolean.TRUE.equals(isNotificationDisabled.get());
  }

  private class ContentNotifyTransactionListener
    extends TransactionListenerAdapter
  {

    private CircabcConfig circabcConfig;

    public ContentNotifyTransactionListener(CircabcConfig circabcConfig) {
      this.circabcConfig = circabcConfig;
    }

    @Override
    public void afterCommit() {
      if (isNotificationDisabled()) {
        return;
      }

      Set<NodeRef> readNodeRefs = getNodeRefsFromResource(
        KEY_CONTENT_NOTIFY_CREATE
      );
      Set<NodeRef> writeNodeRefs = getNodeRefsFromResource(
        KEY_CONTENT_NOTIFY_UPDATE
      );

      if (circabcConfig.isECHA()) {
        processECHANotifications(readNodeRefs, writeNodeRefs);
      } else {
        processStandardNotifications(readNodeRefs, writeNodeRefs);
      }
    }

    @SuppressWarnings("unchecked")
    private Set<NodeRef> getNodeRefsFromResource(String key) {
      return (Set<NodeRef>) TransactionSupportUtil.getResource(key);
    }

    private void processECHANotifications(
      Set<NodeRef> readNodeRefs,
      Set<NodeRef> writeNodeRefs
    ) {
      Set<NodeRef> mergedRefs = new HashSet<>();
      if (writeNodeRefs != null) {
        mergedRefs.addAll(writeNodeRefs);
      }
      if (readNodeRefs != null) {
        mergedRefs.addAll(readNodeRefs);
      }
      executeNotifications(mergedRefs);
    }

    private void processStandardNotifications(
      Set<NodeRef> readNodeRefs,
      Set<NodeRef> writeNodeRefs
    ) {
      executeNotifications(readNodeRefs);
      executeNotifications(writeNodeRefs);
    }

    private void executeNotifications(Set<NodeRef> nodeRefs) {
      if (nodeRefs == null || nodeRefs.isEmpty()) {
        return;
      }

      for (final NodeRef nodeRef : nodeRefs) {
        Runnable runnable = new ContentNotifyer(nodeRef);
        threadExecutor.execute(runnable);
      }
    }

    private boolean isNotificationDisabled() {
      return ContentNotifyAspect.this.isNotificationDisabled();
    }
  }

  private final class ContentNotifyer implements Runnable {

    private NodeRef nodeRef;

    private ContentNotifyer(final NodeRef nodeRef) {
      this.nodeRef = nodeRef;
    }

    /**
     * Send email notifications
     */
    @Override
    @SuppressWarnings("java:S1143")
    public void run() {
      final RetryingTransactionHelper txnHelper =
        transactionService.getRetryingTransactionHelper();

      final RetryingTransactionCallback<Object> callback = () -> {
        final Set<NotifiableUser> users =
          notificationSubscriptionService.getNotifiableUsers(nodeRef);

        notificationService.notify(nodeRef, users);

        return null;
      };

      try {
        // Migration 3.1 -> 3.4.6 - 02/01/2012 - Wrapped disableBehaviour in a
        // transaction
        final RetryingTransactionCallback<Object> policyCallback = () -> {
          // Ensure that the policy doesn't refire for this node on this
          // thread
          // This won't prevent background processes from refiring, though
          policyFilter.disableBehaviour(nodeRef, ASPECT_CONTENT_NOTIFY);

          return null;
        };

        txnHelper.doInTransaction(policyCallback, false, true);

        AuthenticationUtil.runAs(
          () -> txnHelper.doInTransaction(callback, false, true),
          AuthenticationUtil.getSystemUserName()
        );
        // Done
      } catch (final InvalidNodeRefException e) {
        // Node reference is invalid - nothing to do
      } catch (final Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to send notification content: " + nodeRef, e);
        }
        // We are the last call on the thread
      } finally {
        txnHelper.doInTransaction(
          () -> {
            policyFilter.enableBehaviour(nodeRef, ASPECT_CONTENT_NOTIFY);
            return null;
          },
          false,
          true
        );
        // clean threadlocals Alfresco memory leak
      }
    }
  }
}
