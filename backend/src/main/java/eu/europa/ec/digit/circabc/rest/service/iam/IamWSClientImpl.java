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
package eu.europa.ec.digit.circabc.rest.service.iam;

import eu.europa.ec.digit.circabc.rest.exception.IamServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Default implementation of {@link IamWSClient} that integrates CIRCABC with the European
 * Commission IAM (Identity and Access Management) research-theme SOAP web service.
 *
 * <p>This client builds and sends SOAP requests over HTTP (via Apache Commons {@code HttpClient})
 * to grant or revoke a role for a given user on a given research theme. Each request is secured
 * with a WS-Security {@code UsernameToken} whose password is transmitted as a SHA-1 password
 * digest computed from a per-request nonce, the creation timestamp and the configured service
 * password.
 *
 * <p>The service endpoint URL and the credentials (user/password) used to authenticate against the
 * IAM web service are injected from configuration through the {@code iam.location}, {@code
 * iam.user} and {@code iam.password} properties.
 */
@SuppressWarnings({ "java:S2479", "java:S6126" })
public class IamWSClientImpl implements IamWSClient {

  /** Upper bound (exclusive) used when generating the random nonce value. */
  private static final int MAX_NEXT_INT = 999999999;

  /** Socket timeout, in milliseconds, applied to the IAM web service HTTP call (10 seconds). */
  private static final int CONNECTION_TIMEOUT_10_S = 10000;

  private static final Log logger = LogFactory.getLog(IamWSClientImpl.class);

  /**
   * SOAP request template used to grant a role to a user on a research theme. The placeholders are,
   * in order: username, password digest, nonce, creation timestamp, user id, theme id and role.
   */
  private static final String DATA_TEMPLATE_GRANT_THEM_ROLE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" +
    "	xmlns:v3=\"http://ec.europa.eu/rdg/efp/services/iam/research-theme/interfaces/V3\">\r\n" +
    "	<soap:Header>\r\n" +
    "		<wsse:Security\r\n" +
    "			xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\r\n" +
    "			<wsse:UsernameToken wsu:Id=\"UsernameToken-1\"\r\n" +
    "				xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\r\n" +
    "				<wsse:Username>%1$s</wsse:Username>\r\n" +
    "				<wsse:Password\r\n" +
    "					Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%2$s</wsse:Password>\r\n" +
    "				<wsse:Nonce\r\n" +
    "					EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">%3$s</wsse:Nonce>\r\n" +
    "				<wsu:Created>%4$s</wsu:Created>\r\n" +
    "			</wsse:UsernameToken>\r\n" +
    "		</wsse:Security>\r\n" +
    "	</soap:Header>\r\n" +
    "	<soap:Body>\r\n" +
    "		<v3:grantThemeRole>\r\n" +
    "			<v3:UserId>%5$s</v3:UserId>\r\n" +
    "			<v3:ThemeId>%6$s</v3:ThemeId>\r\n" +
    "			<v3:Role>%7$s</v3:Role>\r\n" +
    "		</v3:grantThemeRole>\r\n" +
    "	</soap:Body>\r\n" +
    "</soap:Envelope>";

  /**
   * SOAP request template used to revoke a role from a user on a research theme. The placeholders
   * are, in order: username, password digest, nonce, creation timestamp, user id, theme id and
   * role.
   */
  private static final String DATA_TEMPLATE_REVOKE_THEM_ROLE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" +
    "	xmlns:v3=\"http://ec.europa.eu/rdg/efp/services/iam/research-theme/interfaces/V3\">\r\n" +
    "	<soap:Header>\r\n" +
    "		<wsse:Security\r\n" +
    "			xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\r\n" +
    "			<wsse:UsernameToken wsu:Id=\"UsernameToken-1\"\r\n" +
    "				xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\r\n" +
    "				<wsse:Username>%1$s</wsse:Username>\r\n" +
    "				<wsse:Password\r\n" +
    "					Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%2$s</wsse:Password>\r\n" +
    "				<wsse:Nonce\r\n" +
    "					EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">%3$s</wsse:Nonce>\r\n" +
    "				<wsu:Created>%4$s</wsu:Created>\r\n" +
    "			</wsse:UsernameToken>\r\n" +
    "		</wsse:Security>\r\n" +
    "	</soap:Header>\r\n" +
    "	<soap:Body>\r\n" +
    "		<v3:revokeThemeRole>\r\n" +
    "			<v3:UserId>%5$s</v3:UserId>\r\n" +
    "			<v3:ThemeId>%6$s</v3:ThemeId>\r\n" +
    "			<v3:Role>%7$s</v3:Role>\r\n" +
    "		</v3:revokeThemeRole>\r\n" +
    "	</soap:Body>\r\n" +
    "</soap:Envelope>";

  /** Base URL of the IAM web service endpoint (injected from {@code iam.location}). */
  @Value("${iam.location}")
  private String serviceUrl;

  /** Username used to authenticate against the IAM web service (injected from {@code iam.user}). */
  @Value("${iam.user}")
  private String user;

  /**
   * Password used to build the WS-Security password digest (injected from {@code iam.password}).
   */
  @Value("${iam.password}")
  private String password;

  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Generates a random nonce value used to make each WS-Security token unique.
   *
   * @return a newly generated random nonce as a decimal string
   */
  private static String getNonce() {
    return String.valueOf(RANDOM.nextInt(MAX_NEXT_INT));
  }

  /**
   * Computes the WS-Security password digest expected by the IAM web service.
   *
   * <p>The digest is the Base64 encoding of {@code SHA1(hexEncode(nonce) + created + password)}.
   * SHA-1 is used only for compatibility with the IAM web service.
   *
   * @param nonce the request nonce
   * @param created the request creation timestamp
   * @param password the configured IAM service password
   * @return the Base64-encoded password digest, or {@code null} if the SHA-1 algorithm is
   *     unavailable
   */
  private static String calculatePasswordDigest(
    String nonce,
    String created,
    String password
  ) {
    String encoded = null;
    try {
      String pass = hexEncode(nonce) + created + password;
      // SHA1 is not a strong algorithm, but it is used by the IAM web service
      // and we need to be compatible with it.
      @SuppressWarnings("java:S4790")
      MessageDigest md = MessageDigest.getInstance("SHA1");
      md.update(pass.getBytes());
      byte[] encodedPassword = md.digest();

      encoded = Base64.encodeBase64String(encodedPassword);
    } catch (NoSuchAlgorithmException ex) {
      if (logger.isErrorEnabled()) {
        logger.error("SHA1 algorithm not available", ex);
      }
    }

    return encoded;
  }

  /**
   * Interprets each pair of characters in the input as a two-digit hexadecimal value and converts
   * it into the corresponding character.
   *
   * @param in a string of hexadecimal digit pairs
   * @return the decoded character sequence
   */
  private static String hexEncode(String in) {
    StringBuilder sb = new StringBuilder("");
    for (int i = 0; i < (in.length() - 2) + 1; i = i + 2) {
      int c = Integer.parseInt(in.substring(i, i + 2), 16);
      char chr = (char) c;
      sb.append(chr);
    }
    return sb.toString();
  }

  /**
   * Returns the configured IAM web service endpoint URL.
   *
   * @return the service URL
   */
  public String getServiceUrl() {
    return serviceUrl;
  }

  /**
   * Sets the IAM web service endpoint URL.
   *
   * @param serviceUrl the service URL to use
   */
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * Returns the username used to authenticate against the IAM web service.
   *
   * @return the configured username
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets the username used to authenticate against the IAM web service.
   *
   * @param user the username to use
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Returns the password used to authenticate against the IAM web service.
   *
   * @return the configured password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password used to authenticate against the IAM web service.
   *
   * @param password the password to use
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Grants the given role to the given user on the given research theme by calling the IAM web
   * service.
   *
   * @param userName the identifier of the user to grant the role to
   * @param themeID the identifier of the research theme
   * @param roleID the identifier of the role to grant
   * @throws IamServiceException if the request payload cannot be encoded
   * @throws IamWSEception if the web service call fails or returns an unsuccessful response
   */
  @Override
  public void grantThemeRole(String userName, String themeID, String roleID) {
    callWebService(userName, themeID, roleID, DATA_TEMPLATE_GRANT_THEM_ROLE);
  }

  /**
   * Revokes the given role from the given user on the given research theme by calling the IAM web
   * service.
   *
   * @param userName the identifier of the user to revoke the role from
   * @param themeID the identifier of the research theme
   * @param roleID the identifier of the role to revoke
   * @throws IamServiceException if the request payload cannot be encoded
   * @throws IamWSEception if the web service call fails or returns an unsuccessful response
   */
  @Override
  public void revokeThemeRole(String userName, String themeID, String roleID) {
    callWebService(userName, themeID, roleID, DATA_TEMPLATE_REVOKE_THEM_ROLE);
  }

  /**
   * Builds and sends a SOAP request to the IAM web service using the supplied template.
   *
   * @param userID the identifier of the target user
   * @param themeID the identifier of the research theme
   * @param roleID the identifier of the role
   * @param dataTemplate the SOAP request template to populate (grant or revoke)
   * @throws IamServiceException if the request payload cannot be encoded
   * @throws IamWSEception if the web service call fails or returns an unsuccessful response
   */
  private void callWebService(
    String userID,
    String themeID,
    String roleID,
    String dataTemplate
  ) {
    HttpClient httpClient = new HttpClient();
    httpClient
      .getParams()
      .setParameter("http.useragent", "IAM Web Service Client");
    PostMethod methodPost = new PostMethod(serviceUrl);

    String data = buildRequestData(dataTemplate, userID, themeID, roleID);
    setRequestEntity(methodPost, data);
    methodPost.setRequestHeader("Content-Type", "text/xml");

    executeRequest(httpClient, methodPost);
  }

  /**
   * Populates the given SOAP template with the WS-Security credentials and the request parameters.
   *
   * @param dataTemplate the SOAP request template to populate
   * @param userID the identifier of the target user
   * @param themeID the identifier of the research theme
   * @param roleID the identifier of the role
   * @return the fully rendered SOAP request payload
   */
  private String buildRequestData(
    String dataTemplate,
    String userID,
    String themeID,
    String roleID
  ) {
    String nonce = getNonce();
    String nonceEncoded = Base64.encodeBase64String(
      hexEncode(nonce).getBytes()
    );
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    String created = sdf.format(new Date());
    String digest = calculatePasswordDigest(nonce, created, password);
    return String.format(
      dataTemplate,
      user,
      digest,
      nonceEncoded,
      created,
      userID,
      themeID,
      roleID
    );
  }

  /**
   * Sets the SOAP payload as the request entity of the POST method.
   *
   * @param methodPost the HTTP POST method to configure
   * @param data the SOAP request payload
   * @throws IamServiceException if UTF-8 encoding is not supported
   */
  private void setRequestEntity(PostMethod methodPost, String data) {
    try {
      methodPost.setRequestEntity(
        new StringRequestEntity(data, "text/xml", StandardCharsets.UTF_8.name())
      );
    } catch (UnsupportedEncodingException e) {
      throw new IamServiceException("UTF-8 encoding is not supported", e);
    }
  }

  /**
   * Executes the prepared POST request against the IAM web service, applying the socket timeout and
   * ensuring the connection is released afterwards.
   *
   * @param httpClient the HTTP client to execute the request with
   * @param methodPost the prepared HTTP POST method
   * @throws IamWSEception if an HTTP or I/O error occurs, or if the response is unsuccessful
   */
  private void executeRequest(HttpClient httpClient, PostMethod methodPost) {
    try {
      httpClient.getParams().setSoTimeout(CONNECTION_TIMEOUT_10_S);
      int returnCode = httpClient.executeMethod(methodPost);
      handleResponse(returnCode, methodPost);
    } catch (HttpException e) {
      logger.error("Error Accessing IAM webservice: " + serviceUrl, e);
      throw new IamWSEception("Http Exception", e);
    } catch (IOException e) {
      logger.error("Error Accessing IAM webservice: " + serviceUrl, e);
      throw new IamWSEception("IOException", e);
    } finally {
      methodPost.releaseConnection();
    }
  }

  /**
   * Handles the IAM web service response. A {@code 501 Not Implemented} status is logged and
   * ignored; any other response is considered successful only if its body contains the {@code
   * SUCCESSFUL} marker.
   *
   * @param returnCode the HTTP status code returned by the web service
   * @param methodPost the executed HTTP POST method, used to read the response body
   * @throws IOException if the response body cannot be read
   * @throws IamWSEception if the response does not indicate success
   */
  private void handleResponse(int returnCode, PostMethod methodPost)
    throws IOException {
    if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
      logger.error(
        "The Post method is not implemented by this URI: " + serviceUrl
      );
      return;
    }
    String response = methodPost.getResponseBodyAsString();
    if (!response.contains("SUCCESSFUL")) {
      logger.error(
        "IAM web service call to " + serviceUrl + " was not successful"
      );
      throw new IamWSEception("Response is not successful");
    }
  }
}
