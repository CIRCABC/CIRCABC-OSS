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
import java.util.HashSet;
import java.util.Set;

/**
 * Domain model that groups together the set of permissions granted for a single CIRCABC service
 * (for example the Library, Newsgroup, Information or Events service of an Interest Group).
 *
 * <p>Each instance associates a service, identified by its name, with the collection of permission
 * identifiers that apply to that service. It is used as a simple data-transfer object when
 * exposing or aggregating per-service permission information through the REST API.
 *
 * <p>The class is {@link Serializable} so that instances can be transported or cached as needed.
 */
public class ServicePermissions implements Serializable {

  /** Serialization version identifier used to ensure compatibility across (de)serialization. */
  private static final long serialVersionUID = -2568781927774798099L;

  /** Name identifying the service to which the {@link #permissions} apply. */
  private String serviceName;

  /** Set of permission identifiers granted for the {@link #serviceName service}. */
  private Set<String> permissions = new HashSet<>();

  /**
   * Creates a new {@code ServicePermissions} for the given service, initialising it with an empty,
   * modifiable set of permissions.
   *
   * @param serviceName the name of the service these permissions relate to
   */
  public ServicePermissions(final String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Returns the name of the service these permissions relate to.
   *
   * @return the serviceName
   */
  public final String getServiceName() {
    return serviceName;
  }

  /**
   * Returns a human-readable representation of this object, including the service name and its
   * permissions, primarily intended for logging and debugging.
   *
   * @return a string representation of this {@code ServicePermissions}
   */
  @Override
  public String toString() {
    return (
      "ServicePermissions [serviceName=" +
      serviceName +
      ", permissions=" +
      permissions +
      "]"
    );
  }

  /**
   * Returns the set of permission identifiers granted for this service.
   *
   * @return the permissions
   */
  public final Set<String> getPermissions() {
    return permissions;
  }

  /**
   * Replaces the set of permission identifiers granted for this service.
   *
   * @param permissions the permissions to set for this service
   */
  public final void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }
}
