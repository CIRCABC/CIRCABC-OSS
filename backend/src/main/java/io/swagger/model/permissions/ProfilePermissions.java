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
package io.swagger.model.permissions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents the set of permissions granted to a CIRCABC profile, grouped per Interest Group
 * service (e.g. Library, Newsgroup, Information, Events).
 *
 * <p>Internally the permissions are stored in a map keyed by service name, where each value is a
 * {@link ServicePermissions} holding the concrete Alfresco permission names for that service. In
 * addition to being a serializable data holder, this class provides helper operations to apply or
 * clear the corresponding permissions on Alfresco nodes for a given (prefixed) group name via the
 * Alfresco {@link PermissionService}.
 */
public class ProfilePermissions implements Serializable {

  /**
   * Serialization version identifier for this {@link Serializable} class.
   */
  private static final long serialVersionUID = 5879727028955430081L;
  /**
   * A logger for the class
   */
  private static transient Log logger = LogFactory.getLog(
    ProfilePermissions.class
  );
  /**
   * The permissions held by this profile, keyed by service name; each entry maps a service name to
   * its {@link ServicePermissions} wrapper containing the granted permission names.
   */
  private HashMap<String, ServicePermissions> servicesPermissions =
    new HashMap<>();

  /**
   * Returns a human-readable representation of this profile's per-service permissions.
   *
   * @return a string describing the current {@code servicesPermissions} map
   */
  @Override
  public String toString() {
    return (
      "ProfilePermissions [servicesPermissions=" + servicesPermissions + "]"
    );
  }

  /**
   * @param servicesPermissions {@code HashMap<String serviceName, Set<String> permissions>}
   */
  public void setServicesPermissions(
    final Map<String, Set<String>> servicesPermissions
  ) {
    for (final Map.Entry<
      String,
      Set<String>
    > entry : servicesPermissions.entrySet()) {
      setServicePermissions(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Adds or replaces the permissions of a single service for this profile. If the service is
   * already known, its existing {@link ServicePermissions} entry is updated; otherwise a new entry
   * is created and stored.
   *
   * @param serviceName the name of the service whose permissions are being set
   * @param permissions the set of permission names to associate with the service
   */
  public void setServicePermissions(
    final String serviceName,
    final Set<String> permissions
  ) {
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

  /**
   * Returns the per-service permissions held by this profile.
   *
   * @return a map from service name to its {@link ServicePermissions}
   */
  public Map<String, ServicePermissions> getServicePermissions() {
    return servicesPermissions;
  }

  /**
   * Adds or replaces the permissions for multiple services at once. Each entry in the supplied map
   * is applied through {@link #setServicePermissions(String, Set)}.
   *
   * @param servicesPermissions a map from service name to the set of permission names to grant
   */
  public void setServicePermissions(
    final Map<String, Set<String>> servicesPermissions
  ) {
    for (final Map.Entry<
      String,
      Set<String>
    > entry : servicesPermissions.entrySet()) {
      setServicePermissions(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Returns the permission names granted for a given service.
   *
   * @param serviceName the name of the service to look up
   * @return the set of permission names associated with the service
   * @throws NullPointerException if no permissions are registered for the given service name
   */
  public Set<String> getPermissions(final String serviceName) {
    final ServicePermissions servicePermissions = servicesPermissions.get(
      serviceName
    );
    return servicePermissions.getPermissions();
  }

  /**
   * Removes the permissions granted to the given group on the specified node, but only if this
   * profile actually holds permissions for the given service. The permission is deleted via the
   * Alfresco {@link PermissionService} resolved from the service registry.
   *
   * @param nodeRef the node on which the permission should be cleared
   * @param prefixedGroupName the prefixed authority (group) name whose permission is removed
   * @param serviceName the service name used to decide whether any permission must be cleared
   * @param serviceRegistry the Alfresco service registry used to resolve the {@link
   *     PermissionService}
   */
  public void clearNodePermissions(
    final NodeRef nodeRef,
    final String prefixedGroupName,
    final String serviceName,
    final ServiceRegistry serviceRegistry
  ) {
    final QName permissionServiceQName = QName.createQName(
      NamespaceService.ALFRESCO_URI,
      "permissionService"
    );
    final PermissionService permissionService =
      (PermissionService) serviceRegistry.getService(permissionServiceQName);
    if (
      servicesPermissions != null &&
      servicesPermissions.containsKey(serviceName)
    ) {
      permissionService.deletePermission(nodeRef, prefixedGroupName, null);
    }
  }

  /**
   * Grants the given group all the supplied per-service permissions on the specified node. This
   * profile's internal state is updated for each service, and every individual permission is
   * applied on the node through the Alfresco {@link PermissionService}. The permission changes are
   * executed as the system user via {@link AuthenticationUtil#runAs}.
   *
   * @param nodeRef the node on which the permissions should be set
   * @param prefixedGroupName the prefixed authority (group) name to grant the permissions to
   * @param servicesPermissions a map from service name to the set of permission names to grant
   * @param serviceRegistry the Alfresco service registry used to obtain the {@link
   *     PermissionService}
   */
  public void setNodePermissions(
    final NodeRef nodeRef,
    final String prefixedGroupName,
    final Map<String, Set<String>> servicesPermissions,
    final ServiceRegistry serviceRegistry
  ) {
    final PermissionService permissionService =
      serviceRegistry.getPermissionService();
    if (logger.isTraceEnabled()) {
      logger.trace("setPermissions on node:" + nodeRef);
    }

    AuthenticationUtil.runAs(
      () -> {
        Set<String> permissions;
        for (final Map.Entry<
          String,
          Set<String>
        > entry : servicesPermissions.entrySet()) {
          final String serviceName = entry.getKey();
          final Set<String> servicePermissions = entry.getValue();
          setServicePermissions(serviceName, servicePermissions);

          permissions = servicePermissions;
          for (final String permission : permissions) {
            permissionService.setPermission(
              nodeRef,
              prefixedGroupName,
              permission,
              true
            );

            if (logger.isTraceEnabled()) {
              logger.trace(
                "setPermission: " +
                  prefixedGroupName +
                  " " +
                  permission +
                  " TRUE on node:" +
                  nodeRef
              );
            }
          }
        }
        return null;
      },
      AuthenticationUtil.getSystemUserName()
    );
  }
}
