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

/**
 * Service contract for managing named, application-level locks used to serialize access to shared
 * resources across the CIRCABC backend.
 *
 * <p>Locks are identified by a case-insensitive name (the {@code item} argument). Two families of
 * operations are provided:
 *
 * <ul>
 *   <li>Blocking-style methods ({@link #lock(String)}, {@link #lockForever(String)}) that raise an
 *       {@link IllegalStateException} when the item is already locked.
 *   <li>Non-throwing "try" methods ({@link #tryLock(String)}, {@link #tryLockForever(String)}) that
 *       return {@code false} instead of throwing when the lock cannot be acquired.
 * </ul>
 *
 * <p>Regular locks are subject to automatic clean-up by the scheduled {@code UnlockJob}, whereas the
 * {@code *Forever} variants create locks that the job leaves untouched. Stale locks can also be
 * released in bulk via {@link #unlockAll(int)}.
 */
public interface LockService {
  /**
   * Acquires a lock on the given item.
   *
   * <p>Unlike {@link #tryLock(String)}, this method throws an {@link IllegalStateException} if the
   * item is already locked rather than returning a status flag.
   *
   * @param item case-insensitive name of the lock item to acquire
   * @throws IllegalStateException if the item is already locked
   */
  void lock(String item);

  /**
   * Acquires a lock on the given item that is not removed by the scheduled {@code UnlockJob}.
   *
   * <p>Unlike {@link #tryLockForever(String)}, this method throws an {@link IllegalStateException} if
   * the item is already locked rather than returning a status flag.
   *
   * @param item case-insensitive name of the lock item to acquire
   * @throws IllegalStateException if the item is already locked
   */
  void lockForever(String item);

  /**
   * Releases the lock on the given item if it is currently locked; otherwise does nothing.
   *
   * @param item case-insensitive name of the lock item to release
   */
  void unlock(String item);

  /**
   * Checks whether the given item is currently locked.
   *
   * @param item case-insensitive name of the lock item to check
   * @return {@code true} if the item is already locked, {@code false} otherwise
   */
  boolean isLocked(String item);

  /**
   * Attempts to acquire a lock on the given item without throwing if it is unavailable.
   *
   * @param item case-insensitive name of the lock item to acquire
   * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
   */
  boolean tryLock(String item);

  /**
   * Attempts to acquire a lock on the given item that is not removed by the scheduled
   * {@code UnlockJob}, without throwing if it is unavailable.
   *
   * @param item case-insensitive name of the lock item to acquire
   * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
   */
  boolean tryLockForever(String item);

  /**
   * Releases all locks that are older than the given number of hours.
   *
   * @param hours age threshold, in hours; locks older than this value are released
   */
  void unlockAll(int hours);
}
