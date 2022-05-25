/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.helper;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yanick Pignot
 */
public class UserManager {

    private static final String PREFERENCES_XPATH =
            NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
    private static final String SEARCH_EMAIL_LUCENE = "TYPE:\\'{'http\\://www.alfresco.org/model/content/1.0\\'}'person +@cm\\:email:\"{0}\"";

    private final Log logger = LogFactory.getLog(UserManager.class);

    private NodeService nodeService;
    private PersonService personService;
    private AuthorityService authorityService;
    private ConfigurableService configurableService;
    private SearchService searchService;
    private NamespaceService namespaceService;

    /**
     * Check if the given username exists
     *
     * @param userName The username to check
     * @return true if user exists
     */
    public boolean personExists(final String userName) {
        return personService.personExists(userName);
    }

    /**
     * Get the node ref of the given user
     *
     * @param userName The username to query
     * @return The person node reference or null if user not exists
     */
    public NodeRef getPerson(final String userName) {
        if (personService.personExists(userName)) {
            return personService.getPerson(userName);
        } else {
            return null;
        }
    }

    /**
     * Check if the given username is guest
     *
     * @param userName The username to check
     * @return true if user is guest
     */
    public boolean isGuest(final String userName) {
        return authorityService.isGuestAuthority(userName);
    }

    /**
     * Check if the given username is super admin (Alfresco admin)
     *
     * @param userName The username to check
     * @return true if user is super admin
     */
    public boolean isSuperAdmin(final String userName) {
        return authorityService.isAdminAuthority(userName);
    }

    /**
     * Get the user preference noderef
     *
     * @param uperson the person to query
     * @return the person configuration ref if setted or null.
     */
    public NodeRef getUserPreferencesRef(final NodeRef person) {
        return getOrCreateUserPreferencesRef(person, false);
    }

    /**
     * create and return the user preference noderef
     *
     * @param person the person to query
     * @return the person configuration
     */
    public NodeRef createUserPreferencesRef(final NodeRef person) {
        return getOrCreateUserPreferencesRef(person, true);
    }

    /**
     * Get all properties of a user
     *
     * @param person The person to query
     * @return All properties
     */
    public Map<QName, Serializable> getUserProperties(final NodeRef person) {
        return nodeService.getProperties(person);
    }

    /**
     * Get the user property identified by the qname
     *
     * @param person The person to query
     * @param qname  The property identifier
     * @return The requested property or null if not setted.
     */
    public Serializable getUserProperty(final NodeRef person, final QName qname) {
        return getUserProperties(person).get(qname);
    }

    /**
     * Get all preferences of a user
     *
     * @param person The person to query
     * @return All preferences of a user or an empty map if not configurable yet
     */
    public Map<QName, Serializable> getUserPreferences(final NodeRef person) {
        final NodeRef preferenceRef = getOrCreateUserPreferencesRef(person, false);

        if (preferenceRef == null) {
            return new HashMap<>();
        } else {
            return nodeService.getProperties(preferenceRef);
        }
    }

    /**
     * Get the user preference identified by the qname
     *
     * @param person The person to query
     * @param qname  The preference identifier
     * @return The requested preference or null if not setted.
     */
    public Serializable getUserPreference(final NodeRef person, final QName qname) {
        return getUserPreferences(person).get(qname);
    }

    /**
     * Check if the given email is already in use by any user in circabc
     *
     * @param email To query
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.length() < 1) {
            return false;
        } else {
            boolean emailExists = false;

            final SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(MessageFormat.format(SEARCH_EMAIL_LUCENE, email));
            sp.addStore(personService.getPeopleContainer().getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            ResultSet rs = null;
            try {
                rs = searchService.query(sp);

                if (rs.length() != 0) {
                    emailExists = true;
                }
            } catch (final Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e);
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (final Exception ignore) {
                    }
                }
            }

            return emailExists;
        }
    }

    //--------------
    //-- private helpers

    private NodeRef getOrCreateUserPreferencesRef(final NodeRef person,
                                                  final boolean createIfMissing) {
        NodeRef prefRef = null;

        if (nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE) == false) {
            if (createIfMissing) {
                // create the configuration folder for this Person node
                configurableService.makeConfigurable(person);
            }
        } else {
            // target of the assoc is the configurations folder ref
            final NodeRef configRef = configurableService.getConfigurationFolder(person);
            if (configRef == null) {
                logger.error("Unable to find associated 'configurations' folder for node: " + person);
            } else {
                final List<NodeRef> nodes = searchService
                        .selectNodes(configRef, PREFERENCES_XPATH, null, namespaceService, false);

                if (nodes.size() == 1) {
                    prefRef = nodes.get(0);
                } else {
                    //	create the preferences Node for this user
                    final ChildAssociationRef childRef = nodeService.createNode(
                            configRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "preferences"),
                            ContentModel.TYPE_CMOBJECT);

                    prefRef = childRef.getChildRef();
                }
            }
        }

        return prefRef;
    }

    //--------------
    //-- IOC


    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param configurableService the configurableService to set
     */
    public final void setConfigurableService(ConfigurableService configurableService) {
        this.configurableService = configurableService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

}
