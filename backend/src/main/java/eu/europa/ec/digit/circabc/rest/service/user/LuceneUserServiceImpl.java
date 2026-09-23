package eu.europa.ec.digit.circabc.rest.service.user;

import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.alfresco.UserModel;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link LdapUserService} implementation that resolves user information by querying the Alfresco
 * repository rather than an external LDAP directory.
 *
 * <p>User (person) nodes are stored in the workspace store and are looked up either directly via
 * the {@link PersonService} or by executing Lucene full-text queries through the
 * {@link SearchService}. Matching person nodes are then mapped to CIRCABC user data / search-result
 * beans by reading their {@link ContentModel} and {@link UserModel} properties from the
 * {@link NodeService}.
 */
public class LuceneUserServiceImpl implements LdapUserService {

  /** Alfresco service used to check for and retrieve person nodes by user id. */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to read person node properties (name, email, domain, etc.). */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to execute Lucene queries against the workspace store. */
  @Autowired
  private SearchService searchService;

  /**
   * Lifecycle hook invoked after the bean is constructed. Intentionally empty: all wiring is
   * performed by Spring dependency injection.
   */
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public void init() {
    // Initialization handled by Spring dependency injection
  }

  /**
   * Retrieves the CIRCABC user data for the given user id.
   *
   * @param pLdapUserID the user id (username) to look up
   * @return a {@link CircabcUserDataBean} populated with the person's first name, last name, email,
   *     ECAS username and domain, or {@code null} if no person exists for the given id
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataByUid(String pLdapUserID) {
    CircabcUserDataBean userDataBean = null;
    if (personService.personExists(pLdapUserID)) {
      NodeRef nodeRef = personService.getPerson(pLdapUserID);
      userDataBean = new CircabcUserDataBean();
      userDataBean.setUserName(pLdapUserID);
      final String firstName = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_FIRSTNAME
      );
      userDataBean.setFirstName(firstName);
      final String lastName = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_LASTNAME
      );
      userDataBean.setLastName(lastName);
      final String email = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_EMAIL
      );
      userDataBean.setEmail(email);
      final String ecasUserName = (String) nodeService.getProperty(
        nodeRef,
        UserModel.PROP_ECAS_USER_NAME
      );
      userDataBean.setEcasUserName(ecasUserName);
      final String domain = (String) nodeService.getProperty(
        nodeRef,
        UserModel.PROP_DOMAIN
      );
      userDataBean.setDomain(domain);
    }
    return userDataBean;
  }

  /**
   * Retrieves the CIRCABC user data for the given user id without applying any filtering. In this
   * implementation it simply delegates to {@link #getLDAPUserDataByUid(String)}.
   *
   * @param userID the user id (username) to look up
   * @return a {@link CircabcUserDataBean} for the user, or {@code null} if no such person exists
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
    return getLDAPUserDataByUid(userID);
  }

  /**
   * Finds the ids of users matching the supplied criteria by building and executing a Lucene query.
   *
   * <p>The {@code moniker} argument is accepted for interface compatibility but is not used to build
   * the query in this implementation; only {@code uid}, {@code email} and {@code cn} (mapped to last
   * name) are matched. {@code null} criteria are ignored.
   *
   * @param uid the username to match, or {@code null} to ignore
   * @param moniker the moniker to match (unused in this implementation)
   * @param email the email to match (substring), or {@code null} to ignore
   * @param cn the common/last name to match (substring), or {@code null} to ignore
   * @param conjunction if {@code true} the criteria are combined with {@code AND}, otherwise with
   *     {@code OR}
   * @return the list of usernames of the matching, still-existing person nodes; never {@code null}
   */
  @Override
  public List<String> getLDAPUserIDByIdMonikerEmailCn(
    String uid,
    String moniker,
    String email,
    String cn,
    boolean conjunction
  ) {
    final List<String> userIDs = new ArrayList<>();

    StringBuilder query = getLuceneQueryByUidEmailCn(
      uid,
      email,
      cn,
      conjunction
    );
    String luceneQuery = query.toString();
    final List<NodeRef> users = executeLuceneQuery(luceneQuery);

    for (NodeRef user : users) {
      if (nodeService.exists(user)) {
        final String userId = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_USERNAME
        );
        userIDs.add(userId);
      }
    }
    return userIDs;
  }

  private StringBuilder getLuceneQueryByUidEmailCn(
    String uid,
    String email,
    String cn,
    boolean conjunction
  ) {
    final String logicalOperation = getLogicalOperator(conjunction);

    StringBuilder query = getLuceneQueryByUidEmail(uid, email, conjunction);

    if (cn != null) {
      query.append(logicalOperation);

      query
        .append("@")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:lastName:\"*");
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

  private StringBuilder getLuceneQueryByUidEmail(
    String uid,
    String email,
    boolean conjunction
  ) {
    final String logicalOperation = getLogicalOperator(conjunction);

    StringBuilder query = new StringBuilder(128);
    if (uid != null) {
      query
        .append("@")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:userName:\"");
      query.append(uid);
      query.append("\"");
      if (email != null) {
        query.append(logicalOperation);
      }
    }

    if (email != null) {
      query
        .append("@")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:email:\"*");
      query.append(email);
      query.append("*\"");
    }
    return query;
  }

  /**
   * Searches for users whose first name, last name or username match the given free-text criteria.
   *
   * <p>The criteria string is split on whitespace and every token must match (as a substring) one of
   * the first name, last name or username fields. The {@code pDomain} and {@code filter} arguments
   * are accepted for interface compatibility but are not applied in this implementation.
   *
   * @param pDomain the domain to restrict the search to (unused in this implementation)
   * @param pCriteria the free-text search criteria; leading/trailing whitespace is trimmed
   * @param filter whether results should be filtered (unused in this implementation)
   * @return the list of {@link SearchResultRecord} for the matching, still-existing users; never
   *     {@code null}
   */
  public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
    String pDomain,
    String pCriteria,
    boolean filter
  ) {
    final String query = buildLuceneQuery(pCriteria.trim());
    final List<NodeRef> users = executeLuceneQuery(query);
    List<SearchResultRecord> result = new ArrayList<>();
    for (NodeRef user : users) {
      if (nodeService.exists(user)) {
        final String firstName = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_FIRSTNAME
        );
        final String lastName = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_LASTNAME
        );
        final String userId = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_USERNAME
        );
        final String email = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_EMAIL
        );
        result.add(new SearchResultRecord(userId, firstName, lastName, email));
      }
    }
    return result;
  }

  private String buildLuceneQuery(String search) {
    StringBuilder query = new StringBuilder(128);
    for (
      StringTokenizer t = new StringTokenizer(search, " ");
      t.hasMoreTokens() /**/;

    ) {
      String term = QueryParserBase.escape(t.nextToken());
      query
        .append(" @")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:firstName:*")
        .append(term)
        .append("*");
      query
        .append(" @")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:lastName:*")
        .append(term)
        .append("*");
      query
        .append(" @")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:userName:")
        .append(term)
        .append("*");
    }
    return query.toString();
  }

  private List<NodeRef> executeLuceneQuery(final String query) {
    SearchParameters params = new SearchParameters();
    params.setLanguage(SearchService.LANGUAGE_LUCENE);
    params.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    params.setQuery(query);
    ResultSet results = searchService.query(params);
    try {
      return results.getNodeRefs();
    } finally {
      results.close();
    }
  }

  /**
   * Searches for users whose email address matches the given criteria.
   *
   * <p>The {@code mail} string is split on whitespace and each token is matched as a substring
   * against the email field. The {@code domain} and {@code filter} arguments are accepted for
   * interface compatibility but are not applied in this implementation.
   *
   * @param mail the email search criteria; may contain multiple whitespace-separated tokens
   * @param domain the domain to restrict the search to (unused in this implementation)
   * @param filter whether results should be filtered (unused in this implementation)
   * @return the list of {@link SearchResultRecord} for the matching, still-existing users; never
   *     {@code null}
   */
  @Override
  public List<SearchResultRecord> getUsersByMailDomain(
    String mail,
    String domain,
    boolean filter
  ) {
    StringBuilder query = new StringBuilder(128);
    for (
      StringTokenizer t = new StringTokenizer(mail, " ");
      t.hasMoreTokens() /**/;

    ) {
      String term = QueryParserBase.escape(t.nextToken());
      query
        .append("@")
        .append(NamespaceService.CONTENT_MODEL_PREFIX)
        .append("\\:email:\"*");
      query.append(term);
      query.append("*\"");
    }

    final List<NodeRef> users = executeLuceneQuery(query.toString());
    List<SearchResultRecord> result = new ArrayList<>();
    for (NodeRef user : users) {
      if (nodeService.exists(user)) {
        final String firstName = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_FIRSTNAME
        );
        final String lastName = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_LASTNAME
        );
        final String userId = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_USERNAME
        );
        final String email = (String) nodeService.getProperty(
          user,
          ContentModel.PROP_EMAIL
        );
        result.add(new SearchResultRecord(userId, firstName, lastName, email));
      }
    }
    return result;
  }
}
