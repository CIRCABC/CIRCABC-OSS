/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
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
package eu.cec.digit.circabc.repo.user;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.alfresco.model.ContentModel;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.lucene.queryParser.QueryParser;

import java.util.*;

public class LuceneUserServiceImpl implements LdapUserService {

    ProfileManagerServiceFactory profileManagerServiceFactory;

    /**
     * The search service reference
     */
    private SearchService searchService;

    /**
     * The node service reference
     */
    private NodeService nodeService;

    /**
     * The person service reference
     */
    private PersonService personService;

    /* IOC */

    /**
     * @return the profileManagerServiceFactory
     */
    protected final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public CircabcUserDataBean getLDAPUserDataByUid(String pLdapUserID) {
        CircabcUserDataBean userDataBean = null;
        if (getPersonService().personExists(pLdapUserID)) {
            NodeRef nodeRef = getPersonService().getPerson(pLdapUserID);
            userDataBean = new CircabcUserDataBean();
            userDataBean.setUserName(pLdapUserID);
            final String firstName =
                    (String) nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME);
            userDataBean.setFirstName(firstName);
            final String lastName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME);
            userDataBean.setLastName(lastName);
            final String email = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL);
            userDataBean.setEmail(email);
            final String ecasUserName =
                    (String) nodeService.getProperty(nodeRef, UserModel.PROP_ECAS_USER_NAME);
            userDataBean.setEcasUserName(ecasUserName);
            final String domain = (String) nodeService.getProperty(nodeRef, UserModel.PROP_DOMAIN);
            userDataBean.setDomain(domain);
        }
        return userDataBean;
    }

    public List<String> getLDAPUserIDByIdMonikerEmail(
            String uid, String moniker, String email, boolean conjunction) {
        final List<String> userIDs = new ArrayList<>();

        StringBuilder query = getLuceneQueryByUidEmail(uid, email, conjunction);
        String luceneQuery = query.toString();
        final List<NodeRef> users = executeLuceneQuery(luceneQuery);

        for (NodeRef user : users) {
            if (nodeService.exists(user)) {
                final String userId = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
                userIDs.add(userId);
            }
        }
        return userIDs;
    }

    private StringBuilder getLuceneQueryByUidEmail(String uid, String email, boolean conjunction) {

        final String logicalOperation = getLogicalOperator(conjunction);

        StringBuilder query = new StringBuilder(128);
        if (uid != null) {
            query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:userName:\"");
            query.append(uid);
            query.append("\"");
            if (email != null) {
                query.append(logicalOperation);
            }
        }

        if (email != null) {
            query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:email:\"*");
            query.append(email);
            query.append("*\"");
        }
        return query;
    }

    private StringBuilder getLuceneQueryByUidEmailCn(
            String uid, String email, String cn, boolean conjunction) {

        final String logicalOperation = getLogicalOperator(conjunction);

        StringBuilder query = getLuceneQueryByUidEmail(uid, email, conjunction);

        if (cn != null) {

            query.append(logicalOperation);

            query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:lastName:\"*");
            query.append(cn);
            query.append("*\"");
        }

        return query;
    }

    private String getLogicalOperator(boolean conjunction) {
        final String logicalOperation;
        if (conjunction) {
            logicalOperation = " AND ";
        } else {
            logicalOperation = " OR ";
        }
        return logicalOperation;
    }

    public List<String> getLDAPUserIDByMail(String mail) {
        final List<String> userIDs = new ArrayList<>();

        StringBuilder query = getLuceneQueryByEmail(mail);
        String luceneQuery = query.toString();
        final List<NodeRef> users = executeLuceneQuery(luceneQuery);

        for (NodeRef user : users) {
            if (nodeService.exists(user)) {
                final String userId = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
                userIDs.add(userId);
            }
        }
        return userIDs;
    }

    private StringBuilder getLuceneQueryByEmail(String mail) {
        StringBuilder query = new StringBuilder(128);
        query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:email:\"");
        query.append(mail);
        query.append("\"");
        return query;
    }

    public List<String> getLDAPUserIDByMailDomain(String mail, String domain) {

        final List<String> userIDs = getLDAPUserIDByMail(mail);

        return userIDs;
    }

    public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
            String pDomain, String pCriteria, boolean filter) {
        final String query = buildLuceneQuery(pCriteria.trim());
        final List<NodeRef> users = executeLuceneQuery(query);
        List<SearchResultRecord> result = new ArrayList<>();
        for (NodeRef user : users) {
            if (nodeService.exists(user)) {
                final String firstName =
                        (String) nodeService.getProperty(user, ContentModel.PROP_FIRSTNAME);
                final String lastName = (String) nodeService.getProperty(user, ContentModel.PROP_LASTNAME);
                final String userId = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
                final String email = (String) nodeService.getProperty(user, ContentModel.PROP_EMAIL);
                result.add(new SearchResultRecord(userId, firstName, lastName, email));
            }
        }
        return result;
    }

    private String buildLuceneQuery(String search) {

        StringBuilder query = new StringBuilder(128);
        for (StringTokenizer t = new StringTokenizer(search, " "); t.hasMoreTokens(); /**/) {
            String term = QueryParser.escape(t.nextToken());
            query.append(" @")
                    .append(NamespaceService.CONTENT_MODEL_PREFIX)
                    .append("\\:firstName:*")
                    .append(term)
                    .append("*");
            query.append(" @")
                    .append(NamespaceService.CONTENT_MODEL_PREFIX)
                    .append("\\:lastName:*")
                    .append(term)
                    .append("*");
            query.append(" @")
                    .append(NamespaceService.CONTENT_MODEL_PREFIX)
                    .append("\\:userName:")
                    .append(term)
                    .append("*");
        }
        return query.toString();
    }

    public void init() {
        // TODO Auto-generated method stub

    }

    private List<NodeRef> executeLuceneQuery(final String query) {

        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        params.setQuery(query);
        ResultSet results = getSearchService().query(params);
        try {
            return results.getNodeRefs();
        } finally {
            results.close();
        }
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public List<String> getLDAPUserIDByIdMonikerEmailCn(
            String uid, String moniker, String email, String cn, boolean conjunction) {

        final List<String> userIDs = new ArrayList<>();

        StringBuilder query = getLuceneQueryByUidEmailCn(uid, email, cn, conjunction);
        String luceneQuery = query.toString();
        final List<NodeRef> users = executeLuceneQuery(luceneQuery);

        for (NodeRef user : users) {
            if (nodeService.exists(user)) {
                final String userId = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
                userIDs.add(userId);
            }
        }
        return userIDs;
    }

    public List<String> getUsersByDomainFirstNameLastNameWithoutEmail(
            String pDomain, String pCriteria) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<SearchResultRecord> getUsersByMailDomain(String mail, String domain, boolean filter) {

        StringBuilder query = new StringBuilder(128);
        for (StringTokenizer t = new StringTokenizer(mail, " "); t.hasMoreTokens(); /**/) {
            String term = QueryParser.escape(t.nextToken());
            query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:email:\"*");
            query.append(term);
            query.append("*\"");
        }

        final List<NodeRef> users = executeLuceneQuery(query.toString());
        List<SearchResultRecord> result = new ArrayList<>();
        for (NodeRef user : users) {
            if (nodeService.exists(user)) {
                final String firstName =
                        (String) nodeService.getProperty(user, ContentModel.PROP_FIRSTNAME);
                final String lastName = (String) nodeService.getProperty(user, ContentModel.PROP_LASTNAME);
                final String userId = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
                final String email = (String) nodeService.getProperty(user, ContentModel.PROP_EMAIL);
                result.add(new SearchResultRecord(userId, firstName, lastName, email));
            }
        }
        return result;
    }

    public Map<String, String> getEcasUserDomains() {
        // TODO Auto-generated method stub
        return new HashMap<>();
    }

    @Override
    @Auditable
    public Boolean userExists(String uid) {
        final NodeRef person = getPersonService().getPersonOrNull(uid);
        return person != null;
    }

    @Override
    public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
        
        return getLDAPUserDataByUid(userID);
    }
}
