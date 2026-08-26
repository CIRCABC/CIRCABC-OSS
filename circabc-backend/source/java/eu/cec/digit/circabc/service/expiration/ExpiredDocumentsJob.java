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
package eu.cec.digit.circabc.service.expiration;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.lock.LockService;
import javax.transaction.UserTransaction;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled job that automatically deletes expired documents. A document is considered expired when
 * its cd:expiration_date property is in the past. Deletion is permanent (bypasses the archive store
 * / trashcan) by applying the cm:temporary aspect before deletion.
 *
 * <p>This job is intended to be enabled only for OLAF environments via the property
 * expired.documents.job.enabled=true in alfresco-global.properties.
 *
 * @author morleal
 */
public class ExpiredDocumentsJob implements Job {

  private static final String JOB_LOCK_NAME = "EXPIRED_DOCUMENTS_JOB";

  private static final String PROP_EXPIRATION_DATE = Repository.escapeQName(
    DocumentModel.PROP_EXPIRATION_DATE
  );

  /** Lucene query: all documents with expiration date before now. */
  private static final String QUERY =
    "@" + PROP_EXPIRATION_DATE + ":[MIN TO NOW]";

  private static final Log logger = LogFactory.getLog(
    ExpiredDocumentsJob.class
  );

  private LockService lockService;
  private NodeService nodeService;
  private SearchService searchService;
  private TransactionService transactionService;

  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    initialize(context);
    try {
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
      if (!lockService.isLocked(JOB_LOCK_NAME)) {
        boolean isLocked = false;
        try {
          lockService.lock(JOB_LOCK_NAME);
          isLocked = true;
          deleteExpiredDocuments();
        } catch (Exception e) {
          logger.error("Error running ExpiredDocumentsJob", e);
        } finally {
          if (isLocked) {
            lockService.unlock(JOB_LOCK_NAME);
          }
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
            "ExpiredDocumentsJob is already locked, skipping execution."
          );
        }
      }
    } catch (Exception e) {
      logger.error("Cannot run ExpiredDocumentsJob", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  private void deleteExpiredDocuments() {
    ResultSet results = null;
    int deletedCount = 0;
    int skippedCount = 0;
    int errorCount = 0;
    try {
      results = searchService.query(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        SearchService.LANGUAGE_LUCENE,
        QUERY
      );

      if (logger.isInfoEnabled()) {
        logger.info(
          "ExpiredDocumentsJob: found " +
          results.length() +
          " expired document(s) to process."
        );
      }

      for (ResultSetRow row : results) {
        NodeRef nodeRef = row.getNodeRef();
        UserTransaction trx = null;
        try {
          trx = transactionService.getNonPropagatingUserTransaction(false);
          trx.begin();
          if (nodeService.exists(nodeRef)) {
            // Apply cm:temporary aspect to bypass the archive store (no trashcan)
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(nodeRef);
            trx.commit();
            deletedCount++;
            if (logger.isDebugEnabled()) {
              logger.debug("Permanently deleted expired document: " + nodeRef);
            }
          } else {
            // Node already deleted (stale index entry), just rollback the empty transaction
            trx.rollback();
            skippedCount++;
            if (logger.isDebugEnabled()) {
              logger.debug(
                "Skipping already deleted node (stale index): " + nodeRef
              );
            }
          }
        } catch (InvalidNodeRefException e) {
          // Node was deleted between the exists() check and the actual operation
          skippedCount++;
          if (logger.isDebugEnabled()) {
            logger.debug("Skipping node already deleted: " + nodeRef);
          }
          if (trx != null) {
            try {
              trx.rollback();
            } catch (Exception rollbackEx) {
              // ignore
            }
          }
        } catch (Exception e) {
          // Check if this is a RollbackException caused by a deleted node (stale index)
          if (isCausedByInvalidNodeRef(e)) {
            skippedCount++;
            if (logger.isDebugEnabled()) {
              logger.debug(
                "Skipping node already deleted (detected at commit): " + nodeRef
              );
            }
          } else {
            errorCount++;
            logger.error("Failed to delete expired document: " + nodeRef, e);
          }
          if (trx != null) {
            try {
              trx.rollback();
            } catch (Exception rollbackEx) {
              // Transaction already rolled back, ignore
            }
          }
        }
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }

    if (logger.isInfoEnabled()) {
      if (deletedCount > 0 || errorCount > 0) {
        logger.info(
          "ExpiredDocumentsJob completed: " +
          deletedCount +
          " document(s) deleted, " +
          skippedCount +
          " skipped (already deleted), " +
          errorCount +
          " error(s)."
        );
      } else if (skippedCount > 0) {
        logger.info(
          "ExpiredDocumentsJob completed: " +
          skippedCount +
          " stale index entry(ies) skipped, nothing to delete."
        );
      } else {
        logger.info(
          "ExpiredDocumentsJob completed: no expired documents found."
        );
      }
    }
  }

  private void initialize(JobExecutionContext context) {
    lockService = (LockService) context
      .getMergedJobDataMap()
      .get("lockService");
    nodeService = (NodeService) context
      .getMergedJobDataMap()
      .get("nodeService");
    searchService = (SearchService) context
      .getMergedJobDataMap()
      .get("searchService");
    transactionService = (TransactionService) context
      .getMergedJobDataMap()
      .get("transactionService");
  }

  /**
   * Checks if an exception is caused by an InvalidNodeRefException (node already deleted). This
   * happens when the Lucene index is stale and returns nodes that have already been removed.
   */
  private boolean isCausedByInvalidNodeRef(Throwable e) {
    Throwable cause = e;
    while (cause != null) {
      if (cause instanceof InvalidNodeRefException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }
}
