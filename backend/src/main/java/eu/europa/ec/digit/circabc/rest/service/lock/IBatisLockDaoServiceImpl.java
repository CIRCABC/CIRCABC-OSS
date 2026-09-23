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

import java.util.Date;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link LockDaoService}.
 *
 * <p>Provides persistence operations for CIRCABC locks by delegating to MyBatis mapped statements
 * (namespace {@code CircabcLock}) through a Spring-managed {@link SqlSessionTemplate}. Each lock is
 * identified by an item key and can be created, counted, deleted individually, or purged in bulk by
 * age.
 *
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 IBatisSqlMapper was moved to Spring.
 */
public class IBatisLockDaoServiceImpl implements LockDaoService {

  /** Spring-managed MyBatis session template used to execute the {@code CircabcLock} statements. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Returns the number of lock records currently held for the given item.
   *
   * @param item the key identifying the locked item
   * @return the count of existing lock rows for the item
   */
  public int getLockCount(String item) {
    return (Integer) sqlSessionTemplate.selectOne(
      "CircabcLock.select_lock_count",
      item
    );
  }

  /**
   * Inserts a new lock record for the given item.
   *
   * @param item the key identifying the item to lock
   */
  public void insertLock(String item) {
    sqlSessionTemplate.insert("CircabcLock.insert_lock", item);
  }

  /**
   * Inserts a new lock record for the given item using the {@code insert_lock_nulls} statement,
   * which stores the lock with null-valued optional columns.
   *
   * @param item the key identifying the item to lock
   */
  @Override
  public void insertLockNulls(String item) {
    sqlSessionTemplate.insert("CircabcLock.insert_lock_nulls", item);
  }

  /**
   * Deletes the lock record associated with the given item.
   *
   * @param item the key identifying the item whose lock should be removed
   */
  public void deleteLock(String item) {
    sqlSessionTemplate.delete("CircabcLock.delete_lock", item);
  }

  /**
   * Deletes all lock records older than the given number of hours.
   *
   * @param hours the age threshold in hours; locks created before this cutoff (now minus {@code
   *     hours}) are removed
   */
  public void deleteLocks(int hours) {
    Date date = new Date(
      System.currentTimeMillis() - (long) hours * 3600 * 1000
    );
    sqlSessionTemplate.delete("CircabcLock.delete_locks", date);
  }

  /**
   * @param sqlSessionTemplate the sqlSessionTemplate to set
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }
}
