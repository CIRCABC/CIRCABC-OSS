package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.repo.app.model.GroupLock;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.profile.permissions.CategoryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.util.PathUtils;
import io.swagger.model.GroupLockInfo;
import io.swagger.model.GroupLockRequest;
import io.swagger.util.Converter;
import java.util.Date;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of GroupLockApi for managing Interest Group lock state.
 */
public class GroupLockApiImpl implements GroupLockApi {

  private static final Log logger = LogFactory.getLog(GroupLockApiImpl.class);

  private static final String LOG_SERVICE = "Administration";
  private static final String LOG_ACTIVITY_LOCK = "Lock interest group";
  private static final String LOG_ACTIVITY_UNLOCK = "Unlock interest group";

  private NodeService nodeService;
  private PermissionService permissionService;
  private CircabcDaoServiceImpl circabcDaoService;
  private TransactionService transactionService;
  private LogService logService;

  @Override
  public void lockInterestGroup(
    final String igId,
    final GroupLockRequest request
  ) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);
    validateNodeExists(igNodeRef, igId);

    final String username = AuthenticationUtil.getFullyAuthenticatedUser();

    if (!isLeaderOrCategoryAdmin(igNodeRef, username)) {
      logger.warn(
        "Unauthorized lock attempt on IG " + igId + " by user " + username
      );
      throw new IllegalAccessError("Access denied: insufficient permissions");
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock existingLock = circabcDaoService.getGroupLock(dbId);

    if (existingLock != null) {
      throw new AlreadyLockedException("Interest group is already locked");
    }

    final boolean readOnly = request.getReadOnly() != null
      ? request.getReadOnly()
      : false;
    final LogRecord logRecord = prepareLogRecord(
      igNodeRef,
      dbId,
      username,
      LOG_ACTIVITY_LOCK
    );
    logRecord.setInfo(
      "readOnly=" +
      readOnly +
      (request.getMessage() != null ? ", message=" + request.getMessage() : "")
    );

    try {
      final GroupLock lock = new GroupLock();
      lock.setIgId(dbId);
      lock.setLockMessage(request.getMessage());
      lock.setReadOnly(readOnly);
      lock.setLockedBy(username);
      lock.setLockedDate(new Date());

      circabcDaoService.insertGroupLock(lock);
      logger.info(
        "Interest Group " +
        igId +
        " locked by user " +
        username +
        " (readOnly=" +
        lock.getReadOnly() +
        ")"
      );
      logRecord.setOK(true);
    } catch (Exception e) {
      logRecord.setOK(false);
      logger.error(
        "Failed to persist lock state for IG " + igId + ": " + e.getMessage(),
        e
      );
      throw new LockPersistenceException(
        "Internal error while processing lock operation",
        e
      );
    } finally {
      safeAuditLog(logRecord);
    }

    // Sync: apply the Alfresco aspect so that the permission layer also
    // blocks non-admin users at the repository level. Done after successful
    // DB insert; aspect sync failures are non-fatal since the DB record is
    // authoritative for lock state.
    try {
      addLockedForAccessAspect(igNodeRef);
    } catch (Exception e) {
      logger.warn(
        "Lock persisted for IG " +
        igId +
        " but aspect sync failed: " +
        e.getMessage()
      );
    }
  }

  @Override
  public void unlockInterestGroup(final String igId) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);
    validateNodeExists(igNodeRef, igId);

    final String username = AuthenticationUtil.getFullyAuthenticatedUser();

    if (!isLeaderOrCategoryAdmin(igNodeRef, username)) {
      logger.warn(
        "Unauthorized unlock attempt on IG " + igId + " by user " + username
      );
      throw new IllegalAccessError("Access denied: insufficient permissions");
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock existingLock = circabcDaoService.getGroupLock(dbId);

    if (existingLock == null) {
      throw new NotLockedException("Interest group is not locked");
    }

    final LogRecord logRecord = prepareLogRecord(
      igNodeRef,
      dbId,
      username,
      LOG_ACTIVITY_UNLOCK
    );

    try {
      circabcDaoService.deleteGroupLock(dbId);
      logger.info("Interest Group " + igId + " unlocked by user " + username);
      logRecord.setOK(true);
    } catch (Exception e) {
      logRecord.setOK(false);
      logger.error(
        "Failed to persist lock state for IG " + igId + ": " + e.getMessage(),
        e
      );
      throw new LockPersistenceException(
        "Internal error while processing lock operation",
        e
      );
    } finally {
      safeAuditLog(logRecord);
    }

    // Sync: remove the Alfresco aspect now that the manual lock is lifted.
    // Aspect sync failures are non-fatal since the DB record is authoritative.
    try {
      removeLockedForAccessAspect(igNodeRef);
    } catch (Exception e) {
      logger.warn(
        "Lock removed for IG " +
        igId +
        " but aspect sync failed: " +
        e.getMessage()
      );
    }
  }

  @Override
  public GroupLockInfo getGroupLockInfo(final String igId) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);

    if (!nodeService.exists(igNodeRef)) {
      return null;
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock lock;
    try {
      lock = circabcDaoService.getGroupLock(dbId);
    } catch (Exception e) {
      logger.warn(
        "Could not retrieve lock info for IG " + igId + ": " + e.getMessage()
      );
      return null;
    }

    if (lock == null) {
      final GroupLockInfo info = new GroupLockInfo();
      info.setLocked(false);
      return info;
    }

    final GroupLockInfo info = new GroupLockInfo();
    info.setLocked(true);
    info.setMessage(lock.getLockMessage());
    info.setReadOnly(lock.getReadOnly());
    info.setLockedBy(lock.getLockedBy());
    info.setLockedDate(lock.getLockedDate());
    return info;
  }

  @Override
  public boolean canAccessLockedGroup(final String igId) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);

    if (!nodeService.exists(igNodeRef)) {
      return false;
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock lock;
    try {
      lock = circabcDaoService.getGroupLock(dbId);
    } catch (Exception e) {
      logger.warn(
        "Could not check lock state for IG " +
        igId +
        " — allowing access: " +
        e.getMessage()
      );
      return true;
    }

    if (lock == null) {
      return true;
    }

    final String username = AuthenticationUtil.getFullyAuthenticatedUser();
    return isLeaderOrCategoryAdmin(igNodeRef, username);
  }

  @Override
  public boolean isReadOnly(final String igId) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);

    if (!nodeService.exists(igNodeRef)) {
      return false;
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock lock;
    try {
      lock = circabcDaoService.getGroupLock(dbId);
    } catch (Exception e) {
      logger.warn(
        "Could not check read-only state for IG " +
        igId +
        " — assuming not read-only: " +
        e.getMessage()
      );
      return false;
    }

    if (lock == null) {
      return false;
    }

    return Boolean.TRUE.equals(lock.getReadOnly());
  }

  @Override
  public boolean canWrite(final String igId) {
    final NodeRef igNodeRef = Converter.createNodeRefFromId(igId);
    if (!nodeService.exists(igNodeRef)) {
      return false;
    }

    final long dbId = getDbId(igNodeRef);
    final GroupLock lock;
    try {
      lock = circabcDaoService.getGroupLock(dbId);
    } catch (Exception e) {
      logger.warn(
        "Could not check write access for IG " +
        igId +
        " — assuming writable: " +
        e.getMessage()
      );
      return true;
    }

    // Not locked → regular Alfresco permissions apply.
    if (lock == null) {
      return true;
    }

    // Locked → non-admin/leader users are always blocked, even for reads at the
    // routing level. Here we specifically block writes.
    final String username = AuthenticationUtil.getFullyAuthenticatedUser();
    if (!isLeaderOrCategoryAdmin(igNodeRef, username)) {
      return false;
    }

    // Locked and user is admin/leader → allow writes unless the lock is
    // explicitly marked read-only.
    return !Boolean.TRUE.equals(lock.getReadOnly());
  }

  @Override
  @Deprecated
  public boolean canWriteOrAdmin(final String igId) {
    return canWrite(igId);
  }

  private void validateNodeExists(final NodeRef nodeRef, final String igId) {
    if (!nodeService.exists(nodeRef)) {
      throw new NodeNotFoundException("Interest group not found: " + igId);
    }
  }

  private long getDbId(final NodeRef nodeRef) {
    return (long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
  }

  private boolean isLeaderOrCategoryAdmin(
    final NodeRef igNodeRef,
    final String username
  ) {
    // Check if user is IG leader (has DirAdmin permission on the IG node)
    if (
      permissionService
        .hasPermission(igNodeRef, DirectoryPermissions.DIRADMIN.toString())
        .equals(AccessStatus.ALLOWED)
    ) {
      return true;
    }

    // Also grant access to users who can manage members (DIRMANAGEMEMBERS).
    // Member management is an administrative operation and must remain available
    // even when the IG is locked, so that leaders can still invite/remove members.
    if (
      permissionService
        .hasPermission(
          igNodeRef,
          DirectoryPermissions.DIRMANAGEMEMBERS.toString()
        )
        .equals(AccessStatus.ALLOWED)
    ) {
      return true;
    }

    // Check if user is category admin by navigating to the parent category node
    final NodeRef categoryNodeRef = nodeService
      .getPrimaryParent(igNodeRef)
      .getParentRef();
    if (
      categoryNodeRef != null &&
      nodeService.hasAspect(categoryNodeRef, CircabcModel.ASPECT_CATEGORY)
    ) {
      return permissionService
        .hasPermission(
          categoryNodeRef,
          CategoryPermissions.CIRCACATEGORYADMIN.toString()
        )
        .equals(AccessStatus.ALLOWED);
    }

    return false;
  }

  // --- Audit logging helpers ---

  /**
   * Builds a {@link LogRecord} for an IG lock/unlock activity, following the
   * same convention used by other IG lifecycle operations (see {@code GroupsApiImpl}).
   *
   * @param igNodeRef the IG node
   * @param dbId      the IG DBID (used both as igID and documentID)
   * @param username  the authenticated user performing the action
   * @param activity  human-readable activity label
   * @return an initialised {@code LogRecord}; caller is responsible for
   *         setting {@code isOK} and calling {@link LogService#log(LogRecord)}
   */
  private LogRecord prepareLogRecord(
    final NodeRef igNodeRef,
    final long dbId,
    final String username,
    final String activity
  ) {
    final LogRecord logRecord = new LogRecord();
    logRecord.setDate(new Date());
    logRecord.setIgID(dbId);
    logRecord.setDocumentID(dbId);
    logRecord.setUser(username);
    logRecord.setActivity(activity);
    logRecord.setService(LOG_SERVICE);
    final String name = String.valueOf(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)
    );
    logRecord.setIgName(name);
    logRecord.setPath(
      PathUtils.getCircabcPath(nodeService.getPath(igNodeRef), true)
    );
    return logRecord;
  }

  /**
   * Persists an audit {@link LogRecord} without letting audit failures bubble up
   * or shadow the outcome of the actual lock/unlock operation.
   */
  private void safeAuditLog(final LogRecord logRecord) {
    if (logService == null) {
      logger.warn(
        "logService not wired on GroupLockApiImpl; skipping audit log for activity=" +
        logRecord.getActivity()
      );
      return;
    }
    try {
      logService.log(logRecord);
    } catch (Exception e) {
      logger.warn(
        "Failed to persist audit log for activity=" +
        logRecord.getActivity() +
        " igID=" +
        logRecord.getIgID() +
        ": " +
        e.getMessage()
      );
    }
  }

  // --- Aspect synchronisation helpers ---

  /**
   * Applies {@code ci:lockedForAccess} to the IG node inside a new transaction so that the
   * Alfresco permission layer starts denying access to non-admin users immediately.
   * Uses system credentials because the calling user may not have write permissions on the node.
   */
  private void addLockedForAccessAspect(final NodeRef igNodeRef) {
    AuthenticationUtil.runAsSystem(
      new AuthenticationUtil.RunAsWork<Void>() {
        @Override
        public Void doWork() {
          transactionService
            .getRetryingTransactionHelper()
            .doInTransaction(
              new RetryingTransactionCallback<Void>() {
                @Override
                public Void execute() {
                  if (
                    !nodeService.hasAspect(
                      igNodeRef,
                      CircabcModel.ASPECT_LOCKED_FOR_ACCESS
                    )
                  ) {
                    nodeService.addAspect(
                      igNodeRef,
                      CircabcModel.ASPECT_LOCKED_FOR_ACCESS,
                      null
                    );
                    if (logger.isDebugEnabled()) {
                      logger.debug(
                        "Added ASPECT_LOCKED_FOR_ACCESS to node " + igNodeRef
                      );
                    }
                  }
                  return null;
                }
              },
              false,
              true
            );
          return null;
        }
      }
    );
  }

  /**
   * Removes {@code ci:lockedForAccess} from the IG node after a manual unlock, provided no export
   * is currently running for the same node. The export lifecycle manages the aspect independently
   * via its own {@code finally} block, which is guarded by a DB check — so removing the aspect
   * here is safe: if an export is in progress it will re-add the aspect before any content is read.
   */
  private void removeLockedForAccessAspect(final NodeRef igNodeRef) {
    AuthenticationUtil.runAsSystem(
      new AuthenticationUtil.RunAsWork<Void>() {
        @Override
        public Void doWork() {
          transactionService
            .getRetryingTransactionHelper()
            .doInTransaction(
              new RetryingTransactionCallback<Void>() {
                @Override
                public Void execute() {
                  if (
                    nodeService.hasAspect(
                      igNodeRef,
                      CircabcModel.ASPECT_LOCKED_FOR_ACCESS
                    )
                  ) {
                    nodeService.removeAspect(
                      igNodeRef,
                      CircabcModel.ASPECT_LOCKED_FOR_ACCESS
                    );
                    if (logger.isDebugEnabled()) {
                      logger.debug(
                        "Removed ASPECT_LOCKED_FOR_ACCESS from node " +
                        igNodeRef
                      );
                    }
                  }
                  return null;
                }
              },
              false,
              true
            );
          return null;
        }
      }
    );
  }

  // --- Getters and Setters for Spring XML injection ---

  public NodeService getNodeService() {
    return nodeService;
  }

  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public PermissionService getPermissionService() {
    return permissionService;
  }

  public void setPermissionService(final PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public CircabcDaoServiceImpl getCircabcDaoService() {
    return circabcDaoService;
  }

  public void setCircabcDaoService(
    final CircabcDaoServiceImpl circabcDaoService
  ) {
    this.circabcDaoService = circabcDaoService;
  }

  public TransactionService getTransactionService() {
    return transactionService;
  }

  public void setTransactionService(
    final TransactionService transactionService
  ) {
    this.transactionService = transactionService;
  }

  public LogService getLogService() {
    return logService;
  }

  public void setLogService(final LogService logService) {
    this.logService = logService;
  }

  // --- Custom exception classes for lock operations ---

  /**
   * Thrown when an IG is already locked and a lock attempt is made.
   */
  public static class AlreadyLockedException extends RuntimeException {

    public AlreadyLockedException(final String message) {
      super(message);
    }
  }

  /**
   * Thrown when an IG is not locked and an unlock attempt is made.
   */
  public static class NotLockedException extends RuntimeException {

    public NotLockedException(final String message) {
      super(message);
    }
  }

  /**
   * Thrown when the IG node is not found.
   */
  public static class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(final String message) {
      super(message);
    }
  }

  /**
   * Thrown when a persistence error occurs during lock/unlock operation.
   */
  public static class LockPersistenceException extends RuntimeException {

    public LockPersistenceException(
      final String message,
      final Throwable cause
    ) {
      super(message, cause);
    }
  }

  /**
   * Thrown when a write or administration operation is attempted on an IG that is in read-only mode.
   * Maps to HTTP 403 in the webscript layer.
   */
  public static class ReadOnlyException extends RuntimeException {

    public ReadOnlyException(final String message) {
      super(message);
    }
  }

  /**
   * Thrown when a delete (or deletion-request) operation is attempted on an IG that is currently locked.
   * The IG must be unlocked before it can be removed or a removal can be requested.
   * Maps to HTTP 423 (Locked) in the webscript layer.
   */
  public static class GroupLockedForDeletionException extends RuntimeException {

    public GroupLockedForDeletionException(final String message) {
      super(message);
    }
  }
}
