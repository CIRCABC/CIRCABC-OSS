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
package eu.cec.digit.circabc.service.user;

/*--+
|     Copyright European Community 2006 - Licensed under the EUPL V.1.0
|
|          http://ec.europa.eu/idabc/en/document/6523
|
+--*/

import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.alfresco.service.Auditable;

import java.util.List;
import java.util.Map;

/**
 * It is a spring bean that manages basic operations on User over LDAP
 *
 * @author stephane Clinckart
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface LdapUserService {

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pLdapUserID"})
    CircabcUserDataBean getLDAPUserDataByUid(final String pLdapUserID);

    CircabcUserDataBean getLDAPUserDataNoFilterByUid(final String userID);

    @Auditable(
            /*key = Auditable.Key.NO_KEY, */ parameters = {
            "uid",
            "moniker",
            "email",
            "cn",
            "conjunction"
    })
    List<String> getLDAPUserIDByIdMonikerEmailCn(
            final String uid,
            final String moniker,
            final String email,
            final String cn,
            final boolean conjunction);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pMail"})
    List<String> getLDAPUserIDByMail(String mail);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"mail", "domain"})
    List<String> getLDAPUserIDByMailDomain(final String mail, final String domain);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pDomain", "pCriteria", "filter"})
    List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
            final String pDomain, final String pCriteria, boolean filter);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"mail", "domain", "filter"})
    List<SearchResultRecord> getUsersByMailDomain(final String mail, final String domain, boolean filter);

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    Map<String, String> getEcasUserDomains();

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    Boolean userExists(String uid);

    /**
     * Initialise method
     */
    void init();
}
