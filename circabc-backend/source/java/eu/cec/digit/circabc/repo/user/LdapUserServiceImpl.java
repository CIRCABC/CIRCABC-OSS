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
package eu.cec.digit.circabc.repo.user;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.user.LdapEcasDomainService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.service.user.SearchResultRecordComparator;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;

import javax.naming.Context;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LdapUserServiceImpl implements LdapUserService {

    private static final String BLANK_DELIM = " ";
    private static final String CN = "cn";
    private static final int LDAP_SEARCH_COUNT_LIMIT = 2000;
    private static final int LDAP_SEARCH_SCOPE_LEVEL =
            SearchControls.ONELEVEL_SCOPE; // SearchControls.SUBTREE_SCOPE
    private static final String UID = "uid";
    private static final String MODIFICATION_DATE = "modificationDate";
    private static final String EC_MONIKER = "ecMoniker";
    private static final String O = "o";
    private static final String PHYSICAL_DELIVERY_OFFICE_NAME = "physicalDeliveryOfficeName";
    private static final String DESCRIPTION = "description";
    private static final String FACSIMILE_TELEPHONE_NUMBER = "facsimileTelephoneNumber";
    private static final String DEPARTMENT_NUMBER = "departmentNumber";
    private static final String TELEPHONE_NUMBER = "ecTelephoneNumber";
    private static final String TITLE = "title";
    private static final String SN = "sn";
    private static final String GIVEN_NAME = "givenName";
    private static final String MAIL = "mail";
    private static final String SOURCE_ORGANISATION = "sourceOrganisation";
    private static final String DG = "dg";
    private static final String RECORDSTATUS = "recordStatus";

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(LdapUserServiceImpl.class);
    private static final String PROPERTY = "Property:";
    private static final String IS_NOT_DEFINED_IN_FILE = " is not defined in file:";
    private Hashtable<String, String> env;
    /**
     * profileManagerServiceFactory reference
     */
    private transient ProfileManagerServiceFactory profileManagerServiceFactory;

    private LdapEcasDomainService ldapEcasDomainService;

    private static String encodeForLDAP(String input) {
        return ESAPI.encoder().encodeForLDAP(input);
    }

    public void init() {
        env = new Hashtable<>();
        try {
            env.put(
                    Context.INITIAL_CONTEXT_FACTORY,
                    CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_INITIAL_CONTEXT_FACTORY));
        } catch (final NullPointerException npe) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        PROPERTY
                                + CircabcConfiguration.CONTEXT_INITIAL_CONTEXT_FACTORY
                                + IS_NOT_DEFINED_IN_FILE
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE);
            }
            throw npe;
        }

        try {
            env.put(
                    Context.PROVIDER_URL,
                    CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_PROVIDER_URL));
        } catch (final NullPointerException npe) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        PROPERTY
                                + CircabcConfiguration.CONTEXT_PROVIDER_URL
                                + IS_NOT_DEFINED_IN_FILE
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE);
            }
            throw npe;
        }
        try {
            env.put(
                    Context.SECURITY_AUTHENTICATION,
                    CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_AUTHENTICATION));
        } catch (final NullPointerException npe) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        PROPERTY
                                + CircabcConfiguration.CONTEXT_SECURITY_AUTHENTICATION
                                + IS_NOT_DEFINED_IN_FILE
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE);
            }
            throw npe;
        }
        try {
            env.put(
                    Context.SECURITY_PRINCIPAL,
                    CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_PRINCIPAL));
        } catch (final NullPointerException npe) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        PROPERTY
                                + CircabcConfiguration.CONTEXT_SECURITY_PRINCIPAL
                                + IS_NOT_DEFINED_IN_FILE
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE);
            }
            throw npe;
        }
        try {
            env.put(
                    Context.SECURITY_CREDENTIALS,
                    CircabcConfiguration.getProperty(CircabcConfiguration.CONTEXT_SECURITY_CREDENTIALS));
        } catch (final NullPointerException npe) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        PROPERTY
                                + CircabcConfiguration.CONTEXT_SECURITY_CREDENTIALS
                                + IS_NOT_DEFINED_IN_FILE
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE);
            }
            throw npe;
        }
        env.put("com.sun.jndi.ldap.connect.pool", "true");
    }

    private CircabcUserDataBean getLDAPUserDataByUid(final String pLdapUserID , boolean applyEULoginFilter) {
        CircabcUserDataBean userDataBean = null;

        // TODO put configuration in init and ldap properties in xml file

        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        final String ldapSearchString =applyEULoginFilter ?
                "(&"
                        + getUidFilter(encodeForLDAP(pLdapUserID))
                        + getMailFilter("*@*")
                        + getRecordStatusFilter()
                        + getEmployeTypeFilter()
                        + getDomainFilter("")
                        + ")"
                : 
                "(&"
                        + getUidFilter(encodeForLDAP(pLdapUserID))
                        + getMailFilter("*@*")
                + ")"
                ;
        final String[] returningAttrs = {
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
                DG
        };
        try {
            ctx = new InitialDirContext(env);
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
            controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);
            controls.setReturningAttributes(returningAttrs);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "LDAP search:"
                                + ldapSearchString
                                + " Return params:"
                                + Arrays.toString(returningAttrs));
            }

            results = ctx.search("", ldapSearchString, controls);
            while (results.hasMore()) {
                userDataBean = new CircabcUserDataBean();

                userDataBean.setUserName(pLdapUserID);

                final SearchResult searchResult = (SearchResult) results.next();
                final Attributes attributes = searchResult.getAttributes();

                if (logger.isDebugEnabled()) {
                    logger.debug(getReturnAttributesValues(attributes));
                }

                if (attributes.get(MAIL) != null) {
                    final String mail = (String) attributes.get(MAIL).get();
                    userDataBean.setEmail(mail);
                }
                if (attributes.get(GIVEN_NAME) != null) {
                    final String givenName = (String) attributes.get(GIVEN_NAME).get();
                    userDataBean.setFirstName(givenName);
                }
                if (attributes.get(SN) != null) {
                    final String sn = (String) attributes.get(SN).get();
                    userDataBean.setLastName(sn);
                }
                if (attributes.get(TITLE) != null) {
                    final String title = (String) attributes.get(TITLE).get();
                    userDataBean.setTitle(title);
                }
                if (attributes.get(TELEPHONE_NUMBER) != null) {
                    final String telephoneNumber = (String) attributes.get(TELEPHONE_NUMBER).get();
                    userDataBean.setPhone(telephoneNumber);
                }
                if (attributes.get(DEPARTMENT_NUMBER) != null) {
                    final String departmentNumber = (String) attributes.get(DEPARTMENT_NUMBER).get();
                    userDataBean.setOrgdepnumber(departmentNumber);
                }
                if (attributes.get(FACSIMILE_TELEPHONE_NUMBER) != null) {
                    final String facsimileTelephoneNumber =
                            (String) attributes.get(FACSIMILE_TELEPHONE_NUMBER).get();
                    userDataBean.setFax(facsimileTelephoneNumber);
                }
                if (attributes.get(DESCRIPTION) != null) {
                    final String description = (String) attributes.get(DESCRIPTION).get();
                    userDataBean.setDescription(description);
                }

                if (attributes.get(PHYSICAL_DELIVERY_OFFICE_NAME) != null) {
                    final String postalAddress = (String) attributes.get(PHYSICAL_DELIVERY_OFFICE_NAME).get();
                    userDataBean.setPostalAddress(postalAddress);
                }
                if (attributes.get(O) != null) {
                    final String ecasDomain = (String) attributes.get(O).get();
                    userDataBean.setDomain(ecasDomain);
                }
                if (attributes.get(EC_MONIKER) != null) {
                    final String ecasUserName = (String) attributes.get(EC_MONIKER).get();
                    userDataBean.setEcasUserName(ecasUserName);
                }
                if (attributes.get(MODIFICATION_DATE) != null) {
                    final String modificationDateSting = (String) attributes.get(MODIFICATION_DATE).get();
                    final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");

                    try {
                        final Date modificationDate = formatter.parse(modificationDateSting);
                        userDataBean.setLastModificationDetailsTime(modificationDate);
                        if (logger.isDebugEnabled()) {
                            logger.debug(modificationDate.toString());
                        }
                    } catch (final ParseException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Error parsing modificationDate", e);
                        }
                    }
                }
                if (attributes.get(SOURCE_ORGANISATION) != null) {
                    final String sourceOrganisation = (String) attributes.get(SOURCE_ORGANISATION).get();
                    userDataBean.setSourceOrganisation(sourceOrganisation);
                }
                if (attributes.get(DG) != null) {
                    final String dg = (String) attributes.get(DG).get();
                    userDataBean.setDg(dg);
                }
            }
        } catch (final LimitExceededException e) {
            // don't ignore this... accessing by uid should never get a LimitExceededException
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error LimitExceededException accessing Ldap with query:"
                                + ldapSearchString
                                + " with return attributes:"
                                + Arrays.toString(returningAttrs)
                                + " with countLimit:"
                                + LDAP_SEARCH_COUNT_LIMIT
                                + " with scope Limit:"
                                + LDAP_SEARCH_SCOPE_LEVEL,
                        e);
            }
            // The userDataBean may content only partial informations
            userDataBean = null;
        } catch (final NamingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error accessing Ldap with query:"
                                + ldapSearchString
                                + " with return attributes:"
                                + Arrays.toString(returningAttrs)
                                + " with countLimit:"
                                + LDAP_SEARCH_COUNT_LIMIT
                                + " with scope Limit:"
                                + LDAP_SEARCH_SCOPE_LEVEL,
                        e);
            }
            userDataBean = null;
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (final Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (final Exception e) {
                }
            }
        }
        return userDataBean;
    }

    public List<String> getLDAPUserIDByMailDomain(final String mail, final String domain) {

        final List<String> userIDs = new ArrayList<>();
        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        // TODO create ALL domain constant

        final String ldapSearchString =
                "(&"
                        + getMailFilter(encodeForLDAP(mail))
                        + getDomainFilter(encodeForLDAP(domain))
                        + getRecordStatusFilter()
                        + getEmployeTypeFilter()
                        + getDomainFilter("")
                        + ")";
        final String[] returningAttrs = {UID, MAIL};
        try {
            ctx = new InitialDirContext(env);
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
            controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);
            controls.setReturningAttributes(returningAttrs);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "LDAP search:"
                                + ldapSearchString
                                + " Return params:"
                                + Arrays.toString(returningAttrs));
            }
            results = ctx.search("", ldapSearchString, controls);
            SearchResult searchResult;
            Attributes attributes;
            String uid;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                if (logger.isDebugEnabled()) {
                    logger.debug(getReturnAttributesValues(attributes));
                }
                if (attributes.get(UID) != null) {
                    uid = (String) attributes.get(UID).get();
                    userIDs.add(uid);
                }
            }
        } catch (final LimitExceededException e) {
            // ignore this
            if (userIDs.size() != 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Error LimitExceededException ("
                                    + userIDs.size()
                                    + ") accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Error LimitExceededException accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
                throw new LdapLimitExceededException("Error accessing LDAP (Too many results)", e);
            }
        } catch (final NamingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error accessing Ldap with query:"
                                + ldapSearchString
                                + " with return attributes:"
                                + Arrays.toString(returningAttrs)
                                + " with countLimit:"
                                + LDAP_SEARCH_COUNT_LIMIT
                                + " with scope Limit:"
                                + LDAP_SEARCH_SCOPE_LEVEL,
                        e);
            }
            throw new RuntimeException("Error accessing LDAP ", e);

        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
        return userIDs;
    }

    public List<String> getLDAPUserIDByIdMonikerEmailCn(
            final String uid,
            final String moniker,
            final String email,
            final String cn,
            boolean conjunction) {

        final List<String> userIDs = new ArrayList<>();
        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        final StringBuilder ldapQuery = new StringBuilder();
        if (conjunction) {
            ldapQuery
                    .append("(&")
                    //search if uid or ecMoniker equals given username
                    .append("(|")
                    .append(getUidFilter(encodeForLDAP(uid)))
                    .append(getEcMonikerFilter(encodeForLDAP(moniker)))
                    .append(")")
                    .append(getMailFilter(encodeForLDAP(email)))
                    .append(getCommonNameFilter(encodeForLDAP(cn)))
                    .append(getRecordStatusFilter())
                    .append(getEmployeTypeFilter())
                    .append(getDomainFilter(""))
                    .append(")");
        } else {
            ldapQuery
                    .append("(&")
                    .append("(|")
                    .append(getUidFilter(uid))
                    .append(getMailFilter(email))
                    .append(getCommonNameFilter(cn))
                    .append(getEcMonikerFilter(moniker))
                    .append(")")
                    .append(getRecordStatusFilter())
                    .append(getEmployeTypeFilter())
                    .append(getDomainFilter(""))
                    .append(")");
        }

        final String ldapSearchString = ldapQuery.toString();
        final String[] returningAttrs = {UID};
        try {
            ctx = new InitialDirContext(env);
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
            controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);
            controls.setReturningAttributes(returningAttrs);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "LDAP search:"
                                + ldapSearchString
                                + " Return params:"
                                + Arrays.toString(returningAttrs));
            }
            results = ctx.search("", ldapSearchString, controls);
            SearchResult searchResult;
            Attributes attributes;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                if (logger.isDebugEnabled()) {
                    logger.debug(getReturnAttributesValues(attributes));
                }
                if (attributes.get(UID) != null) {
                    userIDs.add((String) attributes.get(UID).get());
                }
            }
        } catch (final LimitExceededException e) {
            if (userIDs.size() != 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Error LimitExceededException ("
                                    + userIDs.size()
                                    + ") accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Error LimitExceededException accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
                throw new LdapLimitExceededException("Error accessing LDAP (Too many results)", e);
            }
        } catch (final NamingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error accessing Ldap with query:"
                                + ldapSearchString
                                + " with return attributes:"
                                + Arrays.toString(returningAttrs)
                                + " with countLimit:"
                                + LDAP_SEARCH_COUNT_LIMIT
                                + " with scope Limit:"
                                + LDAP_SEARCH_SCOPE_LEVEL,
                        e);
            }
            throw new RuntimeException("Error accessing LDAP ", e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
        return userIDs;
    }

    private String getLdapSearchString(final String domain, final String contains, boolean filter) {
        String result = "";

        final StringTokenizer tokenizer = new StringTokenizer(contains, BLANK_DELIM);
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
            sb.append("(&")
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
            sb.append("(&")
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

        if (logger.isDebugEnabled()) {
            logger.debug("Ldap query:" + result);
        }
        return result;
    }

    private String getFilter(final String key, final String search) {
        String result;
        if (search != null && search.length() != 0) {
            if (search.indexOf('*') != -1) {
                // mail contains the wildcard *
                result = "(" + key + "=" + search + ")";
            } else {
                result = "(" + key + "=*" + search + "*)";
            }
        } else {
            result = "(" + key + "=*)";
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Ldap filter:" + result);
        }
        return result;
    }

    private String getSurnameFilter(final String search) {
        return getFilter("sn", search);
    }

    private String getGivenNameFilter(final String search) {
        return getFilter("givenName", search);
    }

    private String getCommonNameFilter(final String search) {
        return getFilter("cn", search);
    }

    private String getUidFilter(final String search) {
        if (search != null && search.length() != 0) {
            return "(uid=" + search + ")";
        } else {
            return "(uid=*)";
        }
    }

    private String getDomainFilter(final String search) {
        StringBuilder result = new StringBuilder("");
        if (search != null && search.length() != 0 && (!"allusers".equalsIgnoreCase(search))) {
            final String sourceOrganisationFilter = getSourceOrganisationFilter(search);
            if (sourceOrganisationFilter.equals("")) {
                result.append("(o=").append(search).append(")");
            } else {
                result
                        .append("(& ")
                        .append(sourceOrganisationFilter)
                        .append("  (o=")
                        .append(search)
                        .append("))");
            }

        } else {

            final Set<String> allEcasDomains = ldapEcasDomainService.getAllEcasDomains();

            if (allEcasDomains != null && allEcasDomains.size() > 0) {
                result.append("(|");
                for (String domain : allEcasDomains) {
                    final String sourceOrganisationFilter = getSourceOrganisationFilter(domain);
                    if (sourceOrganisationFilter.equals("")) {
                        result.append("(o=").append(domain).append(")");
                    } else {
                        result
                                .append("(& ")
                                .append(sourceOrganisationFilter)
                                .append(" (o=")
                                .append(domain)
                                .append("))");
                    }
                }
                result.append(")");
            }
        }
        return result.toString();
    }

    private String getDomainFilter(final String search, boolean filter) {
        if (filter) {
            return getDomainFilter(search);
        } else {
            StringBuilder result = new StringBuilder("");
            if (search != null && search.length() != 0 && (!"allusers".equalsIgnoreCase(search))) {
                result.append(" (o=")
                        .append(search)
                        .append(")");
            }
            return result.toString();
        }

    }


    private String getMailFilter(final String search) {
        return getFilter("mail", search);
    }

    private String getEcMonikerFilter(final String search) {
    	//AMO DIGIT-5088 do not use * for ecMoniker. 
    	//It does not seem to be supported any more in new EC OpenLdap
    	String result;
    	if (search != null && search.length() != 0) {
    		 result = "(ecMoniker=" + search + ")";
        } else {
        	result = "(ecMoniker=*)";
        }
        return result;
    }

    private String getRecordStatusFilter() {
        String sb = "(|" + "(recordStatus=a)" + "(recordStatus=i)" + "(recordStatus=b)" + "(recordStatus=q)" + ")";
        return sb;
    }

    private String getRecordStatusFilter(boolean filter) {
        if (filter) {
            return getRecordStatusFilter();
        } else {
            return "";
        }
    }

    private String getEmployeTypeFilter() {
        String sb =
                "(|"
                        + "(employeeType=c)"
                        + "(employeeType=f)"
                        + "(employeeType=x)"
                        + "(employeeType=n)"
                        + "(employeeType=i)"
                        + "(employeeType=e)"
                        + "(employeeType=z)"
                        + "(employeeType=v)"
                        + ")";
        return sb;
    }

    private String getEmployeTypeFilter(boolean filter) {
        if (filter) {
            return getEmployeTypeFilter();
        } else {
            return "";
        }

    }

    private String getSourceOrganisationFilter(String domain) {
        StringBuilder sb = new StringBuilder();
        if (domain.equalsIgnoreCase("eu.europa.ec")) {
            sb.append("(|")
                    .append("(sourceOrganisation=COM)")
                    .append("(sourceOrganisation=EXT)") // external users
                    .append(")");
        }
        if (domain.equalsIgnoreCase("external")) {
            sb.append("(|")
                    .append("(sourceOrganisation=AAP)")
                    .append("(sourceOrganisation=AWS)")
                    .append(")");
        }
        return sb.toString();
    }

    public List<SearchResultRecord> getUsersByMailDomain(final String mail, final String domain, boolean filter) {
        final List<SearchResultRecord> users;
        final String[] returningAttrs = {UID, GIVEN_NAME, SN, MAIL, EC_MONIKER};
        final String ldapUserByMailSearchString =
                "(&"
                        + getMailFilter(encodeForLDAP(mail))
                        + getRecordStatusFilter(filter)
                        + getEmployeTypeFilter(filter)
                        + getDomainFilter(encodeForLDAP(domain), filter)
                        + ")";
        users = getUsersWithMail(returningAttrs, ldapUserByMailSearchString);
        return users;
    }

    @Override
    public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
            final String domain, final String criteria, boolean filter) {
        final List<SearchResultRecord> users;
        final String[] returningAttrs = {UID, GIVEN_NAME, SN, MAIL, EC_MONIKER};
        final String ldapSearchString =
                getLdapSearchString(encodeForLDAP(domain), encodeForLDAP(criteria), filter);
        users = getUsersWithMail(returningAttrs, ldapSearchString);
        return users;
    }

    private List<SearchResultRecord> getUsersWithMail(
            final String[] returningAttrs, final String ldapSearchString) {
        final List<SearchResultRecord> users = new ArrayList<>();
        LdapContext ctx = null;
        NamingEnumeration<?> results = null;
        try {

            ctx = new InitialLdapContext(env, null);

            final SearchControls controls = new SearchControls();
            // controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
            controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);

            controls.setReturningAttributes(returningAttrs);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "LDAP search:"
                                + ldapSearchString
                                + " Return params:"
                                + Arrays.toString(returningAttrs));
            }
            results = ctx.search("", ldapSearchString, controls);
            SearchResult searchResult;
            Attributes attributes;
            String uid;
            String firstName;
            String lastName;
            String email;
            String moniker;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();

                if (logger.isDebugEnabled()) {
                    logger.debug(getReturnAttributesValues(attributes));
                }

                if (!(attributes.get(UID) == null
                        || attributes.get(GIVEN_NAME) == null
                        || attributes.get(SN) == null)) {
                    uid = (String) attributes.get(UID).get();
                    firstName = (String) attributes.get(GIVEN_NAME).get();
                    lastName = (String) attributes.get(SN).get();

                    if (attributes.get(EC_MONIKER) != null) {
                        moniker = (String) attributes.get(EC_MONIKER).get();
                    } else {
                        moniker = "";
                    }
                    if (attributes.get(MAIL) != null) {
                        email = (String) attributes.get(MAIL).get();
                    } else {
                        email = "";
                    }
                    users.add(new SearchResultRecord(uid, moniker, firstName, lastName, email));
                }
            }
        } catch (final LimitExceededException e) {
            if (users.size() != 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Error LimitExceededException ("
                                    + users.size()
                                    + ") accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Error LimitExceededException accessing Ldap with query:"
                                    + ldapSearchString
                                    + " with return attributes:"
                                    + Arrays.toString(returningAttrs)
                                    + " with countLimit:"
                                    + LDAP_SEARCH_COUNT_LIMIT
                                    + " with scope Limit:"
                                    + LDAP_SEARCH_SCOPE_LEVEL,
                            e);
                }
                throw new LdapLimitExceededException("Error accessing LDAP (Too many results)", e);
            }
        } catch (final NamingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error accessing Ldap with query:"
                                + ldapSearchString
                                + " with return attributes:"
                                + Arrays.toString(returningAttrs)
                                + " with countLimit:"
                                + LDAP_SEARCH_COUNT_LIMIT
                                + " with scope Limit:"
                                + LDAP_SEARCH_SCOPE_LEVEL,
                        e);
            }
            throw new RuntimeException("Error accessing LDAP ", e);

        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (final Exception e) {
                }
            }
        }

        // Sorting
        Collections.sort(users, SearchResultRecordComparator.getInstance());
        return users;
    }

    private List<String> getUsersWithoutMail(
            final DirContext ctx, final String[] returningAttrs, final String ldapSearchString)
            throws NamingException {
        final List<String> usersWithoutMail = new ArrayList<>();
        NamingEnumeration<?> results;
        final SearchControls controls = new SearchControls();

        controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
        controls.setCountLimit(LDAP_SEARCH_COUNT_LIMIT);
        controls.setReturningAttributes(returningAttrs);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "LDAP search:" + ldapSearchString + " Return params:" + Arrays.toString(returningAttrs));
        }
        results = ctx.search("", ldapSearchString, controls);
        SearchResult searchResult;
        Attributes attributes;
        String uid;
        String firstName;
        String lastName;
        while (results.hasMore()) {
            searchResult = (SearchResult) results.next();
            attributes = searchResult.getAttributes();

            if (logger.isDebugEnabled()) {
                logger.debug(getReturnAttributesValues(attributes));
            }

            if (!(attributes.get(UID) == null
                    || attributes.get(GIVEN_NAME) == null
                    || attributes.get(SN) == null)) {
                uid = (String) attributes.get(UID).get();
                firstName = (String) attributes.get(GIVEN_NAME).get();
                lastName = (String) attributes.get(SN).get();

                // Filter is now done on the LDAP query level
                final String user = firstName + BLANK_DELIM + lastName + " (" + uid + ")";
                usersWithoutMail.add(user);
            }
        }
        return usersWithoutMail;
    }

    public List<String> getLDAPUserIDByMail(final String mail) {
        // TODO create a all domain constant
        return getLDAPUserIDByMailDomain(mail, null);
    }

    private String getReturnAttributesValues(final Attributes attributes) {
        final StringBuilder sb = new StringBuilder();
        sb.append("LDAP search returns:\n");
        final NamingEnumeration<? extends Attribute> namingEnumaration = attributes.getAll();
        while (namingEnumaration.hasMoreElements()) {
            final Attribute attribute = namingEnumaration.nextElement();
            sb.append("ID:");
            sb.append(attribute.getID());
            sb.append(" VALUE:");
            try {
                sb.append((String) attribute.get());
            } catch (final NamingException e) {
                // TODO Auto-generated catch block
                if (logger.isErrorEnabled()) {
                    logger.error("Could not retrieve value", e);
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

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

    public LdapEcasDomainService getLdapEcasDomainService() {
        return ldapEcasDomainService;
    }

    public void setLdapEcasDomainService(LdapEcasDomainService ldapEcasDomainService) {
        this.ldapEcasDomainService = ldapEcasDomainService;
    }

    public Map<String, String> getEcasUserDomains() {
        return ldapEcasDomainService.getDefaultEcasDomains();
    }

    @Override
    public Boolean userExists(String uid) {
        Boolean userExists = false;
        uid = encodeForLDAP(uid);
        DirContext ctx = null;
        NamingEnumeration<?> results = null;
        final String ldapSearchString = "(uid=" + uid + ")";
        final String[] returningAttrs = {UID, RECORDSTATUS};

        try {
            ctx = new InitialDirContext(env);
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(LDAP_SEARCH_SCOPE_LEVEL);
            controls.setCountLimit(1);
            controls.setReturningAttributes(returningAttrs);
            results = ctx.search("", ldapSearchString, controls);
            SearchResult searchResult;
            Attributes attributes;
            if (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                if (!(attributes.get(UID) == null || attributes.get(RECORDSTATUS) == null)) {
                    uid = (String) attributes.get(UID).get();
                    String recordStatus = (String) attributes.get(RECORDSTATUS).get();
                    if (!recordStatus.equalsIgnoreCase("d")) {
                        userExists = true;
                    }
                }
            }
        } catch (NamingException e) {
            userExists = null;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error when try to close context", e);
                    }
                }
            }
        }
        return userExists;
    }

    @Override
    public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
        
        return getLDAPUserDataByUid(userID,false);
    }

    @Override
    public CircabcUserDataBean getLDAPUserDataByUid(String pLdapUserID) {
        
        return getLDAPUserDataByUid(pLdapUserID,true);
    }
}
