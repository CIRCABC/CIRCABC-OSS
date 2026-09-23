package eu.europa.ec.digit.circabc.rest.service.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class LockDaoServiceTest {

  private IBatisLockDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    service = new IBatisLockDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testGetLockCount_whenItemExists_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne("CircabcLock.select_lock_count", "item1")
    ).thenReturn(3);
    assertEquals(3, service.getLockCount("item1"));
  }

  @Test
  public void testGetLockCount_whenItemNotFound_thenReturnsZero() {
    when(
      sqlSessionTemplate.selectOne("CircabcLock.select_lock_count", "unknown")
    ).thenReturn(0);
    assertEquals(0, service.getLockCount("unknown"));
  }

  @Test
  public void testInsertLock_callsSqlSession() {
    service.insertLock("item1");
    verify(sqlSessionTemplate).insert("CircabcLock.insert_lock", "item1");
  }

  @Test
  public void testInsertLockNulls_callsSqlSession() {
    service.insertLockNulls("item1");
    verify(sqlSessionTemplate).insert("CircabcLock.insert_lock_nulls", "item1");
  }

  @Test
  public void testDeleteLock_callsSqlSession() {
    service.deleteLock("item1");
    verify(sqlSessionTemplate).delete("CircabcLock.delete_lock", "item1");
  }

  @Test
  public void testDeleteLocks_callsSqlSessionWithDate() {
    service.deleteLocks(24);
    verify(sqlSessionTemplate).delete(
      eq("CircabcLock.delete_locks"),
      any(java.util.Date.class)
    );
  }

  @Test
  public void testDeleteLocks_whenZeroHours_callsSqlSession() {
    service.deleteLocks(0);
    verify(sqlSessionTemplate).delete(
      eq("CircabcLock.delete_locks"),
      any(java.util.Date.class)
    );
  }
}
