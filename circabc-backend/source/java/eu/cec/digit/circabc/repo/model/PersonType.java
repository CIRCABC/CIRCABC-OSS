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
package eu.cec.digit.circabc.repo.model;

import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Class containing behavior for the person type. Inside Circabc, the email of an user <b>MUST</b>
 * be valid and unique.
 *
 * <p>{@link ContentModel#TYPE_PERSON person type}
 *
 * @author Yanick Pignot
 */
public class PersonType implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    //     Dependencies
    private PolicyComponent policyComponent;
    private UserService userService;
    private boolean validateDuplicateEmail;

    /**
     * Initialise the Person Type
     */
    public void init() {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onUpdateProperties"));
    }

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     * Check if the email defined is not already in use in circabc.
     */
    public void onUpdateProperties(
            final NodeRef nodeRef,
            final Map<QName, Serializable> before,
            Map<QName, Serializable> after) {
        if (!validateDuplicateEmail) {
            return;
        }

        final String oldEmail = (String) before.get(ContentModel.PROP_EMAIL);
        final String newEmail = (String) after.get(ContentModel.PROP_EMAIL);

        if (newEmail == null || newEmail.length() < 1) {
            if (oldEmail != null && oldEmail.length() > 0) {
                // The exception is throwed only if the properties are in a edit mode. The new
                // email can be null ONLY if the old email is null too. For example for the Alfresco Admin
                // created
                // at the startup.
                throw new IllegalStateException("The email of a person is mandatory.");
            }

        } else if (oldEmail == null || oldEmail.length() < 1 || !oldEmail.equalsIgnoreCase(newEmail)) {
            final Boolean exists =
                    (Boolean)
                            AuthenticationUtil.runAs(
                                    new AuthenticationUtil.RunAsWork<Object>() {
                                        public Object doWork() {
                                            boolean exists = userService.isEmailExists(newEmail, true);
                                            return exists;
                                        }
                                    },
                                    AuthenticationUtil.getSystemUserName());

            if (exists) {
                throw new IllegalArgumentException("The email of a person must be unique.");
            }
        }
    }

    /**
     * @param userService the UserService to set
     */
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public boolean isValidateDuplicateEmail() {
        return validateDuplicateEmail;
    }

    public void setValidateDuplicateEmail(boolean validateDuplicateEmail) {
        this.validateDuplicateEmail = validateDuplicateEmail;
    }
}
