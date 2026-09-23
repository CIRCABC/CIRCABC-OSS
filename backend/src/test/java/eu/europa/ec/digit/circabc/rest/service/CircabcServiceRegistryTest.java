package eu.europa.ec.digit.circabc.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class CircabcServiceRegistryTest {

  private BeanFactory beanFactory;
  private CircabcServiceDescriptor registry;

  @Before
  public void setUp() {
    beanFactory = mock(BeanFactory.class);
    registry = new CircabcServiceDescriptor(beanFactory);
  }

  @Test
  public void testIsServiceProvided_whenBeanExists_thenReturnsTrue() {
    when(beanFactory.getBean("logService")).thenReturn(mock(LogService.class));
    assertTrue(
      registry.isServiceProvided(CircabcServiceRegistry.NON_SECURED_LOG_SERVICE)
    );
  }

  @Test
  public void testIsServiceProvided_whenBeanNotFound_thenReturnsFalse() {
    when(beanFactory.getBean("nonExistent")).thenThrow(
      new NoSuchBeanDefinitionException("nonExistent")
    );
    QName unknown = QName.createQName("http://test", "nonExistent");
    assertFalse(registry.isServiceProvided(unknown));
  }

  @Test
  public void testGetLogService_returnsLogServiceBean() {
    LogService logService = mock(LogService.class);
    when(beanFactory.getBean("logService")).thenReturn(logService);
    assertSame(logService, registry.getLogService());
  }

  @Test
  public void testGetLockService_returnsLockServiceBean() {
    LockService lockService = mock(LockService.class);
    when(beanFactory.getBean("CircabcLockService")).thenReturn(lockService);
    assertSame(lockService, registry.getLockService());
  }

  @Test
  public void testGetCircabcApiService_returnsCircabcApiBean() {
    CircabcApi circabcApi = mock(CircabcApi.class);
    when(beanFactory.getBean("circabcApi")).thenReturn(circabcApi);
    assertSame(circabcApi, registry.getCircabcApiService());
  }

  @Test
  public void testGetUserService_returnsUserServiceBean() {
    UserService userService = mock(UserService.class);
    when(beanFactory.getBean("UserService")).thenReturn(userService);
    assertSame(userService, registry.getUserService());
  }

  @Test
  public void testGetNonSecureEventService_returnsEventServiceBean() {
    EventService eventService = mock(EventService.class);
    when(beanFactory.getBean("eventService")).thenReturn(eventService);
    assertSame(eventService, registry.getNonSecureEventService());
  }

  @Test
  public void testGetCircabcConfig_returnsConfigBean() {
    CircabcConfig config = mock(CircabcConfig.class);
    when(beanFactory.getBean("circabcConfig")).thenReturn(config);
    assertSame(config, registry.getCircabcConfig());
  }

  @Test
  public void testGetBehaviourFilter_returnsBehaviourFilterBean() {
    BehaviourFilter filter = mock(BehaviourFilter.class);
    when(beanFactory.getBean("policyBehaviourFilter")).thenReturn(filter);
    assertSame(filter, registry.getBehaviourFilter());
  }
}
