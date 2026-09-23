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

import io.swagger.config.CircabcConfig;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import org.alfresco.repo.cache.SimpleCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LdapEcasDomainService}.
 *
 * <p>This service retrieves the list of EU Login (ECAS) domains from an LDAP directory and exposes
 * them, optionally localized, to the rest of the application. The LDAP connection parameters
 * (context factory, provider URL and security credentials) are read from {@link CircabcConfig}, and
 * LDAP lookups are only performed when LDAP usage is enabled in the configuration.
 *
 * <p>Because the set of domains rarely changes and LDAP queries are relatively expensive, the raw
 * directory data is loaded once and stored in an injected {@link SimpleCache} keyed under the
 * {@code "data"} entry. The cached structure maps each domain common name ({@code cn}) to a map of
 * its {@code description} attributes, including the language-specific variants
 * ({@code description;lang-<language>}).
 */
public class LdapEcasDomainServiceImpl implements LdapEcasDomainService {

  /**
   * Logger
   */
  private static final Log logger = LogFactory.getLog(
    LdapEcasDomainServiceImpl.class
  );

  /**
   * JNDI environment holding the LDAP connection settings (initial context factory, provider URL
   * and security credentials) used to open a directory context. Populated by {@link #init()} from
   * {@link CircabcConfig} and left empty when LDAP usage is disabled.
   */
  @SuppressWarnings("java:S1149")
  private Hashtable<String, String> env;

  /**
   * Cache holding the LDAP domain data under the single {@code "data"} key. The cached value maps
   * each domain common name to a map of its {@code description} attributes (including
   * language-specific variants).
   */
  private SimpleCache<
    String,
    Map<String, HashMap<String, String>>
  > ldapECASDomainsCache;

  /**
   * Application configuration providing the LDAP/ECAS connection parameters and the flag indicating
   * whether LDAP lookups are enabled.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Initializes the JNDI environment used to connect to the ECAS LDAP directory.
   *
   * <p>When LDAP usage is enabled in the configuration, the initial context factory, provider URL
   * and security credentials are read from {@link CircabcConfig} into {@link #env} and the domain
   * cache is primed. When LDAP usage is disabled, only an empty environment is created and no cache
   * initialization takes place. Typically invoked as the Spring bean init method.
   */
  public void init() {
    env = new Hashtable<>();
    if (circabcConfig.isUseLDAP()) {
      env.put(
        Context.INITIAL_CONTEXT_FACTORY,
        circabcConfig.getContextFactory()
      );
      env.put(Context.PROVIDER_URL, circabcConfig.getProviderUrlEcas());
      env.put(
        Context.SECURITY_AUTHENTICATION,
        circabcConfig.getSecurityAuthentication()
      );
      env.put(Context.SECURITY_PRINCIPAL, circabcConfig.getSecurityPrincipal());
      env.put(
        Context.SECURITY_CREDENTIALS,
        circabcConfig.getSecurityCredentials()
      );

      // Follow LDAP referrals (required to access external users in EUDS)
      env.put(Context.REFERRAL, "follow");

      initCache();
    }
  }

  private Map<String, HashMap<String, String>> getCachedLdapData() {
    initCache();
    return ldapECASDomainsCache.get("data");
  }

  /**
   *
   */
  private void initCache() {
    if (!ldapECASDomainsCache.contains("data")) {
      ldapECASDomainsCache.put("data", getLdapData());
    }
  }

  private Map<String, HashMap<String, String>> getLdapData() {
    Map<String, HashMap<String, String>> ldapData = new HashMap<>();
    DirContext ctx = null;
    NamingEnumeration<?> results = null;

    try {
      ctx = new InitialDirContext(env);
      SearchControls controls = new SearchControls();
      controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      controls.setCountLimit(1000);
      results = ctx.search("", "(cn=*)", controls);

      while (results.hasMore()) {
        SearchResult searchResult = (SearchResult) results.next();
        processSearchResult(searchResult, ldapData);
      }
    } catch (NamingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when call getLdapData method", e);
      }
    } finally {
      closeQuietly(results);
      closeQuietly(ctx);
    }
    return ldapData;
  }

  private void processSearchResult(
    SearchResult searchResult,
    Map<String, HashMap<String, String>> ldapData
  ) throws NamingException {
    Attributes attributes = searchResult.getAttributes();
    String cn = (String) attributes.get("cn").get();
    HashMap<String, String> map = new HashMap<>();

    @SuppressWarnings("rawtypes")
    NamingEnumeration ae = attributes.getAll();
    while (ae.hasMore()) {
      Attribute attr = (Attribute) ae.next();
      if (attr.getID().startsWith("description")) {
        map.put(attr.getID(), (String) attr.get());
      }
    }
    ldapData.put(cn, map);
  }

  private void closeQuietly(NamingEnumeration<?> results) {
    if (results != null) {
      try {
        results.close();
      } catch (Exception e) {
        if (logger.isWarnEnabled()) logger.warn(
          "Error when close NamingEnumeration",
          e
        );
      }
    }
  }

  private void closeQuietly(DirContext ctx) {
    if (ctx != null) {
      try {
        ctx.close();
      } catch (Exception e) {
        if (logger.isWarnEnabled()) logger.warn(
          "Error when close DirContext",
          e
        );
      }
    }
  }

  /**
   * Returns the common names ({@code cn}) of all ECAS domains available in the cached LDAP data.
   *
   * @return the set of domain common names; empty if no data could be loaded
   */
  public Set<String> getAllEcasDomains() {
    return getCachedLdapData().keySet();
  }

  /**
   * Returns all ECAS domains together with their default (non-localized) descriptions.
   *
   * @return a map from domain common name to its default {@code description} value
   */
  public Map<String, String> getDefaultEcasDomains() {
    Map<String, String> result = HashMap.newHashMap(12);

    for (Map.Entry<
      String,
      HashMap<String, String>
    > element : getCachedLdapData().entrySet()) {
      result.put(element.getKey(), element.getValue().get("description"));
    }
    return result;
  }

  /**
   * Returns all ECAS domains together with their descriptions localized for the given language.
   *
   * <p>Descriptions are looked up using the {@code description;lang-<language>} LDAP attribute. If a
   * domain has no description for the requested language, its value in the resulting map will be
   * {@code null}.
   *
   * @param language the language code used to select the localized {@code description} attribute
   * @return a map from domain common name to its localized description
   */
  public Map<String, String> getEcasDomains(String language) {
    Map<String, String> result = HashMap.newHashMap(12);

    for (Map.Entry<
      String,
      HashMap<String, String>
    > element : getCachedLdapData().entrySet()) {
      result.put(
        element.getKey(),
        element.getValue().get("description;lang-" + language)
      );
    }
    return result;
  }

  /**
   * Injects the cache used to store the loaded LDAP domain data.
   *
   * @param ldapECASDomainsCache the cache instance to use for holding the ECAS domain data
   */
  public void setLdapECASDomainsCache(
    SimpleCache<
      String,
      Map<String, HashMap<String, String>>
    > ldapECASDomainsCache
  ) {
    this.ldapECASDomainsCache = ldapECASDomainsCache;
  }
}
