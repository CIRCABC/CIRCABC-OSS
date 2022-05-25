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
/*--+
|     Copyright European Community 2011 - Licensed under the EUPL V.1.0
|
|          http://ec.europa.eu/idabc/en/document/6523
|
+--*/

package eu.cec.digit.circabc.service.authentication;

import org.alfresco.repo.security.authentication.AuthenticationServiceImpl;

/**
 * Service that was implemented to test why the ECAS authentication subsystem is destroying the user
 * context...
 *
 * @author schwerr
 */
public class EcasAuthenticationServiceImpl extends AuthenticationServiceImpl {

    /**
     * Must always return false since the authentication must always exist in Alfresco to determine if
     * it should be created.
     *
     * @see org.alfresco.repo.security.authentication.AuthenticationServiceImpl#authenticationExists(java.lang.String)
     */
    @Override
    public boolean authenticationExists(String userName) {
        return false;
    }
}
