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
package eu.cec.digit.circabc.action;

import org.alfresco.repo.action.ActionCancelledException;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.replication.ReplicationActionExecutor;
import org.alfresco.repo.replication.ReplicationDefinitionImpl;
import org.alfresco.repo.replication.ReplicationDefinitionPersisterImpl;
import org.alfresco.repo.replication.ReplicationParams;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.ChildAssociatedNodeFinder;
import org.alfresco.repo.transfer.ContentClassFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class CircabcReplicationActionExecutor extends ReplicationActionExecutor {

    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(CircabcReplicationActionExecutor.class);

    private NodeService nodeService;
    private JobLockService jobLockService;
    private TransferService2 transferService;
    private NodeCrawlerFactory nodeCrawlerFactory;
    private ActionTrackingService actionTrackingService;
    private TransactionService transactionService;
    private ReplicationDefinitionPersisterImpl replicationDefinitionPersister;
    private ReplicationParams replicationParams;
    private List<QName> excludedAspects = new ArrayList<>();

    private DictionaryService dictionaryService;

    /**
     * By default, we lock for a minute, so if this server is shutdown another can take over a minute
     * later.
     */
    private long replicationActionLockDuration = 60L * 1000;

    /**
     * Injects the NodeService bean.
     *
     * @param nodeService the NodeService.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Injects the JobLockService bean.
     *
     * @param jobLockService the JobLockService.
     */
    public void setJobLockService(JobLockService jobLockService) {
        this.jobLockService = jobLockService;
    }

    /**
     * Injects the TransferService bean.
     *
     * @param transferService the TransferService.
     */
    public void setTransferService(TransferService2 transferService) {
        this.transferService = transferService;
    }

    /**
     * Injects the NodeCrawlerFactory bean.
     *
     * @param nodeCrawlerFactory the NodeCrawlerFactory.
     */
    public void setNodeCrawlerFactory(NodeCrawlerFactory nodeCrawlerFactory) {
        this.nodeCrawlerFactory = nodeCrawlerFactory;
    }

    /**
     * Injects the ActionTrackingService bean.
     *
     * @param actionTrackingService the ActionTrackingService.
     */
    public void setActionTrackingService(ActionTrackingService actionTrackingService) {
        this.actionTrackingService = actionTrackingService;
    }

    /**
     * Injects the TransactionService bean.
     *
     * @param transactionService the TransactionService.
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Injects the ReplicationDefinitionPersister bean.
     */
    public void setReplicationDefinitionPersister(
            ReplicationDefinitionPersisterImpl replicationDefinitionPersister) {
        this.replicationDefinitionPersister = replicationDefinitionPersister;
    }

    /**
     * Sets Replication Parameters
     *
     * @param replicationParams replication parameters
     */
    public void setReplicationParams(ReplicationParams replicationParams) {
        this.replicationParams = replicationParams;
    }

    public void setExcludedAspects(String[] excludedAspects) {
        for (String aspect : excludedAspects) {
            this.excludedAspects.add(QName.createQName(aspect));
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // Not used - our definitions hold everything on them
    }

    /**
     * Takes a {@link ReplicationDefinition}, which contains one or more payloads {@link NodeRef}s,
     * and expands them into a full list of nodes to be transfered.
     */
    protected Set<NodeRef> expandPayload(ReplicationDefinition replicationDef) {
        Set<NodeRef> toTransfer = new HashSet<>(89);
        NodeCrawler crawler = nodeCrawlerFactory.getNodeCrawler();
        final Collection<QName> allAssociations = dictionaryService.getAllAssociations();
        crawler.setNodeFinders(new ChildAssociatedNodeFinder(
                new HashSet<>(allAssociations)
        ));
        final Collection<QName> allTypes = dictionaryService.getAllTypes();
        crawler.setNodeFilters(new ContentClassFilter(
                allTypes.toArray(new QName[allTypes.size()])
        ));

        for (NodeRef payload : replicationDef.getPayload()) {
            if (nodeService.exists(payload)) {
                Set<NodeRef> crawledNodes = crawler.crawl(payload);
                toTransfer.addAll(crawledNodes);
            } else {
                logger.warn("Skipping replication of non-existant node " + payload);
            }
        }

        return toTransfer;
    }

    /**
     * Takes a {@link ReplicationDefinition} and a list of {@link NodeRef}s, and returns the {@link
     * TransferDefinition} which will allow the replication to be run.
     */
    protected TransferDefinition buildTransferDefinition(
            ReplicationDefinition replicationDef, Set<NodeRef> toTransfer) {
        TransferDefinition transferDefinition =
                new TransferDefinition();
        transferDefinition.setNodes(toTransfer);
        transferDefinition.setSync(true);
        transferDefinition.setReadOnly(replicationParams.getTransferReadOnly());

        // Exclude aspects from transfer
        // NOTE: this list of aspects should be synced up with the NodeCrawler in expandPayload to
        //       ensure a coherent set of nodes are transferred
        transferDefinition.setExcludedAspects(excludedAspects);

        return transferDefinition;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (action instanceof ReplicationDefinition) {
            // Already of the correct type
        } else if (action.getActionDefinitionName().equals(ReplicationDefinitionImpl.EXECUTOR_NAME)) {
            // Specialise the action if needed, eg when loaded directly from
            //  the NodeRef without going via the replication service
            action = new ReplicationDefinitionImpl(action);
        }

        // Off we go
        final ReplicationDefinition replicationDef = (ReplicationDefinition) action;
        if (replicationDef.getTargetName() == null ||
                replicationDef.getTargetName().equals("")) {
            throw new ReplicationServiceException("The target is required but wasn't given");
        }
        if (replicationDef.getPayload().size() == 0) {
            throw new ReplicationServiceException("No payloads were specified");
        }
        if (!replicationDef.isEnabled()) {
            throw new ReplicationServiceException("Unable to execute a disabled replication definition");
        }

        // Lock the service - only one instance of the replication
        //  should occur at a time
        ReplicationDefinitionLockExtender lock =
                new ReplicationDefinitionLockExtender(replicationDef);

        // Turn our payload list of root nodes into something that
        //  the transfer service can work with
        Set<NodeRef> toTransfer;
        try {
            toTransfer = expandPayload(replicationDef);
        } catch (Exception e) {
            lock.close();
            throw new ReplicationServiceException("Error processing payload list - " + e.getMessage(), e);
        }

        // Ask the transfer service to do the replication
        //  work for us
        TransferEndEvent endEvent = null;
        try {
            // Build the definition
            TransferDefinition transferDefinition =
                    buildTransferDefinition(replicationDef, toTransfer);

            // Off we go
            endEvent = transferService.transfer(
                    replicationDef.getTargetName(),
                    transferDefinition,
                    lock);

            if (endEvent instanceof TransferEventCancelled) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cancelling replication job");
                }

                // If we were cancelled, throw the magic exception so
                //  that this is correctly recorded
                throw new ActionCancelledException(replicationDef);
            }

            // Record details of the transfer reports (in success case)
            replicationDef.setLocalTransferReport(endEvent.getSourceReport());
            replicationDef.setRemoteTransferReport(endEvent.getDestinationReport());
            replicationDefinitionPersister.saveReplicationDefinition(replicationDef);
        } catch (Exception e) {
            if (e instanceof ActionCancelledException && endEvent != null) {
                writeDefinitionReports(replicationDef, endEvent.getSourceReport(),
                        endEvent.getDestinationReport());
                throw (ActionCancelledException) e;
            }
            if (e instanceof TransferFailureException) {
                TransferEventError failureEndEvent = ((TransferFailureException) e).getErrorEvent();
                writeDefinitionReports(replicationDef, failureEndEvent.getSourceReport(),
                        failureEndEvent.getDestinationReport());
                Throwable cause = (e.getCause() == null) ? e : e.getCause();
                throw new ReplicationServiceException("Error executing transfer - " + cause.getMessage(),
                        cause);
            }
            writeDefinitionReports(replicationDef, null, null);
            throw new ReplicationServiceException("Error executing transfer - " + e.getMessage(), e);
        } finally {
            lock.close();
        }
    }

    private void writeDefinitionReports(final ReplicationDefinition replicationDef,
                                        NodeRef sourceReport, NodeRef destinationReport) {
        replicationDef.setLocalTransferReport(sourceReport);
        replicationDef.setRemoteTransferReport(destinationReport);

        if (replicationDef.getNodeRef() != null) {
            // Record details of the transfer reports
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                        public Object execute() throws Throwable {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Exception - writing replication def reports");
                            }

                            replicationDefinitionPersister.saveReplicationDefinition(replicationDef);
                            return null;
                        }
                    }, false, true);
        }
    }

    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * A {@link TransferCallback} which periodically renews the lock held against a {@link
     * ReplicationDefinition}
     */
    protected class ReplicationDefinitionLockExtender
            implements TransferCallback, JobLockService.JobLockRefreshCallback {

        private ReplicationDefinition replicationDef;
        private String transferId;
        private String lockToken;
        private boolean active;

        protected ReplicationDefinitionLockExtender(ReplicationDefinition replicationDef) {
            this.replicationDef = replicationDef;
            acquireLock();
        }

        /**
         * No matter what the event is, refresh our lock on the {@link ReplicationDefinition}, and
         * handle a cancel if it was requested.
         */
        public void processEvent(TransferEvent event) {
            // If it's the enter event, do skip
            if (event instanceof TransferEventEnterState) {
                return;
            }

            // If this is a begin event, make a note of the ID
            if (event instanceof TransferEventBegin) {
                transferId = ((TransferEventBegin) event).getTransferId();
            }

            checkCancel();
        }

        /**
         * Give up our lock on the {@link ReplicationDefinition}
         */
        public void close() {
            releaseLock();
        }

        /**
         * Get a lock on the job. Tries every 5 seconds for 30 seconds, then every 30 seconds for half
         * an hour.
         */
        private void acquireLock() {
            try {
                // Quick try
                lockToken = jobLockService.getLock(
                        replicationDef.getReplicationQName(),
                        replicationActionLockDuration,
                        5 * 1000, // Every 5 seconds
                        6         // 6 times = wait up to 30 seconds
                );

                active = true;

                /**
                 * Got the lock - now register the refresh callback which will keep the
                 * lock alive
                 */
                jobLockService.refreshLock(
                        lockToken,
                        replicationDef.getReplicationQName(),
                        replicationActionLockDuration,
                        this
                );

                if (logger.isDebugEnabled()) {
                    logger.debug("lock aquired:" + replicationDef.getReplicationQName());
                }
            } catch (LockAcquisitionException e) {
                long retryTime = 30L * 1000;
                int retries = 60;

                logger.debug(
                        "Unable to get the replication job lock on " +
                                replicationDef.getReplicationQName() +
                                ", retrying every " + (int) (retryTime / 1000) + " seconds"
                );

                active = true;

                // Long try - every 30 seconds
                lockToken = jobLockService.getLock(
                        replicationDef.getReplicationQName(),
                        replicationActionLockDuration,
                        retryTime,
                        retries
                );

                /**
                 * Got the lock - now register the refresh callback which will keep the
                 * lock alive
                 */
                jobLockService.refreshLock(
                        lockToken,
                        replicationDef.getReplicationQName(),
                        replicationActionLockDuration,
                        this
                );

                if (logger.isDebugEnabled()) {
                    logger.debug("lock aquired (from long timeout):" + replicationDef.getReplicationQName());
                }
            }
        }

        private void releaseLock() {
            if (active) {
                if (logger.isDebugEnabled()) {
                    logger.debug("about to release lock:" + replicationDef.getReplicationQName());
                }
                jobLockService.releaseLock(
                        lockToken,
                        replicationDef.getReplicationQName());
                active = false;
            }
        }

        private void checkCancel() {
            // Has someone tried to cancel us?
            if (actionTrackingService.isCancellationRequested(replicationDef)) {
                // Tell the transfer service to cancel, if we can
                if (transferId != null) {
                    transferService.cancelAsync(transferId);
                    logger.debug(
                            "Replication cancel was requested for " + replicationDef.getReplicationQName());
                } else {
                    logger.warn(
                            "Unable to cancel replication as requested, as transfer has yet to reach a cancellable state");
                }
            }
        }

        /**
         * Job Lock Refresh
         */
        @Override
        public boolean isActive() {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "lock callback isActive:" + active + ", " + replicationDef.getReplicationQName());
            }
            return active;
        }

        /**
         * Job Lock Service has released us.
         */
        @Override
        public void lockReleased() {
            logger.debug("lock released:" + replicationDef.getReplicationQName());
            // nothing to do
        }
    }
}
