package eu.europa.ec.digit.circabc.rest.service.user;

import eu.europa.ec.digit.circabc.rest.exception.LdapAccessException;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.SearchResultRecordComparator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.naming.Context;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LdapUserService} that resolves and searches
 * CIRCABC user information against an external LDAP directory (the EC OpenLDAP /
 * EU Login directory).
 *
 * <p>The service builds LDAP search filters, executes queries through JNDI
 * ({@link javax.naming.directory.InitialDirContext} /
 * {@link javax.naming.ldap.InitialLdapContext}) and maps the returned directory
 * attributes onto CIRCABC domain objects such as {@link CircabcUserDataBean} and
 * {@link SearchResultRecord}. Connection parameters (provider URL, credentials,
 * authentication mechanism, etc.) are taken from {@link CircabcConfig}, and the
 * set of known EU Login domains is provided by {@link LdapEcasDomainService}.</p>
 *
 * <p>All user-supplied search terms are escaped with
 * {@link org.owasp.esapi.ESAPI ESAPI} before being embedded into LDAP filters to
 * mitigate LDAP injection. Searches are bounded by a fixed count limit and search
 * scope; when the directory returns too many results a
 * {@link LdapLimitExceededException} is raised, and other JNDI failures are
 * wrapped in a {@link LdapAccessException}.</p>
 */
public class LdapUserServiceImpl implements LdapUserService {

  private static final String BLANK_DELIM = " ";
  private static final String CN = "cn";
  // Count limit for user search queries (autocomplete, search by name/email)
  private static final int LDAP_SEARCH_COUNT_LIMIT = 20;
  // Count limit for single user lookup by uid - only 1 result expected
  private static final int LDAP_SINGLE_USER_COUNT_LIMIT = 1;
  private static final int LDAP_SEARCH_SCOPE_LEVEL =
    SearchControls.ONELEVEL_SCOPE; // SearchControls.SUBTREE_SCOPE
  private static final String UID = "uid";
  private static final String MODIFICATION_DATE = "modificationDate";
  private static final String EC_MONIKER = "ecMoniker";
  private static final String O = "o";
  private static final String PHYSICAL_DELIVERY_OFFICE_NAME =
    "physicalDeliveryOfficeName";
  private static final String DESCRIPTION = "description";
  private static final String FACSIMILE_TELEPHONE_NUMBER =
    "facsimileTelephoneNumber";
  private static final String DEPARTMENT_NUMBER = "departmentNumber";
  private static final String TELEPHONE_NUMBER = "ecTelephoneNumber";
  private static final String TITLE = "title";
  private static final String SN = "sn";
  private static final String GIVEN_NAME = "givenName";
  private static final String MAIL = "mail";
  private static final String SOURCE_ORGANISATION = "sourceOrganisation";
  private static final String DG = "dg";

  @SuppressWarnings("unused")
  private static final String RECORDSTATUS = "recordStatus";

  private static final String ERROR_LIMIT_EXCEEDED =
    "Error LimitExceededException accessing Ldap with query:";
  private static final String WITH_RETURN_ATTRIBUTES =
    " with return attributes:";
  private static final String WITH_COUNT_LIMIT = " with countLimit:";
  private static final String WITH_SCOPE_LIMIT = " with scope Limit:";
  private static final String ERROR_ACCESSING_LDAP =
    "Error accessing Ldap with query:";

  /**
   * Logger
   */
  private static final Log logger = LogFactory.getLog(
    LdapUserServiceImpl.class
  );

  /**
   * JNDI environment used to open LDAP contexts. Populated by {@link #init()}
   * from {@link CircabcConfig} (context factory, provider URL, security
   * credentials, connection pooling). Remains empty when LDAP is disabled.
   */
  @SuppressWarnings("java:S1149")
  private Hashtable<String, String> env;

  /**
   * Configuration holder providing LDAP connection settings and the flag that
   * indicates whether LDAP integration is enabled.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Provides the set of known EU Login (ECAS) domains, used to build the
   * "all domains" LDAP filter.
   */
  @Autowired
  private LdapEcasDomainService ldapEcasDomainService;

  /**
   * Initializes the JNDI {@link #env environment} from {@link CircabcConfig}.
   *
   * <p>Typically invoked as a Spring bean init method. When LDAP is disabled
   * ({@link CircabcConfig#isUseLDAP()} returns {@code false}) the environment is
   * created empty and no connection settings are added.</p>
   */
  public void init() {
    env = new Hashtable<>();
    if (circabcConfig.isUseLDAP()) {
      env.put(
        Context.INITIAL_CONTEXT_FACTORY,
        circabcConfig.getContextFactory()
      );

      env.put(Context.PROVIDER_URL, circabcConfig.getProviderURL());

      env.put(
        Context.SECURITY_AUTHENTICATION,
        circabcConfig.getSecurityAuthentication()
      );

      env.put(Context.SECURITY_PRINCIPAL, circabcConfig.getSecurityPrincipal());

      env.put(
        Context.SECURITY_CREDENTIALS,
        circabcConfig.getSecurityCredentials()
      );

      env.put("com.sun.jndi.ldap.connect.pool", "true");

      // Follow LDAP referrals (required to access external users in EUDS)
      env.put(Context.REFERRAL, "follow");

      // LDAP connection and read timeouts to prevent threads from hanging indefinitely
      // Connection timeout: 10 seconds - time to establish TCP connection
      env.put("com.sun.jndi.ldap.connect.timeout", "10000");
      // Read timeout: 30 seconds - time to wait for LDAP server response
      env.put("com.sun.jndi.ldap.read.timeout", "30000");
    }
  }

  /**
   * Looks up a single user in LDAP by their unique identifier (uid).
   *
   * <p>No EU Login record-status / employee-type / domain filtering is applied;
   * only a uid and a "has mail" constraint are used.</p>
   *
   * @param ldapUserID the LDAP uid of the user to look up
   * @return the populated {@link CircabcUserDataBean} for the user, or
   *         {@code null} if no matching entry is found
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataByUid(String ldapUserID) {
    return getLDAPUserDataByUid(ldapUserID, false);
  }

  private CircabcUserDataBean getLDAPUserDataByUid(
    final String pLdapUserID,
    boolean applyEULoginFilter
  ) {
    CircabcUserDataBean userDataBean = null;
    String ldapSearchString = buildLdapSearchString(
      pLdapUserID,
      applyEULoginFilter
    );
    String[] returningAttrs = {
      UID,
      CN,
      MAIL,
      GIVEN_NAME,
      SN,
      TITLE,
      TELEPHONE_NUMBER,
      DEPARTMENT_NUMBER,
      FACSIMILE_TELEPHONE_NUMBER,
      DESCRIPTION,
      PHYSICAL_DELIVERY_OFFICE_NAME,
      MODIFICATION_DATE,
      O,
      EC_MONIKER,
      SOURCE_ORGANISATION,
      DG,
    };

    try (
      CloseableDirContext ctx = new CloseableDirContext(
        new InitialDirContext(env)
      )
    ) {
      SearchControls controls = new SearchControls();
      controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
      // Use lower count limit for single user lookup - only 1 result expected
      controls.setCountLimit(LDAP_SINGLE_USER_COUNT_LIMIT);
      controls.setReturningAttributes(returningAttrs);
      NamingEnumeration<?> results = ctx
        .getContext()
        .search("", ldapSearchString, controls);
      while (results.hasMore()) {
        userDataBean = new CircabcUserDataBean();
        userDataBean.setUserName(pLdapUserID);
        Attributes attributes = ((SearchResult) results.next()).getAttributes();
        populateUserDataBean(userDataBean, attributes);
      }
    } catch (NamingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error during LDAP search for user " + pLdapUserID, e);
      }
    }
    return userDataBean;
  }

  private String buildLdapSearchString(
    String pLdapUserID,
    boolean applyEULoginFilter
  ) {
    String base =
      "(&" + getUidFilter(encodeForLDAP(pLdapUserID)) + getMailFilter("*@*");
    return applyEULoginFilter
      ? base +
        getRecordStatusFilter() +
        getEmployeTypeFilter() +
        getDomainFilter("") +
        ")"
      : base + ")";
  }

  private void populateUserDataBean(
    CircabcUserDataBean userDataBean,
    Attributes attributes
  ) throws NamingException {
    setAttributeIfPresent(attributes, MAIL, userDataBean::setEmail);
    setAttributeIfPresent(attributes, GIVEN_NAME, userDataBean::setFirstName);
    setAttributeIfPresent(attributes, SN, userDataBean::setLastName);
    setAttributeIfPresent(attributes, TITLE, userDataBean::setTitle);
    setAttributeIfPresent(attributes, TELEPHONE_NUMBER, userDataBean::setPhone);
    setAttributeIfPresent(
      attributes,
      DEPARTMENT_NUMBER,
      userDataBean::setOrgdepnumber
    );
    setAttributeIfPresent(
      attributes,
      FACSIMILE_TELEPHONE_NUMBER,
      userDataBean::setFax
    );
    setAttributeIfPresent(
      attributes,
      DESCRIPTION,
      userDataBean::setDescription
    );
    setAttributeIfPresent(
      attributes,
      PHYSICAL_DELIVERY_OFFICE_NAME,
      userDataBean::setPostalAddress
    );
    setAttributeIfPresent(attributes, O, userDataBean::setDomain);
    setAttributeIfPresent(
      attributes,
      EC_MONIKER,
      userDataBean::setEcasUserName
    );
    if (attributes.get(MODIFICATION_DATE) != null) {
      String modificationDateSting = (String) attributes
        .get(MODIFICATION_DATE)
        .get();
      setModificationDate(
        userDataBean,
        modificationDateSting,
        new SimpleDateFormat("yyyyMMddHHmmss'Z'")
      );
    }
    setAttributeIfPresent(
      attributes,
      SOURCE_ORGANISATION,
      userDataBean::setSourceOrganisation
    );
    setAttributeIfPresent(attributes, DG, userDataBean::setDg);
  }

  private void setAttributeIfPresent(
    Attributes attributes,
    String attrName,
    java.util.function.Consumer<String> setter
  ) throws NamingException {
    if (attributes.get(attrName) != null) {
      setter.accept((String) attributes.get(attrName).get());
    }
  }

  private static class CloseableDirContext implements AutoCloseable {

    private final DirContext context;

    CloseableDirContext(DirContext context) {
      this.context = context;
    }

    DirContext getContext() {
      return context;
    }

    @Override
    public void close() {
      try {
        if (context != null) context.close();
      } catch (NamingException e) {
        /* ignore */
      }
    }
  }

  private void setModificationDate(
    CircabcUserDataBean userDataBean,
    String modificationDateSting,
    SimpleDateFormat formatter
  ) {
    try {
      final Date modificationDate = formatter.parse(modificationDateSting);
      userDataBean.setLastModificationDetailsTime(modificationDate);
    } catch (final ParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error parsing modificationDate", e);
      }
    }
  }

  private static String encodeForLDAP(String input) {
    return ESAPI.encoder().encodeForLDAP(input);
  }

  private String getUidFilter(final String search) {
    if (search != null && !search.isEmpty()) {
      return "(uid=" + search + ")";
    } else {
      return "(uid=*)";
    }
  }

  private String getDomainFilter(final String search) {
    if (isValidSearch(search)) {
      return buildSingleDomainFilter(search);
    }
    return buildAllDomainsFilter();
  }

  private boolean isValidSearch(String search) {
    return (
      search != null &&
      !search.isEmpty() &&
      !"allusers".equalsIgnoreCase(search)
    );
  }

  private String buildSingleDomainFilter(String search) {
    String sourceOrganisationFilter = getSourceOrganisationFilter(search);
    if (sourceOrganisationFilter.isEmpty()) {
      return "(o=" + search + ")";
    }
    return "(& " + sourceOrganisationFilter + "  (o=" + search + "))";
  }

  private String buildAllDomainsFilter() {
    Set<String> allEcasDomains = ldapEcasDomainService.getAllEcasDomains();
    if (allEcasDomains == null || allEcasDomains.isEmpty()) {
      return "";
    }

    StringBuilder result = new StringBuilder("(|");
    for (String domain : allEcasDomains) {
      result.append(buildSingleDomainFilter(domain));
    }
    result.append(")");
    return result.toString();
  }

  private String getSourceOrganisationFilter(String domain) {
    StringBuilder sb = new StringBuilder();
    if (domain.equalsIgnoreCase("eu.europa.ec")) {
      sb
        .append("(|")
        .append("(sourceOrganisation=COM)")
        .append("(sourceOrganisation=EXT)") // external users
        .append(")");
    }
    if (domain.equalsIgnoreCase("external")) {
      sb
        .append("(|")
        .append("(sourceOrganisation=AAP)")
        .append("(sourceOrganisation=AWS)")
        .append(")");
    }
    return sb.toString();
  }

  private String getMailFilter(final String search) {
    return getFilter("mail", search);
  }

  private String getFilter(final String key, final String search) {
    String result;
    if (search != null && !search.isEmpty()) {
      if (search.indexOf('*') != -1) {
        // mail contains the wildcard *
        result = "(" + key + "=" + search + ")";
      } else {
        result = "(" + key + "=*" + search + "*)";
      }
    } else {
      result = "(" + key + "=*)";
    }

    return result;
  }

  private String getRecordStatusFilter() {
    return (
      "(|" +
      "(recordStatus=a)" +
      "(recordStatus=i)" +
      "(recordStatus=b)" +
      "(recordStatus=q)" +
      ")"
    );
  }

  private String getEmployeTypeFilter() {
    return (
      "(|" +
      "(employeeType=c)" +
      "(employeeType=f)" +
      "(employeeType=x)" +
      "(employeeType=n)" +
      "(employeeType=i)" +
      "(employeeType=e)" +
      "(employeeType=z)" +
      "(employeeType=v)" +
      ")"
    );
  }

  /**
   * Looks up a single user in LDAP by their uid without applying the EU Login
   * (record status / employee type / domain) filters.
   *
   * @param userID the LDAP uid of the user to look up
   * @return the populated {@link CircabcUserDataBean} for the user, or
   *         {@code null} if no matching entry is found
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
    return getLDAPUserDataByUid(userID, false);
  }

  /**
   * Searches LDAP for user identifiers matching the supplied uid, EC moniker,
   * email and common name, restricted to active EU Login users.
   *
   * @param uid         uid search term (escaped before use)
   * @param moniker     EC moniker search term (escaped before use)
   * @param email       email search term (escaped before use)
   * @param cn          common name search term (escaped before use)
   * @param conjunction when {@code true} the uid/moniker are combined with a
   *                    logical OR while email and cn are ANDed to them;
   *                    when {@code false} all four criteria are combined with a
   *                    single logical OR
   * @return the list of matching user uids (possibly empty); when the count
   *         limit is exceeded but some results were collected, the partial list
   *         is returned
   * @throws LdapLimitExceededException if the directory result count limit is
   *         exceeded and no results could be collected
   * @throws LdapAccessException if the LDAP query fails for any other reason
   */
  @Override
  public List<String> getLDAPUserIDByIdMonikerEmailCn(
    final String uid,
    final String moniker,
    final String email,
    final String cn,
    boolean conjunction
  ) {
    final String ldapSearchString = buildLdapQuery(
      uid,
      email,
      cn,
      moniker,
      conjunction
    );
    final String[] returningAttrs = { UID };
    final List<String> userIDs = new ArrayList<>();

    DirContext ctx = null;
    NamingEnumeration<?> results = null;

    try {
      ctx = new InitialDirContext(env);
      final SearchControls controls = createSearchControls(returningAttrs);
      results = ctx.search("", ldapSearchString, controls);

      while (results.hasMore()) {
        SearchResult searchResult = (SearchResult) results.next();
        Attributes attributes = searchResult.getAttributes();
        if (attributes.get(UID) != null) {
          userIDs.add((String) attributes.get(UID).get());
        }
      }
    } catch (final LimitExceededException e) {
      handleLimitExceededException(e, userIDs, returningAttrs);
    } catch (final NamingException e) {
      logAndThrowNamingException(e, returningAttrs);
    } finally {
      closeQuietly(results);
      closeQuietly(ctx);
    }
    return userIDs;
  }

  private String buildLdapQuery(
    String uid,
    String email,
    String cn,
    String moniker,
    boolean conjunction
  ) {
    StringBuilder ldapQuery = new StringBuilder("(&");
    if (conjunction) {
      ldapQuery
        .append("(|")
        .append(getUidFilter(encodeForLDAP(uid)))
        .append(getEcMonikerFilter(encodeForLDAP(moniker)))
        .append(")")
        .append(getMailFilter(encodeForLDAP(email)))
        .append(getCommonNameFilter(encodeForLDAP(cn)));
    } else {
      ldapQuery
        .append("(|")
        .append(getUidFilter(uid))
        .append(getMailFilter(email))
        .append(getCommonNameFilter(cn))
        .append(getEcMonikerFilter(moniker))
        .append(")");
    }
    ldapQuery
      .append(getRecordStatusFilter())
      .append(getEmployeTypeFilter())
      .append(getDomainFilter(""))
      .append(")");
    return ldapQuery.toString();
  }

  private SearchControls createSearchControls(String[] returningAttrs) {
    SearchControls controls = new SearchControls();
    controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
    controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);
    controls.setReturningAttributes(returningAttrs);
    return controls;
  }

  private void handleLimitExceededException(
    LimitExceededException e,
    List<String> userIDs,
    String[] returningAttrs
  ) {
    String logMessage = buildLogMessage(returningAttrs);
    if (!userIDs.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          ERROR_LIMIT_EXCEEDED +
            userIDs.size() +
            ") accessing Ldap with query:" +
            logMessage,
          e
        );
      }
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(ERROR_LIMIT_EXCEEDED + logMessage, e);
      }
      throw new LdapLimitExceededException(
        "Error accessing LDAP (Too many results)",
        e
      );
    }
  }

  private void logAndThrowNamingException(
    NamingException e,
    String[] returningAttrs
  ) {
    if (logger.isErrorEnabled()) {
      logger.error(ERROR_ACCESSING_LDAP + buildLogMessage(returningAttrs), e);
    }
    throw new LdapAccessException("Error accessing LDAP ", e);
  }

  private String buildLogMessage(String[] returningAttrs) {
    return (
      "[LDAP_QUERY] " +
      WITH_RETURN_ATTRIBUTES +
      Arrays.toString(returningAttrs) +
      WITH_COUNT_LIMIT +
      LDAP_SEARCH_COUNT_LIMIT +
      WITH_SCOPE_LIMIT +
      LDAP_SEARCH_SCOPE_LEVEL
    );
  }

  private void closeQuietly(NamingEnumeration<?> results) {
    if (results != null) {
      try {
        results.close();
      } catch (Exception e) {
        /* ignore */
      }
    }
  }

  private void closeQuietly(DirContext ctx) {
    if (ctx != null) {
      try {
        ctx.close();
      } catch (Exception e) {
        /* ignore */
      }
    }
  }

  /**
   * Searches LDAP for users within a domain whose given name, surname, uid or
   * EC moniker match the supplied criteria, returning lightweight search records.
   *
   * <p>When the criteria contain two or more words of at least three characters,
   * the search is performed against the common name in both word orders;
   * otherwise each single term is matched independently.</p>
   *
   * @param domain   the domain (organisation) to restrict the search to; an
   *                 empty value or {@code "allusers"} disables domain filtering
   * @param criteria free-text search criteria (escaped before use)
   * @param filter   when {@code true} the EU Login record-status, employee-type
   *                 and domain filters are applied
   * @return the matching {@link SearchResultRecord}s, sorted using
   *         {@link SearchResultRecordComparator}
   * @throws LdapLimitExceededException if the directory result count limit is
   *         exceeded and no results could be collected
   * @throws LdapAccessException if the LDAP query fails for any other reason
   */
  @Override
  public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
    final String domain,
    final String criteria,
    boolean filter
  ) {
    final List<SearchResultRecord> users;
    final String[] returningAttrs = { UID, GIVEN_NAME, SN, MAIL, EC_MONIKER };
    final String ldapSearchString = getLdapSearchString(
      encodeForLDAP(domain),
      encodeForLDAP(criteria),
      filter
    );
    users = getUsersWithMail(returningAttrs, ldapSearchString);
    return users;
  }

  private String getLdapSearchString(
    final String domain,
    final String contains,
    boolean filter
  ) {
    String result = "";

    final StringTokenizer tokenizer = new StringTokenizer(
      contains,
      BLANK_DELIM
    );
    String token;
    String firstWord = "";
    String secondWord = "";
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (token.length() >= 3) {
        if (firstWord.equals("")) {
          firstWord = token;
        } else if (secondWord.equals("")) {
          secondWord = token;
        }
      }
    }

    final StringBuilder sb = new StringBuilder();
    if (secondWord.equals("")) {
      sb
        .append("(&")
        .append("(|")
        .append(getGivenNameFilter(contains))
        .append(getSurnameFilter(contains))
        .append(getUidFilter(contains))
        .append(getEcMonikerFilter(contains))
        .append(")")
        .append(getDomainFilter(domain, filter))
        .append(getMailFilter("*@*"))
        .append(getRecordStatusFilter(filter))
        .append(getEmployeTypeFilter(filter))
        .append(")");
    } else {
      sb
        .append("(&")
        .append("(|")
        .append(getCommonNameFilter("*" + firstWord + "* " + secondWord + "*"))
        .append(getCommonNameFilter("*" + secondWord + "* " + firstWord + "*"))
        .append(")")
        .append(getDomainFilter(domain, filter))
        .append(getMailFilter("*@*"))
        .append(getRecordStatusFilter(filter))
        .append(getEmployeTypeFilter(filter))
        .append(")");
    }

    result = sb.toString();

    return result;
  }

  private String getSurnameFilter(final String search) {
    return getFilter("sn", search);
  }

  private String getGivenNameFilter(final String search) {
    return getFilter(GIVEN_NAME, search);
  }

  private String getCommonNameFilter(final String search) {
    return getFilter("cn", search);
  }

  private String getEcMonikerFilter(final String search) {
    // AMO DIGIT-5088 do not use * for ecMoniker.
    // It does not seem to be supported any more in new EC OpenLdap
    String result;
    if (search != null && !search.isEmpty()) {
      result = "(ecMoniker=" + search + ")";
    } else {
      result = "(ecMoniker=*)";
    }
    return result;
  }

  /**
   * Searches LDAP for users matching the given mail address within a domain,
   * returning lightweight search records.
   *
   * @param mail   the mail search term (escaped before use)
   * @param domain the domain (organisation) to restrict the search to; an empty
   *               value or {@code "allusers"} disables domain filtering
   * @param filter when {@code true} the EU Login record-status, employee-type
   *               and domain filters are applied
   * @return the matching {@link SearchResultRecord}s, sorted using
   *         {@link SearchResultRecordComparator}
   * @throws LdapLimitExceededException if the directory result count limit is
   *         exceeded and no results could be collected
   * @throws LdapAccessException if the LDAP query fails for any other reason
   */
  @Override
  public List<SearchResultRecord> getUsersByMailDomain(
    final String mail,
    final String domain,
    boolean filter
  ) {
    final List<SearchResultRecord> users;
    final String[] returningAttrs = { UID, GIVEN_NAME, SN, MAIL, EC_MONIKER };
    final String ldapUserByMailSearchString =
      "(&" +
      getMailFilter(encodeForLDAP(mail)) +
      getRecordStatusFilter(filter) +
      getEmployeTypeFilter(filter) +
      getDomainFilter(encodeForLDAP(domain), filter) +
      ")";
    users = getUsersWithMail(returningAttrs, ldapUserByMailSearchString);
    return users;
  }

  private List<SearchResultRecord> getUsersWithMail(
    final String[] returningAttrs,
    final String ldapSearchString
  ) {
    final List<SearchResultRecord> users = new ArrayList<>();
    LdapContext ctx = null;
    NamingEnumeration<?> searchResults = null;
    try {
      ctx = new InitialLdapContext(env, null);
      final SearchControls controls = createSearchControls(returningAttrs);
      searchResults = ctx.search("", ldapSearchString, controls);

      while (searchResults.hasMore()) {
        SearchResultRecord searchRecord = parseSearchResult(
          (SearchResult) searchResults.next()
        );
        if (searchRecord != null) {
          users.add(searchRecord);
        }
      }
    } catch (final LimitExceededException e) {
      handleLimitExceededForUsers(e, users, returningAttrs);
    } catch (final NamingException e) {
      logAndThrowNamingException(e, returningAttrs);
    } finally {
      closeQuietly(searchResults);
      closeQuietly(ctx);
    }

    Collections.sort(users, SearchResultRecordComparator.getInstance());
    return users;
  }

  private SearchResultRecord parseSearchResult(SearchResult searchResult)
    throws NamingException {
    Attributes attributes = searchResult.getAttributes();
    if (
      attributes.get(UID) == null ||
      attributes.get(GIVEN_NAME) == null ||
      attributes.get(SN) == null
    ) {
      return null;
    }

    String uid = (String) attributes.get(UID).get();
    String firstName = (String) attributes.get(GIVEN_NAME).get();
    String lastName = (String) attributes.get(SN).get();
    String moniker =
      attributes.get(EC_MONIKER) != null
        ? (String) attributes.get(EC_MONIKER).get()
        : "";
    String email =
      attributes.get(MAIL) != null ? (String) attributes.get(MAIL).get() : "";

    return new SearchResultRecord(uid, moniker, firstName, lastName, email);
  }

  private void handleLimitExceededForUsers(
    LimitExceededException e,
    List<SearchResultRecord> users,
    String[] returningAttrs
  ) {
    String logMessage = buildLogMessage(returningAttrs);
    if (!users.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          ERROR_LIMIT_EXCEEDED +
            users.size() +
            ") accessing Ldap with query:" +
            logMessage,
          e
        );
      }
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(ERROR_LIMIT_EXCEEDED + logMessage, e);
      }
      throw new LdapLimitExceededException(
        "Error accessing LDAP (Too many results)",
        e
      );
    }
  }

  private void closeQuietly(LdapContext ctx) {
    if (ctx != null) {
      try {
        ctx.close();
      } catch (Exception e) {
        /* ignore */
      }
    }
  }

  private String getDomainFilter(final String search, boolean filter) {
    if (filter) {
      return getDomainFilter(search);
    } else {
      StringBuilder result = new StringBuilder("");
      if (
        search != null &&
        !search.isEmpty() &&
        (!"allusers".equalsIgnoreCase(search))
      ) {
        result.append(" (o=").append(search).append(")");
      }
      return result.toString();
    }
  }

  private String getRecordStatusFilter(boolean filter) {
    if (filter) {
      return getRecordStatusFilter();
    } else {
      return "";
    }
  }

  private String getEmployeTypeFilter(boolean filter) {
    if (filter) {
      return getEmployeTypeFilter();
    } else {
      return "";
    }
  }
}
