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
package eu.europa.ec.digit.circabc.rest.service.lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Database-backed implementation of {@link LockService}.
 *
 * <p>Locks are represented as rows in a dedicated database table and are managed through the
 * delegated {@link LockDaoService}. A lock is uniquely identified by its item name, which is always
 * normalized to upper case so that lock names are treated case-insensitively. Because the unique
 * constraint on the lock table is enforced by the database, attempting to insert a lock that
 * already exists fails, which this implementation uses to detect contention.
 *
 * <p>Two flavours of lock are supported: regular locks (created via {@link #lock(String)} /
 * {@link #tryLock(String)}) that carry a timestamp and can be reclaimed by the periodic unlock job
 * through {@link #unlockAll(int)}, and permanent locks (created via {@link #lockForever(String)} /
 * {@link #tryLockForever(String)}) that are stored with null values so they are never reclaimed by
 * the unlock job.
 */
public class DBLockServiceImpl implements LockService {

  /** Logger used to report failures raised by the delegated {@link LockDaoService}. */
  private static final Log logger = LogFactory.getLog(DBLockServiceImpl.class);

  /** DAO that performs the actual database operations backing every lock operation. */
  @Autowired
  private LockDaoService lockDaoService;

  /**
   * Checks whether the given item is currently locked.
   *
   * <p>Any exception raised while querying the database is logged and swallowed, in which case the
   * item is reported as not locked.
   *
   * @param item case-insensitive name of the lock item
   * @return {@code true} if exactly one lock exists for the item, {@code false} otherwise (including
   *     when the lookup fails)
   */
  public boolean isLocked(String item) {
    int lockCount = 0;
    try {
      final String itemUpper = item.toUpperCase();
      lockCount = lockDaoService.getLockCount(itemUpper);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Erorr executing lockDaoService.getLockCount  for item  " + item,
          e
        );
      }
    }
    return (lockCount == 1);
  }

  /**
   * Attempts to acquire a regular (reclaimable) lock on the given item without throwing on failure.
   *
   * @param item case-insensitive name of the lock item
   * @return {@code true} if the lock was acquired, {@code false} if it could not be acquired (for
   *     example because the item is already locked)
   */
  @Override
  public boolean tryLock(String item) {
    try {
      final String itemUpper = item.toUpperCase();
      lockDaoService.insertLock(itemUpper);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Attempts to acquire a permanent lock on the given item without throwing on failure.
   *
   * <p>The lock is stored with null values so that it is not reclaimed by the periodic unlock job
   * ({@link #unlockAll(int)}).
   *
   * @param item case-insensitive name of the lock item
   * @return {@code true} if the lock was acquired, {@code false} if it could not be acquired (for
   *     example because the item is already locked)
   */
  @Override
  public boolean tryLockForever(String item) {
    try {
      final String itemUpper = item.toUpperCase();
      lockDaoService.insertLockNulls(itemUpper);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Acquires a regular (reclaimable) lock on the given item, failing loudly if it is already
   * locked.
   *
   * @param item case-insensitive name of the lock item
   * @throws IllegalStateException if the lock cannot be acquired because the item is already locked
   */
  @Override
  public void lock(String item) {
    try {
      final String itemUpper = item.toUpperCase();
      lockDaoService.insertLock(itemUpper);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Can not lock item " + item + " it is already locked.",
        e
      );
    }
  }

  /**
   * Acquires a permanent lock on the given item, failing loudly if it is already locked.
   *
   * <p>The lock is stored with null values so that it is not reclaimed by the periodic unlock job
   * ({@link #unlockAll(int)}).
   *
   * @param item case-insensitive name of the lock item
   * @throws IllegalStateException if the lock cannot be acquired because the item is already locked
   */
  @Override
  public void lockForever(String item) {
    try {
      final String itemUpper = item.toUpperCase();
      lockDaoService.insertLockNulls(itemUpper);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Can not lock item " + item + " it is already locked.",
        e
      );
    }
  }

  /**
   * Releases the lock held on the given item, if any.
   *
   * <p>If the item is not locked this is a no-op. Any exception raised while deleting the lock is
   * logged and swallowed.
   *
   * @param item case-insensitive name of the lock item
   */
  @Override
  public void unlock(String item) {
    try {
      final String itemUpper = item.toUpperCase();
      lockDaoService.deleteLock(itemUpper);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Erorr executing lockDaoService.unlock for item  " + item,
          e
        );
      }
    }
  }

  /**
   * Releases all reclaimable locks older than the given age.
   *
   * <p>Permanent locks (created via {@link #lockForever(String)} / {@link #tryLockForever(String)})
   * are not affected. Any exception raised while deleting the locks is logged and swallowed.
   *
   * @param hours minimum age, in hours, a lock must have reached to be released
   */
  @Override
  public void unlockAll(int hours) {
    try {
      lockDaoService.deleteLocks(hours);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Erorr executing lockDaoService.unlockAll for item  ", e);
      }
    }
  }
}
