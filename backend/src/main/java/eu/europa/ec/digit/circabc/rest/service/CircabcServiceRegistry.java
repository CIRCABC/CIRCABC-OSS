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
import eu.europa.ec.digit.circabc.rest.service.namespace.CircabcNameSpaceService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * This interface represents the registry of public Repository Services. The
 * registry provides
 * meta-data about each service and provides access to the service interface.
 *
 * @author Clinckart Stephane
 */
public interface CircabcServiceRegistry {
  /** Spring bean name under which the service registry is published. */
  String CIRCABC_SERVICE_REGISTRY = "circabcServiceRegistry";

  /** Qualified name identifying the core Alfresco {@code ServiceRegistry} in the registry. */
  QName ALFRESCO_REGISTRY_SERVICE = QName.createQName(
    NamespaceService.ALFRESCO_URI,
    "ServiceRegistry"
  );

  /** Qualified name of the non-secured CIRCABC log service. */
  QName NON_SECURED_LOG_SERVICE = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "logService"
  );

  /** Qualified name of the non-secured CIRCABC event service. */
  QName NON_SECURED_EVENT_SERVICE = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "eventService"
  );

  /** Qualified name of the CIRCABC lock service. */
  QName LOCK_SERVICE = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "CircabcLockService"
  );

  /** Qualified name of the CIRCABC API service. */
  QName CIRCABC_API = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "circabcApi"
  );

  /** Qualified name of the CIRCABC user service. */
  QName CIRCABC_USER_SERVICE = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "UserService"
  );

  /** Qualified name of the CIRCABC configuration holder. */
  QName CIRCABC_CONFIG = QName.createQName(
    CircabcNameSpaceService.CEC_DIGIT_URI,
    "circabcConfig"
  );

  /** Qualified name of the Alfresco policy behaviour filter used to toggle policy behaviours. */
  QName POLICY_BEHAVIOUR_FILTER = QName.createQName(
    NamespaceService.ALFRESCO_URI,
    "policyBehaviourFilter"
  );

  /**
   * Is the specified service provided by Circabc?
   *
   * @param service name of service to test provision of
   * @return true => provided, false => not provided
   */
  @NotAuditable
  boolean isServiceProvided(QName service);

  /**
   * @return the non secure service
   */
  @NotAuditable
  LogService getLogService();

  /**
   * @return the CIRCABC lock service used to manage content locks
   */
  @NotAuditable
  LockService getLockService();

  /**
   * @return the CIRCABC API service exposing the platform's business operations
   */
  @NotAuditable
  CircabcApi getCircabcApiService();

  /**
   * @return the CIRCABC user service handling user profiles and memberships
   */
  @NotAuditable
  UserService getUserService();

  /**
   * @return the non-secured event service used to publish and manage events
   */
  @NotAuditable
  EventService getNonSecureEventService();

  /**
   * @return the CIRCABC configuration holder
   */
  @NotAuditable
  CircabcConfig getCircabcConfig();

  /**
   * @return the Alfresco behaviour filter used to enable or disable policy behaviours
   */
  @NotAuditable
  BehaviourFilter getBehaviourFilter();
}
