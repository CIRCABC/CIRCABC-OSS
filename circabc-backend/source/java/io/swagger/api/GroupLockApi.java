package io.swagger.api;

import io.swagger.model.GroupLockInfo;
import io.swagger.model.GroupLockRequest;

/**
 * API for managing Interest Group lock state.
 * Provides operations to lock/unlock an IG and query its lock status.
 */
public interface GroupLockApi {
  /**
   * Locks an Interest Group, restricting access to leaders and category admins only.
   *
   * @param igId the ID of the Interest Group to lock
   * @param request the lock request containing message and read-only flag
   */
  void lockInterestGroup(String igId, GroupLockRequest request);

  /**
   * Unlocks a previously locked Interest Group, restoring normal access.
   *
   * @param igId the ID of the Interest Group to unlock
   */
  void unlockInterestGroup(String igId);

  /**
   * Returns the lock information for an Interest Group.
   *
   * @param igId the ID of the Interest Group
   * @return the lock info, or null if the IG is not locked
   */
  GroupLockInfo getGroupLockInfo(String igId);

  /**
   * Checks whether the current user can access a locked Interest Group.
   * Access is granted if the IG is not locked, or if the user is a leader or category admin.
   *
   * @param igId the ID of the Interest Group
   * @return true if the current user can access the IG, false otherwise
   */
  boolean canAccessLockedGroup(String igId);

  /**
   * Checks whether the Interest Group is currently in read-only mode.
   *
   * @param igId the ID of the Interest Group
   * @return true if the IG is locked in read-only mode, false otherwise
   */
  boolean isReadOnly(String igId);

  /**
   * Checks whether the currently authenticated user can perform write operations on the IG.
   *
   * <p>Semantics:</p>
   * <ul>
   *   <li>If the IG is <b>not locked</b> → returns {@code true} (regular Alfresco
   *       permissions still apply downstream).</li>
   *   <li>If the IG is <b>locked</b> and the user is <b>not</b> an IG leader nor a
   *       category admin → returns {@code false}. Regular members, registered users
   *       and guests are fully blocked while a lock is in place.</li>
   *   <li>If the IG is <b>locked</b> and the user <b>is</b> a leader or category admin →
   *       returns {@code true} when {@code readOnly=false}, or {@code false} when
   *       {@code readOnly=true} (leaders/admins are blocked only when the lock is
   *       explicitly marked read-only).</li>
   * </ul>
   *
   * @param igId the ID of the Interest Group
   * @return true if the current user is allowed to write to the IG, false otherwise
   */
  boolean canWrite(String igId);

  /**
   * @deprecated Use {@link #canWrite(String)} instead. This method is kept for
   * backwards compatibility and now delegates to {@link #canWrite(String)}.
   *
   * @param igId the ID of the Interest Group
   * @return true if the current user can write to the IG, false otherwise
   */
  @Deprecated
  boolean canWriteOrAdmin(String igId);
}
