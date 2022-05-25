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
package eu.cec.digit.circabc.service.profile;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Profile {

    /**
     * @return the profileName
     */
    String getProfileName();

    /**
     * set the profileName
     */
    void setProfileName(final String profileName);

    /**
     * @return the alfrescoGroupName
     */
    String getAlfrescoGroupName();

    void setAlfrescoGroupName(final String alfrescoGroupName);

    String getPrefixedAlfrescoGroupName();

    void setPrefixedAlfrescoGroupName(final String prefixedAlfrescoGroupName);

    void clearNodeServicesPermissions(
            final NodeRef noderef, Set<String> services, final ServiceRegistry serviceRegistry);

    void setNodeServicesPermissions(
            final NodeRef nodeRef,
            final Map<String, Set<String>> servicesPermissions,
            final ServiceRegistry serviceRegistry);

    Set<String> getServicePermissions(final String serviceName);

    HashMap<String, Set<String>> getServicesPermissions();

    void setServicesPermissions(final Map<String, Set<String>> servicesPermissions);

    MLText getTitle();

    void setTitles(final MLText titles);

    MLText getDescription();

    void setDescriptions(final MLText descriptions);

    boolean isExported();

    void setExported(final boolean isExported);

    boolean isImported();

    void setImported(final boolean isImported);

    NodeRef getImportedNodeRef();

    void setImportedNodeRef(final NodeRef importedNodeRef);

    String getImportedNodeName();

    void setImportedNodeName(final String property);

    String getProfileDisplayName();

    String getProfileDescription();

    boolean isAdmin();

    NodeRef getNodeRef();

    void setNodeRef(NodeRef value);
}
