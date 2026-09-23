package eu.europa.ec.digit.circabc.rest.service.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class LockServiceTest {

  private LockService lockService;
  private LockDaoService lockDaoService;

  @Before
  public void setUp() throws Exception {
    DBLockServiceImpl impl = new DBLockServiceImpl();
    lockDaoService = mock(LockDaoService.class);
    Field field = DBLockServiceImpl.class.getDeclaredField("lockDaoService");
    field.setAccessible(true);
    field.set(impl, lockDaoService);
    lockService = impl;
  }

  @Test
  public void testIsLocked_whenLocked_thenReturnsTrue() {
    when(lockDaoService.getLockCount("ITEM")).thenReturn(1);
    assertTrue(lockService.isLocked("item"));
  }

  @Test
  public void testIsLocked_whenNotLocked_thenReturnsFalse() {
    when(lockDaoService.getLockCount("ITEM")).thenReturn(0);
    assertFalse(lockService.isLocked("item"));
  }

  @Test
  public void testIsLocked_whenDaoThrows_thenReturnsFalse() {
    when(lockDaoService.getLockCount("ITEM")).thenThrow(
      new RuntimeException("error")
    );
    assertFalse(lockService.isLocked("item"));
  }

  @Test
  public void testIsLocked_caseInsensitive() {
    when(lockDaoService.getLockCount("MYLOCK")).thenReturn(1);
    assertTrue(lockService.isLocked("MyLock"));
    assertTrue(lockService.isLocked("mylock"));
    assertTrue(lockService.isLocked("MYLOCK"));
  }

  @Test
  public void testTryLock_whenSuccess_thenReturnsTrue() {
    assertTrue(lockService.tryLock("item"));
    verify(lockDaoService).insertLock("ITEM");
  }

  @Test
  public void testTryLock_whenAlreadyLocked_thenReturnsFalse() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLock("ITEM");
    assertFalse(lockService.tryLock("item"));
  }

  @Test
  public void testTryLockForever_whenSuccess_thenReturnsTrue() {
    assertTrue(lockService.tryLockForever("item"));
    verify(lockDaoService).insertLockNulls("ITEM");
  }

  @Test
  public void testTryLockForever_whenAlreadyLocked_thenReturnsFalse() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLockNulls("ITEM");
    assertFalse(lockService.tryLockForever("item"));
  }

  @Test
  public void testLock_whenSuccess_thenCompletes() {
    lockService.lock("item");
    verify(lockDaoService).insertLock("ITEM");
  }

  @Test(expected = IllegalStateException.class)
  public void testLock_whenAlreadyLocked_thenThrowsIllegalState() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLock("ITEM");
    lockService.lock("item");
  }

  @Test
  public void testLockForever_whenSuccess_thenCompletes() {
    lockService.lockForever("item");
    verify(lockDaoService).insertLockNulls("ITEM");
  }

  @Test(expected = IllegalStateException.class)
  public void testLockForever_whenAlreadyLocked_thenThrowsIllegalState() {
    doThrow(new RuntimeException("duplicate"))
      .when(lockDaoService)
      .insertLockNulls("ITEM");
    lockService.lockForever("item");
  }

  @Test
  public void testUnlock_whenLocked_thenDeletesCalled() {
    lockService.unlock("item");
    verify(lockDaoService).deleteLock("ITEM");
  }

  @Test
  public void testUnlock_whenDaoThrows_thenNoExceptionPropagated() {
    doThrow(new RuntimeException("error"))
      .when(lockDaoService)
      .deleteLock("ITEM");
    lockService.unlock("item");
  }

  @Test
  public void testUnlockAll_whenSuccess_thenDeleteLocksCalled() {
    lockService.unlockAll(12);
    verify(lockDaoService).deleteLocks(12);
  }

  @Test
  public void testUnlockAll_whenDaoThrows_thenNoExceptionPropagated() {
    doThrow(new RuntimeException("error")).when(lockDaoService).deleteLocks(12);
    lockService.unlockAll(12);
  }
}
