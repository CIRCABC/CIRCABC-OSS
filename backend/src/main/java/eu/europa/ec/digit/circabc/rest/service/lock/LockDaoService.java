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
 * Data access contract for managing persistent locks on CIRCABC items.
 *
 * <p>Implementations of this interface encapsulate the persistence operations used to create,
 * query and remove locks that guard concurrent access to a given item. A lock is identified by an
 * item key (typically a node reference or item identifier) and may be reclaimed automatically after
 * a configurable age to avoid stale locks lingering in the store.
 *
 * @author Slobodan Filipovic
 */
public interface LockDaoService {
  /**
   * Inserts a lock for the given item.
   *
   * @param item the identifier of the item to lock
   */
  void insertLock(String item);

  /**
   * Inserts a lock for the given item leaving its optional/nullable columns unset.
   *
   * @param item the identifier of the item to lock
   */
  void insertLockNulls(String item);

  /**
   * Deletes the lock held on the given item.
   *
   * @param item the identifier of the item whose lock should be removed
   */
  void deleteLock(String item);

  /**
   * Returns the number of locks currently recorded for the given item.
   *
   * @param item the identifier of the item to check
   * @return the count of existing locks for the item
   */
  int getLockCount(String item);

  /**
   * Deletes locks that are older than the specified age, reclaiming stale locks.
   *
   * @param hours the age threshold in hours; locks older than this value are removed
   */
  void deleteLocks(int hours);
}
