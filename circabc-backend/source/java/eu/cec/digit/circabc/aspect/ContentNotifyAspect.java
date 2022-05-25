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
package eu.cec.digit.circabc.aspect;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
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
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class ContentNotifyAspect implements
        ContentServicePolicies.OnContentUpdatePolicy,
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnMoveNodePolicy {

    /**
     * Aspect name
     */
    public static final QName ASPECT_CONTENT_NOTIFY = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaContentNotify");
    private static final Log logger = LogFactory.getLog(ContentNotifyAspect.class);
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
    private NotificationService notificationService;
    private NotificationSubscriptionService notificationSubscriptionService;

    private BehaviourFilter policyFilter;

    private TransactionService transactionService;

    private ThreadPoolExecutor threadExecutor;

    private TransactionListener transactionListener;
    /**
     * The policy component
     */
    private PolicyComponent policyComponent;

    public ContentNotifyAspect() {
        this.transactionListener = new ContentNotifyTransactionListener();
    }

    /**
     * Set the thread pool that will handle the post-commit write transactions.
     *
     * @param threadExecutor an <i>executor</i>
     */
    public void setThreadExecutor(final ThreadPoolExecutor threadExecutor) {
        this.threadExecutor = threadExecutor;
    }

    /**
     * Spring initilaise method used to register the policy behaviours
     */
    public void initialise() {

        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_CONTENT_NOTIFY,
                new JavaBehaviour(this, "onAddAspect", NotificationFrequency.FIRST_EVENT));


        this.policyComponent.bindClassBehaviour(
                OnContentUpdatePolicy.QNAME,
                ASPECT_CONTENT_NOTIFY,
                new JavaBehaviour(this, "onContentUpdate", NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ASPECT_CONTENT_NOTIFY,
                new JavaBehaviour(this, "onMoveNode"));

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
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
        //		 Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes written
        @SuppressWarnings("unchecked")
        Set<NodeRef> createNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                .getResource(KEY_CONTENT_NOTIFY_CREATE);

        if (createNodeRefs == null) {
            createNodeRefs = new HashSet<>(5);
            AlfrescoTransactionSupport.bindResource(KEY_CONTENT_NOTIFY_CREATE, createNodeRefs);
        }
        createNodeRefs.add(nodeRef);
    }

    /**
     * onContentUpdate policy behaviour.
     * <p>
     * Save node reference that will be processed after commit.
     *
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef,
     * boolean)
     */
    public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
        //		 Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes written
        @SuppressWarnings("unchecked")
        Set<NodeRef> updateNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                .getResource(KEY_CONTENT_NOTIFY_UPDATE);

        if (updateNodeRefs == null) {
            updateNodeRefs = new HashSet<>(5);
            AlfrescoTransactionSupport.bindResource(KEY_CONTENT_NOTIFY_UPDATE, updateNodeRefs);
        }

        updateNodeRefs.add(nodeRef);

    }

    /**
     * @param notificationService the notificationService to set
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * @return the notificationSubscriptionService
     */
    protected final NotificationSubscriptionService getNotificationSubscriptionService() {
        return notificationSubscriptionService;
    }

    /**
     * @param notificationSubscriptionService the notificationSubscriptionService to set
     */
    public final void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }

    /**
     * Sets the policy component
     *
     * @param policyComponent the policy component
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the component to filter out behaviour
     *
     * @param policyFilter the policy behaviour filter
     */
    public void setPolicyFilter(final BehaviourFilter policyFilter) {
        this.policyFilter = policyFilter;
    }

    /**
     * Set the service that allows new transactions
     *
     * @param transactionService the transaction service
     */
    public void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
        DisableNotificationThreadLocal isNotificationDisabled = new DisableNotificationThreadLocal();
        if (isNotificationDisabled.get()) {
            return;
        }
        NodeRef nodeRef = newChildAssocRef.getChildRef();
        if (logger.isDebugEnabled()) {
            NodeRef oldParent = oldChildAssocRef.getParentRef();
            NodeRef newParent = newChildAssocRef.getParentRef();

            logger.info("move node: " + nodeRef.toString() + " from " + oldParent.toString() + " to " + newParent.toString());

        }
        Runnable runnable;
        runnable = new ContentNotifyer(nodeRef);
        threadExecutor.execute(runnable);
    }

    private class ContentNotifyTransactionListener extends TransactionListenerAdapter {

        @Override
        public void afterCommit() {

            DisableNotificationThreadLocal isNotificationDisabled = new DisableNotificationThreadLocal();
            if (isNotificationDisabled.get()) {
                return;
            }

            @SuppressWarnings("unchecked")
            Set<NodeRef> readNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                    .getResource(KEY_CONTENT_NOTIFY_CREATE);
            @SuppressWarnings("unchecked")
            Set<NodeRef> writeNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                    .getResource(KEY_CONTENT_NOTIFY_UPDATE);

            if (CircabcConfig.ECHA) {
                // added to avoid ECHA double notification with CMIS

                if (writeNodeRefs == null) {
                    writeNodeRefs = readNodeRefs;
                } else if (readNodeRefs != null) {
                    writeNodeRefs.addAll(readNodeRefs);
                }

                if (writeNodeRefs != null) {
                    Runnable runnable;
                    for (final NodeRef nodeRef : writeNodeRefs) {
                        runnable = new ContentNotifyer(nodeRef);
                        threadExecutor.execute(runnable);
                    }
                }
            } else {
                // original CIRCABC behaviour

                if (readNodeRefs != null) {
                    Runnable runnable;
                    for (final NodeRef nodeRef : readNodeRefs) {
                        runnable = new ContentNotifyer(nodeRef);
                        threadExecutor.execute(runnable);
                    }
                }

                if (writeNodeRefs != null) {
                    Runnable runnable;
                    for (final NodeRef nodeRef : writeNodeRefs) {
                        runnable = new ContentNotifyer(nodeRef);
                        threadExecutor.execute(runnable);
                    }
                }
            }
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
        public void run() {
            final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

            final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                public Object execute() throws Throwable {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Trying to notify the creation/update of the node " + nodeRef);
                    }

                    final Set<NotifiableUser> users = notificationSubscriptionService
                            .getNotifiableUsers(nodeRef);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Users successfully retreived: " + users);
                    }

                    notificationService.notify(nodeRef, users);

                    if (logger.isDebugEnabled()) {
                        logger.debug("User successfully ntified for the node " + nodeRef);
                    }

                    return null;
                }
            };

            try {
                // Migration 3.1 -> 3.4.6 - 02/01/2012 - Wrapped disableBehaviour in a transaction
                final RetryingTransactionCallback<Object> policyCallback = new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        // Ensure that the policy doesn't refire for this node on this
                        // thread
                        // This won't prevent background processes from refiring, though
                        policyFilter.disableBehaviour(nodeRef, ASPECT_CONTENT_NOTIFY);

                        return null;
                    }
                };

                txnHelper.doInTransaction(policyCallback, false, true);

                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                    public Object doWork() {
                        return txnHelper.doInTransaction(callback, false, true);
                    }
                }, AuthenticationUtil.getSystemUserName());

                // Done

            } catch (final InvalidNodeRefException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable send notification content on missing node: " + nodeRef);
                }
            } catch (final Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e);
                }
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to send notification content: " + nodeRef, e);
                }
                // We are the last call on the thread
            } finally {
                txnHelper.doInTransaction(new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        policyFilter.enableBehaviour(ASPECT_CONTENT_NOTIFY);
                        return null;
                    }
                }, false, true);
                // clean threadlocals  Alfresco memory leak
            }
        }
    }
}
