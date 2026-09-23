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
package eu.europa.ec.digit.circabc.rest.service.user;

import java.util.Map;
import java.util.Set;

/**
 * Service contract for resolving the set of ECAS (EU Login) authentication domains backed by LDAP.
 *
 * <p>Implementations expose the list of available ECAS domain keys together with their
 * human-readable descriptions, optionally localized for a requested language. This is used by the
 * user-management layer to present the domains a user can authenticate against.
 *
 * @author Slobodan Filipovic
 */
public interface LdapEcasDomainService {
  /**
   * Initializes the Spring bean, loading and caching the ECAS domain data required by the other
   * service methods. Intended to be invoked as the bean's init method after construction.
   */
  void init();

  /**
   * Returns all known ECAS domain keys.
   *
   * @return the set of ECAS domain keys
   */
  Set<String> getAllEcasDomains();

  /**
   * Returns the ECAS domains with their default (English) descriptions.
   *
   * @return a map of ECAS domain key to its default (English) description
   */
  Map<String, String> getDefaultEcasDomains();

  /**
   * Returns the ECAS domains with their descriptions localized for the given language.
   *
   * @param language the ISO language code used to localize the descriptions (for example {@code
   *     en}, {@code fr})
   * @return a map of ECAS domain key to its description in the requested language
   */
  Map<String, String> getEcasDomains(String language);
}
