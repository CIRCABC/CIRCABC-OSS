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
package eu.cec.digit.circabc.repo.user;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.user.LdapEcasDomainService;
import org.alfresco.repo.cache.SimpleCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class LdapEcasDomainServiceImpl implements LdapEcasDomainService {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(LdapEcasDomainServiceImpl.class);

    private Hashtable<String, String> env;
    private SimpleCache<String, Map<String, HashMap<String, String>>> ldapECASDomainsCache;

    public void init() {
        env = new Hashtable<>();
        env.put(
                Context.INITIAL_CONTEXT_FACTORY,
                CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_INITIAL_CONTEXT_FACTORY));
        env.put(
                Context.PROVIDER_URL,
                CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_PROVIDER_URL_ECAS));
        env.put(
                Context.SECURITY_AUTHENTICATION,
                CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_AUTHENTICATION));
        env.put(
                Context.SECURITY_PRINCIPAL,
                CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_PRINCIPAL));
        env.put(
                Context.SECURITY_CREDENTIALS,
                CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_CREDENTIALS));
        if (CircabcConfig.ENT || CircabcConfig.ECHA) {
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

        Map<String, HashMap<String, String>> ldapData;
        ldapData = new HashMap<>();
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        final String ldapSearchString = "(cn=*)";
        try {
            ctx = new InitialDirContext(env);

            final SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            controls.setCountLimit(1000);
            results = ctx.search("", ldapSearchString, controls);
            SearchResult searchResult;
            Attributes attributes;
            String cn;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                cn = (String) attributes.get("cn").get();
                HashMap<String, String> map = new HashMap<>();
                for (NamingEnumeration ae = attributes.getAll(); ae.hasMore(); ) {
                    Attribute attr = (Attribute) ae.next();
                    final String id = attr.getID();
                    final String value = (String) attr.get();
                    if (id.startsWith("description")) {
                        map.put(id, value);
                    }
                }
                ldapData.put(cn, map);
            }

        } catch (NamingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when call getLdapData method", e);
            }
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
        return ldapData;
    }

    public Set<String> getAllEcasDomains() {
        return getCachedLdapData().keySet();
    }

    public Map<String, String> getDefaultEcasDomains() {
        Map<String, String> result = new HashMap<>(12);

        for (Map.Entry<String, HashMap<String, String>> element : getCachedLdapData().entrySet()) {
            result.put(element.getKey(), element.getValue().get("description"));
        }
        return result;
    }

    public Map<String, String> getEcasDomains(String language) {
        Map<String, String> result = new HashMap<>(12);

        for (Map.Entry<String, HashMap<String, String>> element : getCachedLdapData().entrySet()) {
            result.put(element.getKey(), element.getValue().get("description;lang-" + language));
        }
        return result;
    }

    public void setLdapECASDomainsCache(
            SimpleCache<String, Map<String, HashMap<String, String>>> ldapECASDomainsCache) {
        this.ldapECASDomainsCache = ldapECASDomainsCache;
    }
}
