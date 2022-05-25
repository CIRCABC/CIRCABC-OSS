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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ServicePermissions implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2568781927774798099L;

    private String serviceName;

    private Set<String> permissions = new HashSet<>();

    private ServicePermissions() {
    }

    public ServicePermissions(final String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the serviceName
     */
    public final String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "ServicePermissions [serviceName=" + serviceName + ", permissions=" + permissions + "]";
    }

    /**
     * @return the permissions
     */
    public final Set<String> getPermissions() {
        return permissions;
    }

    /**
     * Set the permissions
     */
    public final void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
