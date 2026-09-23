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
package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import org.alfresco.service.ServiceRegistry;

/**
 * Base class for {@link IndexValidator} implementations used during bulk import index validation.
 *
 * <p>It centralises the shared state required by concrete validators, namely access to the Alfresco
 * {@link ServiceRegistry}, so that subclasses can focus solely on their specific validation logic
 * (implementing {@link IndexValidator#validate}). Concrete validators (for example a validator
 * checking record names) extend this class and reuse the injected registry to look up the Alfresco
 * services they need.
 */
public abstract class AbstractIndexValidator implements IndexValidator {

  /**
   * Entry point to the Alfresco services (node, permission, dictionary, etc.) that subclasses may
   * use to perform their validation.
   */
  protected ServiceRegistry serviceRegistry;

  /**
   * Creates a validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services required for validation
   */
  protected AbstractIndexValidator(final ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
