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

public class CircabcServiceDescriptorTest {

  private BeanFactory beanFactory;
  private CircabcServiceDescriptor descriptor;

  @Before
  public void setUp() {
    beanFactory = mock(BeanFactory.class);
    descriptor = new CircabcServiceDescriptor(beanFactory);
  }

  @Test
  public void testIsServiceProvided_whenBeanExists_thenReturnsTrue() {
    QName service = CircabcServiceRegistry.NON_SECURED_LOG_SERVICE;
    when(beanFactory.getBean(service.getLocalName())).thenReturn(
      mock(LogService.class)
    );

    assertTrue(descriptor.isServiceProvided(service));
  }

  @Test
  public void testIsServiceProvided_whenBeanNotFound_thenReturnsFalse() {
    QName service = CircabcServiceRegistry.LOCK_SERVICE;
    when(beanFactory.getBean(service.getLocalName())).thenThrow(
      new NoSuchBeanDefinitionException("not found")
    );

    assertFalse(descriptor.isServiceProvided(service));
  }

  @Test
  public void testGetLogService_returnsLogService() {
    LogService logService = mock(LogService.class);
    when(
      beanFactory.getBean(
        CircabcServiceRegistry.NON_SECURED_LOG_SERVICE.getLocalName()
      )
    ).thenReturn(logService);

    assertSame(logService, descriptor.getLogService());
  }

  @Test
  public void testGetLockService_returnsLockService() {
    LockService lockService = mock(LockService.class);
    when(
      beanFactory.getBean(CircabcServiceRegistry.LOCK_SERVICE.getLocalName())
    ).thenReturn(lockService);

    assertSame(lockService, descriptor.getLockService());
  }

  @Test
  public void testGetCircabcApiService_returnsCircabcApi() {
    CircabcApi circabcApi = mock(CircabcApi.class);
    when(
      beanFactory.getBean(CircabcServiceRegistry.CIRCABC_API.getLocalName())
    ).thenReturn(circabcApi);

    assertSame(circabcApi, descriptor.getCircabcApiService());
  }

  @Test
  public void testGetUserService_returnsUserService() {
    UserService userService = mock(UserService.class);
    when(
      beanFactory.getBean(
        CircabcServiceRegistry.CIRCABC_USER_SERVICE.getLocalName()
      )
    ).thenReturn(userService);

    assertSame(userService, descriptor.getUserService());
  }

  @Test
  public void testGetNonSecureEventService_returnsEventService() {
    EventService eventService = mock(EventService.class);
    when(
      beanFactory.getBean(
        CircabcServiceRegistry.NON_SECURED_EVENT_SERVICE.getLocalName()
      )
    ).thenReturn(eventService);

    assertSame(eventService, descriptor.getNonSecureEventService());
  }

  @Test
  public void testGetCircabcConfig_returnsCircabcConfig() {
    CircabcConfig config = mock(CircabcConfig.class);
    when(
      beanFactory.getBean(CircabcServiceRegistry.CIRCABC_CONFIG.getLocalName())
    ).thenReturn(config);

    assertSame(config, descriptor.getCircabcConfig());
  }

  @Test
  public void testGetBehaviourFilter_returnsBehaviourFilter() {
    BehaviourFilter filter = mock(BehaviourFilter.class);
    when(
      beanFactory.getBean(
        CircabcServiceRegistry.POLICY_BEHAVIOUR_FILTER.getLocalName()
      )
    ).thenReturn(filter);

    assertSame(filter, descriptor.getBehaviourFilter());
  }
}
