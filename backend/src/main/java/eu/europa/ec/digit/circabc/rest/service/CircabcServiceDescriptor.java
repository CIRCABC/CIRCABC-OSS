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
package eu.europa.ec.digit.circabc.rest.service;

import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link CircabcServiceRegistry}.
 *
 * <p>Acts as a service locator / registry facade for the CIRCABC REST layer: it resolves the
 * various CIRCABC and Alfresco services on demand from the Spring {@link BeanFactory} using the
 * {@link QName} identifiers declared on {@link CircabcServiceRegistry}. The local name of each
 * {@code QName} is used as the Spring bean name, decoupling callers from concrete bean wiring and
 * allowing services to be looked up lazily rather than injected individually.
 *
 * @author Stephane Clinckart
 * @see CircabcServiceRegistry
 */
@Service
public class CircabcServiceDescriptor implements CircabcServiceRegistry {

  /** Spring bean factory used to lazily resolve registered services by their bean name. */
  private final BeanFactory beanFactory;

  /**
   * Creates the service descriptor.
   *
   * @param beanFactory the Spring bean factory from which CIRCABC and Alfresco services are
   *     resolved
   */
  @Autowired
  public CircabcServiceDescriptor(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Indicates whether the given service is available in the underlying Spring context.
   *
   * @param service the {@link QName} identifying the service; its local name is used as the Spring
   *     bean name
   * @return {@code true} if a matching bean exists and can be resolved, {@code false} if no such
   *     bean is defined
   */
  public boolean isServiceProvided(final QName service) {
    try {
      return getService(service) != null;
    } catch (
      org.springframework.beans.factory.NoSuchBeanDefinitionException ex
    ) {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.alfresco.repo.service.ServiceRegistry#getService(org.alfresco.repo.ref.
   * QName)
   */
  private Object getService(final QName service) {
    return beanFactory.getBean(service.getLocalName());
  }

  /**
   * Returns the non-secured logging service.
   *
   * @return the {@link LogService} registered under {@link #NON_SECURED_LOG_SERVICE}
   */
  @Override
  public LogService getLogService() {
    return (LogService) getService(NON_SECURED_LOG_SERVICE);
  }

  /**
   * Returns the CIRCABC lock service.
   *
   * @return the {@link LockService} registered under {@link #LOCK_SERVICE}
   */
  @Override
  public LockService getLockService() {
    return (LockService) getService(LOCK_SERVICE);
  }

  /**
   * Returns the CIRCABC API service.
   *
   * @return the {@link CircabcApi} registered under {@link #CIRCABC_API}
   */
  @Override
  public CircabcApi getCircabcApiService() {
    return (CircabcApi) getService(CIRCABC_API);
  }

  /**
   * Returns the CIRCABC user service.
   *
   * @return the {@link UserService} registered under {@link #CIRCABC_USER_SERVICE}
   */
  @Override
  public UserService getUserService() {
    return (UserService) getService(CIRCABC_USER_SERVICE);
  }

  /**
   * Returns the non-secured event service.
   *
   * @return the {@link EventService} registered under {@link #NON_SECURED_EVENT_SERVICE}
   */
  @Override
  public EventService getNonSecureEventService() {
    return (EventService) getService(NON_SECURED_EVENT_SERVICE);
  }

  /**
   * Returns the CIRCABC configuration bean.
   *
   * @return the {@link CircabcConfig} registered under {@link #CIRCABC_CONFIG}
   */
  @Override
  public CircabcConfig getCircabcConfig() {
    return (CircabcConfig) getService(CIRCABC_CONFIG);
  }

  /**
   * Returns the Alfresco policy behaviour filter, used to temporarily disable content-model
   * behaviours during repository operations.
   *
   * @return the {@link BehaviourFilter} registered under {@link #POLICY_BEHAVIOUR_FILTER}
   */
  @Override
  public BehaviourFilter getBehaviourFilter() {
    return (BehaviourFilter) getService(POLICY_BEHAVIOUR_FILTER);
  }
}
