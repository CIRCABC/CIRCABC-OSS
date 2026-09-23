package eu.europa.ec.digit.circabc.rest.service.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class IBatisLockDaoServiceImplTest {

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

    int result = service.getLockCount("item1");

    assertEquals(3, result);
    verify(sqlSessionTemplate).selectOne(
      "CircabcLock.select_lock_count",
      "item1"
    );
  }

  @Test
  public void testGetLockCount_whenItemNotFound_thenReturnsZero() {
    when(
      sqlSessionTemplate.selectOne("CircabcLock.select_lock_count", "unknown")
    ).thenReturn(0);

    int result = service.getLockCount("unknown");

    assertEquals(0, result);
  }

  @Test
  public void testInsertLock_whenCalled_thenDelegates() {
    service.insertLock("item1");

    verify(sqlSessionTemplate).insert("CircabcLock.insert_lock", "item1");
  }

  @Test
  public void testInsertLockNulls_whenCalled_thenDelegates() {
    service.insertLockNulls("item1");

    verify(sqlSessionTemplate).insert("CircabcLock.insert_lock_nulls", "item1");
  }

  @Test
  public void testDeleteLock_whenCalled_thenDelegates() {
    service.deleteLock("item1");

    verify(sqlSessionTemplate).delete("CircabcLock.delete_lock", "item1");
  }

  @Test
  public void testDeleteLocks_whenCalled_thenDeletesWithDateParameter() {
    long beforeCall = System.currentTimeMillis() - 2L * 3600 * 1000;

    service.deleteLocks(2);

    verify(sqlSessionTemplate).delete(
      eq("CircabcLock.delete_locks"),
      any(Date.class)
    );
  }
}
