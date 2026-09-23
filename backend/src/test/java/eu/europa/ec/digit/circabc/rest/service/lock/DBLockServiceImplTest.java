package eu.europa.ec.digit.circabc.rest.service.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class DBLockServiceImplTest {

  private DBLockServiceImpl service;
  private LockDaoService lockDaoService;

  @Before
  public void setUp() throws Exception {
    service = new DBLockServiceImpl();
    lockDaoService = mock(LockDaoService.class);
    Field field = DBLockServiceImpl.class.getDeclaredField("lockDaoService");
    field.setAccessible(true);
    field.set(service, lockDaoService);
  }

  @Test
  public void testIsLocked_whenCountIsOne_thenReturnsTrue() {
    when(lockDaoService.getLockCount("TEST")).thenReturn(1);
    assertTrue(service.isLocked("test"));
  }

  @Test
  public void testIsLocked_whenCountIsZero_thenReturnsFalse() {
    when(lockDaoService.getLockCount("TEST")).thenReturn(0);
    assertFalse(service.isLocked("test"));
  }

  @Test
  public void testIsLocked_whenExceptionThrown_thenReturnsFalse() {
    when(lockDaoService.getLockCount("TEST")).thenThrow(
      new RuntimeException("db error")
    );
    assertFalse(service.isLocked("test"));
  }

  @Test
  public void testTryLock_whenSuccess_thenReturnsTrue() {
    assertTrue(service.tryLock("test"));
    verify(lockDaoService).insertLock("TEST");
  }

  @Test
  public void testTryLock_whenExceptionThrown_thenReturnsFalse() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLock("TEST");
    assertFalse(service.tryLock("test"));
  }

  @Test
  public void testTryLockForever_whenSuccess_thenReturnsTrue() {
    assertTrue(service.tryLockForever("test"));
    verify(lockDaoService).insertLockNulls("TEST");
  }

  @Test
  public void testTryLockForever_whenExceptionThrown_thenReturnsFalse() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLockNulls("TEST");
    assertFalse(service.tryLockForever("test"));
  }

  @Test
  public void testLock_whenSuccess_thenNoException() {
    service.lock("test");
    verify(lockDaoService).insertLock("TEST");
  }

  @Test(expected = IllegalStateException.class)
  public void testLock_whenExceptionThrown_thenThrowsIllegalState() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLock("TEST");
    service.lock("test");
  }

  @Test
  public void testLockForever_whenSuccess_thenNoException() {
    service.lockForever("test");
    verify(lockDaoService).insertLockNulls("TEST");
  }

  @Test(expected = IllegalStateException.class)
  public void testLockForever_whenExceptionThrown_thenThrowsIllegalState() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLockNulls("TEST");
    service.lockForever("test");
  }

  @Test
  public void testUnlock_whenSuccess_thenDeletesCalled() {
    service.unlock("test");
    verify(lockDaoService).deleteLock("TEST");
  }

  @Test
  public void testUnlock_whenExceptionThrown_thenNoExceptionPropagated() {
    doThrow(new RuntimeException("db error"))
      .when(lockDaoService)
      .deleteLock("TEST");
    service.unlock("test");
  }

  @Test
  public void testUnlockAll_whenSuccess_thenDeleteLocksCalled() {
    service.unlockAll(24);
    verify(lockDaoService).deleteLocks(24);
  }

  @Test
  public void testUnlockAll_whenExceptionThrown_thenNoExceptionPropagated() {
    doThrow(new RuntimeException("db error"))
      .when(lockDaoService)
      .deleteLocks(24);
    service.unlockAll(24);
  }
}
