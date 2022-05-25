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
package eu.cec.digit.circabc.service.profile.permissions;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProfilePermissions implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5879727028955430081L;
    /**
     * A logger for the class
     */
    private static transient Log logger = LogFactory.getLog(ProfilePermissions.class);
    private HashMap<String, ServicePermissions> servicesPermissions = new HashMap<>();

    @Override
    public String toString() {
        return "ProfilePermissions [servicesPermissions=" + servicesPermissions + "]";
    }

    /**
     * @param servicesPermissions HashMap<String serviceName, Set<String> permissions>
     */
    public void setServicesPermissions(final HashMap<String, Set<String>> servicesPermissions) {
        for (final Map.Entry<String, Set<String>> entry : servicesPermissions.entrySet()) {
            setServicePermissions(entry.getKey(), entry.getValue());
        }
    }

    public void setServicePermissions(final String serviceName, final Set<String> permissions) {
        ServicePermissions servicePermissions;
        if (servicesPermissions.containsKey(serviceName)) {
            servicePermissions = servicesPermissions.get(serviceName);
            servicePermissions.setPermissions(permissions);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("new ServicePermissions: " + serviceName);
            }
            servicePermissions = new ServicePermissions(serviceName);
            servicePermissions.setPermissions(permissions);
            servicesPermissions.put(serviceName, servicePermissions);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("set Permission: " + permissions + " for " + serviceName);
        }
    }

    public HashMap<String, ServicePermissions> getServicePermissions() {
        return servicesPermissions;
    }

    public void setServicePermissions(final Map<String, Set<String>> servicesPermissions) {
        for (final Map.Entry<String, Set<String>> entry : servicesPermissions.entrySet()) {
            setServicePermissions(entry.getKey(), entry.getValue());
        }
    }

    public Set<String> getPermissions(final String serviceName) {
        final ServicePermissions servicePermissions = servicesPermissions.get(serviceName);
        return servicePermissions.getPermissions();
    }

    public void clearNodePermissions(
            final NodeRef nodeRef,
            final String prefixedGroupName,
            final String serviceName,
            final ServiceRegistry serviceRegistry) {
        // final PermissionService permissionService = serviceRegistry.getPermissionService();
        final QName permissionServiceQName =
                QName.createQName(NamespaceService.ALFRESCO_URI, "permissionService");
        final PermissionService permissionService =
                (PermissionService) serviceRegistry.getService(permissionServiceQName);
        if (servicesPermissions != null && servicesPermissions.containsKey(serviceName)) {
            // final ServicePermissions servicePermissions = servicesPermissions.get(serviceName);
            // for (final String permission : servicePermissions.getPermissions())
            // {
            permissionService.deletePermission(nodeRef, prefixedGroupName, null);
            // }
        }
    }

    public void setNodePermissions(
            final NodeRef nodeRef,
            final String prefixedGroupName,
            final Map<String, Set<String>> servicesPermissions,
            final ServiceRegistry serviceRegistry) {
        final PermissionService permissionService = serviceRegistry.getPermissionService();
        if (logger.isTraceEnabled()) {
            logger.trace("setPermissions on node:" + nodeRef + "");
        }

        AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Object>() {
                    public Object doWork() {
                        Set<String> permissions;
                        for (final String serviceName : servicesPermissions.keySet()) {
                            setServicePermissions(serviceName, servicesPermissions.get(serviceName));

                            permissions = servicesPermissions.get(serviceName);
                            for (final String permission : permissions) {

                                permissionService.setPermission(nodeRef, prefixedGroupName, permission, true);

                                if (logger.isTraceEnabled()) {
                                    logger.trace(
                                            "setPermission: "
                                                    + prefixedGroupName
                                                    + " "
                                                    + permission
                                                    + " TRUE on node:"
                                                    + nodeRef);
                                }
                            }
                        }
                        return null;
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }
}
