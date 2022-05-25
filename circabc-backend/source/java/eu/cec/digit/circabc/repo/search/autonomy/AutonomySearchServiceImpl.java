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
package eu.cec.digit.circabc.repo.search.autonomy;

import com.autonomy.aci.*;
import com.autonomy.aci.businessobjects.SecurityType;
import com.autonomy.aci.constants.IDOLConstants;
import com.autonomy.aci.exceptions.AciException;
import com.autonomy.encryption.BTEAEncryptionDetails;
import eu.cec.digit.circabc.service.search.autonomy.AutonomyResultNode;
import eu.cec.digit.circabc.service.search.autonomy.AutonomyResults;
import eu.cec.digit.circabc.service.search.autonomy.AutonomySearchService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Autonomy Search Service implementation class.
 *
 * @author schwerr
 */
public class AutonomySearchServiceImpl implements AutonomySearchService {

    private static final Log logger = LogFactory.getLog(AutonomySearchServiceImpl.class);
    protected AciConnection connection = null;
    protected AuthenticationService authenticationService = null;
    protected PermissionService permissionService = null;
    protected AuthorityService authorityService = null;
    private boolean enabled = false;
    private String host = "localhost";
    private int port = 9000;
    private int timeout = 10000;
    private int retries = 2;
    private boolean https = false;
    private boolean autonomySecurityEnabled = false;
    private String charEncoding = "UTF-8"; // UTF-8 is the default
    private int summaryLength = 200;
    private String autonomyDBaseName = "CIRCABC";
    private long[] encryptionKeys = {123, 456, 789, 123};
    private long[] securityKeys = {123, 144, 564, 231};
    private int maxResults = 1000;

    public void init() {

        if (!enabled) {
            logger.info("Autonomy search disabled.");
            return;
        }

        // Create connection details for this host and port
        final AciConnectionDetails connectionDetails = new AciConnectionDetails();
        connectionDetails.setHost(host);
        connectionDetails.setPort(port);

        // Set the timeout and number of times the API retries the action if
        // it fails
        connectionDetails.setTimeout(timeout); // 10 second timeout
        connectionDetails.setRetries(retries); // Retry the action twice

        // You can change the character encoding that is used to convert
        // between bytes and characters to ensure that the response is
        // displayed correctly
        connectionDetails.setCharacterEncoding(charEncoding);

        // If IDOL Server is SSL protected, you can change the protocol that
        // is used from HTTP to HTTPS. Ensure the correct certificates are set
        // in the Java keystore so that the API can communicate with IDOL.
        // Refer to the keytool documentation for your JDK version and platform
        // JDK 1.4.2 Windows -
        // http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html
        // JDK 1.4.2 Solaris & Linux -
        // http://java.sun.com/j2se/1.4.2/docs/tooldocs/solaris/keytool.html
        if (https) {
            connectionDetails.setProtocol(AciConnectionDetails.PROTOCOL_HTTPS);
        } else {
            connectionDetails.setProtocol(AciConnectionDetails.PROTOCOL_HTTP);
        }

        // connectionDetails.setProtocol(AciConnectionDetails.PROTOCOL_HTTPS);
        // Create the connection object to use to execute the action
        connection = new AciConnection(connectionDetails);

        // If IDOL Server is protected by BTEA encryption, you can create an
        // encryption codec to handle the encrytpion/decryption of the ACI
        // request/response
        if (autonomySecurityEnabled) {

            final BTEAEncryptionDetails encryptionDetails =
                    new BTEAEncryptionDetails(
                            encryptionKeys[0],
                            encryptionKeys[1],
                            encryptionKeys[2],
                            encryptionKeys[3],
                            charEncoding);
            // You must set this to true for the action to be encypted
            encryptionDetails.setEncrypting(true);
            connection.setEncryptionDetails(encryptionDetails);

            // You can set an encryption codec to generate user security info
            // strings. This one uses the default user security info string
            // keys found in your IDOL Server configuration file
            connection.setSecurityInfoEncryptionDetails(
                    new BTEAEncryptionDetails(
                            securityKeys[0], securityKeys[1], securityKeys[2], securityKeys[3], charEncoding));
            // You do not have to call setEncrypting(true) on the encryption
            // codec you set for security info string generation
        }

        logger.info("ACI Connection initialized.");
    }

    /**
     * @see eu.cec.digit.circabc.service.search.autonomy.AutonomySearchService#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled
     *
     * @param enabled the enabled to set.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see eu.cec.digit.circabc.service.search.autonomy.AutonomySearchService#search(java.lang.String)
     */
    @Override
    public AutonomyResults search(String text) {

        if (!enabled) {
            logger.info("Autonomy search disabled.");
            return new AutonomyResults("Autonomy search disabled.");
        }

        // Create the action to run
        final AciAction action = new AciAction(IDOLConstants.QUERY_ACTION);
        action.setParameter(new ActionParameter(IDOLConstants.TEXT_PARAM_NAME, text));
        // Only bring back maxResults results
        action.setParameter(
                new ActionParameter(IDOLConstants.QUERY_MAX_RESULTS_PARAM_NAME, maxResults));
        // Show the total number of results
        action.setParameter(new ActionParameter(IDOLConstants.QUERY_TOTALRESULTS_PARAM_NAME, true));
        // Only show a summary of results, with no fields
        action.setParameter(new ActionParameter(IDOLConstants.QUERY_PRINT_PARAM_NAME, "none"));
        // See the help for the different types of summary
        action.setParameter(
                new ActionParameter(IDOLConstants.QUERY_SUMMARY_PARAM_NAME, "ParagraphConcept"));
        // Use a summary that is three sentences long
        action.setParameter(new ActionParameter(IDOLConstants.QUERY_SENTENCES_PARAM_NAME, 3));
        // Bring back results in any language
        action.setParameter(new ActionParameter(IDOLConstants.QUERY_ANY_LANGUAGE_PARAM_NAME, true));
        // Database to search into
        action.setParameter(new ActionParameter(AGENT_DATABASES_FIELD_NAME, autonomyDBaseName));

        if (autonomySecurityEnabled) {
            // To use the security info string to limit what a user can see,
            // you can send a UserRead action to IDOL and use the security info
            // string that is provided in the response. For example, there
            // could be a getSecurityInfoForUser(user) method in your
            // application which does this
            //			action.setParameter(new ActionParameter(
            //					IDOLConstants.QUERY_SECURITY_INFO_PARAM_NAME,
            //						getSecurityInfoForUser(
            //							authenticationService.getCurrentUserName())));
            // Alternatively, you can add SecurityType objects to the action
            // and have the action build a security info string just before
            // it is executed, using the encryption codec that has been set
            // on the connection
            final SecurityType securityType = new SecurityType("Alfresco");
            String userName = authenticationService.getCurrentUserName();
            securityType.setSecurityFieldValue("Username", userName);
            securityType.setSecurityFieldValue("Groups", getGroups(userName));
            action.addUserSecurityInfo(securityType);
        }

        // Execute the action and check for an ACI error response
        AciResponse response = null;

        try {
            response = connection.aciActionExecute(action);
            AciResponseChecker.check(response);
        } catch (AciException e) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            logger.error("Error when executing Autonomy search for text: " + text, e);

            return new AutonomyResults(
                    "On " + simpleDateFormat.format(new Date()) + " - " + e.getMessage());
        }

        List<AutonomyResultNode> nodes = new ArrayList<>();

        // Iterate over the returned hits
        AciResponse hit = response.findFirstOccurrence("autn:hit");

        while (hit != null) {

            // Output a summary of the hit
            AutonomyResultNode node = new AutonomyResultNode();

            String link = hit.getTagValue("autn:reference");

            NodeRef nodeRef = null;

            int linkLastIndex = 0;
            int nodeUUIDLastIndex = 0;

            String name = link;
            String builtLink = "";

            if (!autonomySecurityEnabled) {

                linkLastIndex = link.lastIndexOf('/');
                if (linkLastIndex == -1) {
                    linkLastIndex = link.lastIndexOf('\\');
                }

                if (linkLastIndex != -1) {

                    name = link.substring(linkLastIndex + 1, link.length());
                    builtLink = link.substring(0, linkLastIndex);

                    String nodeUUID = link.substring(0, linkLastIndex);

                    nodeUUIDLastIndex = nodeUUID.lastIndexOf('/');
                    if (nodeUUIDLastIndex == -1) {
                        nodeUUIDLastIndex = nodeUUID.lastIndexOf('\\');
                    }

                    if (nodeUUIDLastIndex != -1 && nodeUUIDLastIndex < nodeUUID.length()) {

                        nodeUUID = nodeUUID.substring(nodeUUIDLastIndex + 1, nodeUUID.length());

                        nodeRef = new NodeRef("workspace://SpacesStore/" + nodeUUID);
                    }
                }
            }

            if (autonomySecurityEnabled
                    || (nodeRef != null
                    && permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED)) {

                String summary = hit.getTagValue("autn:summary");

                node.setName(name);
                node.setTitle(hit.getTagValue("autn:title"));
                node.setLink(builtLink);
                node.setDescription(
                        summary.length() > summaryLength
                                ? summary.substring(0, summaryLength - 1) + "..."
                                : summary);
                nodes.add(node);
            }

            // Go to the next hit
            hit = hit.next("autn:hit");
        }

        return new AutonomyResults(
                nodes,
                response.getTagValue("autn:numhits"),
                Integer.toString(nodes.size()),
                Integer.toString(maxResults));
    }

    /**
     * Returns the groups that the given user belongs to as a comma separated String (needed for the
     * Groups security parameter)
     *
     * <p>Add cache (user, groups)?
     */
    protected String getGroups(String userName) {

        String groups = "";

        final Set<String> containingAuthoritiesForUser =
                authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, false);

        int size = containingAuthoritiesForUser.size();
        for (String groupName : containingAuthoritiesForUser) {
            groups += groupName.substring("GROUP_".length(), groupName.length());
            if (--size > 0) {
                groups += ",";
            }
        }

        return groups;
    }

    /**
     * Sets the value of the host
     *
     * @param host the host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the value of the port
     *
     * @param port the port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the value of the timeout
     *
     * @param timeout the timeout to set.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the value of the retries
     *
     * @param retries the retries to set.
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Sets the value of the https
     *
     * @param https the https to set.
     */
    public void setHttps(boolean https) {
        this.https = https;
    }

    /**
     * Sets the value of the autonomySecurityEnabled
     *
     * @param autonomySecurityEnabled the autonomySecurityEnabled to set.
     */
    public void setAutonomySecurityEnabled(boolean autonomySecurityEnabled) {
        this.autonomySecurityEnabled = autonomySecurityEnabled;
    }

    /**
     * Sets the value of the maxResults
     *
     * @param maxResults the maxResults to set.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Sets the value of the charEncoding
     *
     * @param charEncoding the charEncoding to set.
     */
    public void setCharEncoding(String charEncoding) {
        this.charEncoding = charEncoding;
    }

    /**
     * Sets the value of the encryptionKeys
     *
     * @param encryptionKeys the encryptionKeys to set.
     */
    public void setEncryptionKeys(long[] encryptionKeys) {
        this.encryptionKeys = encryptionKeys;
    }

    /**
     * Sets the value of the securityKeys
     *
     * @param securityKeys the securityKeys to set.
     */
    public void setSecurityKeys(long[] securityKeys) {
        this.securityKeys = securityKeys;
    }

    /**
     * Sets the value of the authenticationService
     *
     * @param authenticationService the authenticationService to set.
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets the value of the permissionService
     *
     * @param permissionService the permissionService to set.
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Sets the value of the authorityService
     *
     * @param authorityService the authorityService to set.
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * Sets the value of the summaryLength
     *
     * @param summaryLength the summaryLength to set.
     */
    public void setSummaryLength(int summaryLength) {
        this.summaryLength = summaryLength;
    }

    /**
     * Sets the value of the autonomyDBaseName
     *
     * @param autonomyDBaseName the autonomyDBaseName to set.
     */
    public void setAutonomyDBaseName(String autonomyDBaseName) {
        this.autonomyDBaseName = autonomyDBaseName;
    }
}
