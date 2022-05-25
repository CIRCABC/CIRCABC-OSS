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

import eu.cec.digit.circabc.model.CircabcModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class contains the behaviour behind the 'ci:circaContentHits' aspect.
 *
 * @author Roy Wetherall
 */
public class ContentHitsAspect implements
        ContentServicePolicies.OnContentReadPolicy,
        ContentServicePolicies.OnContentUpdatePolicy,
        NodeServicePolicies.OnAddAspectPolicy {

    /**
     * Aspect name
     */
    public static final QName ASPECT_CONTENT_HITS = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaContentHits");
    /**
     * Property names
     */
    public static final QName PROP_COUNT_STARTED_DATE = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "countStartedDate");
    public static final QName PROP_UPDATE_COUNT = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "updateCount");
    public static final QName PROP_READ_COUNT = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "readCount");
    /**
     * A key that keeps track of nodes that need read count increments
     */
    private static final String KEY_CONTENT_HITS_READS = ContentHitsAspect.class
            .getName()
            + ".reads";
    /**
     * A key that keeps track of nodes that need write count increments
     */
    private static final String KEY_CONTENT_HITS_WRITES = ContentHitsAspect.class
            .getName()
            + ".writes";
    private static final Log logger = LogFactory.getLog(ContentHitsAspect.class);

    private PolicyComponent policyComponent;

    private BehaviourFilter policyFilter;

    private NodeService nodeService;

    private TransactionService transactionService;

    private ThreadPoolExecutor threadExecuter;

    private TransactionListener transactionListener;

    /**
     * Default constructor for bean construction
     */
    public ContentHitsAspect() {
        this.transactionListener = new ContentHitsTransactionListener();
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
     * Sets the node service
     *
     * @param nodeService the node service
     */
    public void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set the service that allows new transactions
     *
     * @param transactionService the transaction service
     */
    public void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Set the thread pool that will handle the post-commit write transactions.
     *
     * @param threadExecuter an <i>executor</i>
     */
    public void setThreadExecuter(final ThreadPoolExecutor threadExecuter) {
        this.threadExecuter = threadExecuter;
    }

    /**
     * Spring initilaise method used to register the policy behaviours
     */
    public void initialise() {
        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_CONTENT_HITS, new JavaBehaviour(this, "onAddAspect",
                        NotificationFrequency.FIRST_EVENT));
        this.policyComponent.bindClassBehaviour(
                OnContentReadPolicy.QNAME, ASPECT_CONTENT_HITS,
                new JavaBehaviour(this, "onContentRead",
                        NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
                OnContentUpdatePolicy.QNAME, ASPECT_CONTENT_HITS,
                new JavaBehaviour(this, "onContentUpdate",
                        NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * onAddAspect policy behaviour.
     * <p>
     * Sets the count started date to the date/time at which the contentHits aspect was first
     * applied.
     *
     * @param nodeRef         the node reference
     * @param aspectTypeQName the qname of the aspect being applied
     */
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
        // Set the count started date
        this.nodeService.setProperty(nodeRef, PROP_COUNT_STARTED_DATE,
                new Date());
    }

    /**
     * onContentRead policy behaviour.
     * <p>
     * Increments the aspect's read count property by one.
     *
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy#onContentRead(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void onContentRead(final NodeRef nodeRef) {
        //		 Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes read
        @SuppressWarnings("unchecked")
        Set<NodeRef> readNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                .getResource(KEY_CONTENT_HITS_READS);
        if (readNodeRefs == null) {
            readNodeRefs = new HashSet<>(5);
            AlfrescoTransactionSupport.bindResource(KEY_CONTENT_HITS_READS,
                    readNodeRefs);
        }
        readNodeRefs.add(nodeRef);
    }

    /**
     * onContentUpdate policy behaviour.
     * <p>
     * Increments the aspect's update count property by one.
     *
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef,
     * boolean)
     */
    public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
        //		 Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
        // Get the set of nodes written
        @SuppressWarnings("unchecked")
        Set<NodeRef> writeNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                .getResource(KEY_CONTENT_HITS_WRITES);
        if (writeNodeRefs == null) {
            writeNodeRefs = new HashSet<>(5);
            AlfrescoTransactionSupport.bindResource(KEY_CONTENT_HITS_WRITES,
                    writeNodeRefs);
        }
        writeNodeRefs.add(nodeRef);
    }

    private class ContentHitsTransactionListener extends
            TransactionListenerAdapter {

        @Override
        public void afterCommit() {
            // Get all the nodes that need their read counts incremented
            @SuppressWarnings("unchecked") final Set<NodeRef> readNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                    .getResource(KEY_CONTENT_HITS_READS);
            if (readNodeRefs != null) {
                Runnable runnable;
                for (final NodeRef nodeRef : readNodeRefs) {
                    runnable = new ContentHitsReadCountIncrementer(
                            nodeRef);
                    threadExecuter.execute(runnable);
                }
            }
            @SuppressWarnings("unchecked") final Set<NodeRef> writeNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
                    .getResource(KEY_CONTENT_HITS_WRITES);

            if (writeNodeRefs != null) {
                Runnable runnable;
                for (final NodeRef nodeRef : writeNodeRefs) {
                    runnable = new ContentHitsWriteCountIncrementer(
                            nodeRef);
                    threadExecuter.execute(runnable);
                }
            }
        }
    }

    /**
     * The worker that will increment a node's content read count.
     *
     * @author Derek Hulley
     */
    private class ContentHitsReadCountIncrementer implements Runnable {

        private NodeRef nodeRef;

        private ContentHitsReadCountIncrementer(final NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }

        /**
         * Increments the read count on the node
         */
        public void run() {
            final RetryingTransactionHelper txnHelper = transactionService
                    .getRetryingTransactionHelper();
            final RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>() {
                public Integer execute() throws Throwable {
                    // Increment the read count property value
                    final Integer currentValue = (Integer) nodeService.getProperty(
                            nodeRef, PROP_READ_COUNT);
                    int newValue = currentValue + 1;
                    nodeService.setProperty(nodeRef, PROP_READ_COUNT, newValue);
                    return (Integer) newValue;
                }
            };
            try {
                // Ensure that the policy doesn't refire for this node on this thread
                // This won't prevent background processes from refiring, though
                policyFilter.disableBehaviour(nodeRef, ASPECT_CONTENT_HITS);

                final Integer newCount = txnHelper.doInTransaction(callback, false,
                        true);
                // Done
                if (logger.isDebugEnabled()) {
                    logger.debug("Incremented content read count on node: \n"
                            + "   Node:      " + nodeRef + "\n"
                            + "   New Count: " + newCount);
                }
            } catch (final InvalidNodeRefException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Unable to increment content read count on missing node: "
                                    + nodeRef);
                }
            } catch (final Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e);
                }
                logger.error("Failed to increment content read count on node: "
                        + nodeRef);
                // We are the last call on the thread
            } finally {
                policyFilter.enableBehaviour(ASPECT_CONTENT_HITS);
            }
        }
    }

    /**
     * The worker that will increment a node's content write count.
     *
     * @author Derek Hulley
     */
    private class ContentHitsWriteCountIncrementer implements Runnable {

        private NodeRef nodeRef;

        private ContentHitsWriteCountIncrementer(final NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }

        /**
         * Increments the write count on the node
         */
        public void run() {
            final RetryingTransactionHelper txnHelper = transactionService
                    .getRetryingTransactionHelper();
            final RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>() {
                public Integer execute() throws Throwable {
                    // Increment the read count property value
                    final Integer currentValue = (Integer) nodeService.getProperty(
                            nodeRef, PROP_UPDATE_COUNT);
                    int newValue = currentValue + 1;
                    nodeService.setProperty(nodeRef, PROP_UPDATE_COUNT,
                            newValue);
                    return (Integer) newValue;
                }
            };
            try {
                // Ensure that the policy doesn't refire for this node on this thread
                // This won't prevent background processes from refiring, though
                policyFilter.disableBehaviour(nodeRef, ASPECT_CONTENT_HITS);

                final Integer newCount = txnHelper.doInTransaction(callback, false,
                        true);
                // Done
                if (logger.isDebugEnabled()) {
                    logger.debug("Incremented content write count on node: \n"
                            + "   Node:      " + nodeRef + "\n"
                            + "   New Count: " + newCount);
                }
            } catch (final InvalidNodeRefException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Unable to increment content write count on missing node: "
                                    + nodeRef);
                }
            } catch (final Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e);
                }
                if (logger.isErrorEnabled()) {
                    logger
                            .error("Failed to increment content write count on node: "
                                    + nodeRef);
                }
                // We are the last call on the thread
            } finally {
                policyFilter.enableBehaviour(ASPECT_CONTENT_HITS);
            }
        }
    }
}
