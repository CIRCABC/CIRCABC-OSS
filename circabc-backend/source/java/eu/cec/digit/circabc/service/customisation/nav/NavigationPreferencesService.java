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
package eu.cec.digit.circabc.service.customisation.nav;

import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for the customisation of the navigation thought Interest Groups. Under any Interest
 * Group service (Inforation, library, ...), we can define own navigation properties: Column
 * properties, actions, list size ...
 *
 * @author Yanick Pignot
 * @see eu.cec.digit.circabc.service.customisation.NodePreferencesService
 */
public interface NavigationPreferencesService {

    String SEARCH_SERVICE = "search";
    String LIBRARY_SERVICE = "library";
    String INFORMATION_SERVICE = "information";
    String NEWSGROUP_SERVICE = "newsgroup";

    String DISCUSSION_TYPE = "discussion";
    String CONTAINER_TYPE = "container";
    String CONTENT_TYPE = "content";
    String FORUM_TYPE = "forum";
    String TOPIC_TYPE = "topic";

    /**
     * Return the navigation preference of a sepecif service of a given interest group.
     */
    NodeRef addServicePreference(
            final NodeRef interestGroup,
            final String serviceName,
            final String nodeType,
            final NavigationPreference navigationPreference)
            throws CustomizationException;

    /**
     * Return the navigation preference of a sepecif service of a given interest group.
     */
    NavigationPreference getServicePreference(
            final NodeRef interestGroup, final String serviceName, final String nodeType);

    /**
     * Remove a given navigation preference for a given node.
     */
    void removeServicePreference(
            final NodeRef interestGroup, final String serviceName, final String nodeType)
            throws CustomizationException;
}
