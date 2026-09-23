package io.swagger.util.parsers;

import io.swagger.model.EmailDefinition;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.db.DistributionEmailDAO;
import io.swagger.util.EmailUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that converts the JSON payload of email-related webscript
 * requests into the corresponding domain objects.
 *
 * <p>It reads the raw body of a {@link WebScriptRequest} and produces either a
 * fully populated {@link EmailDefinition} (subject, content, target profiles,
 * target users and attachments) or a list of {@link DistributionEmailDAO}
 * entries. Email addresses encountered while parsing are validated and
 * sanitized through {@link EmailUtil#sanitizeEmailAddresses(String)}; invalid
 * addresses are logged and discarded.
 *
 * <p>This class is a stateless utility: it exposes only static methods and
 * cannot be instantiated.
 *
 * @author beaurpi
 */
public class EmailJsonParser {

  /** Logger used to report invalid or skipped email addresses. */
  private static final Log logger = LogFactory.getLog(EmailJsonParser.class);

  private static final String SUBJECT = "subject";
  private static final String CONTENT = "content";
  private static final String PROFILES = "profiles";
  private static final String ID = "id";
  private static final String GROUP_NAME = "groupName";
  private static final String NAME = "name";
  private static final String USERS = "users";
  private static final String USER_ID = "userId";
  private static final String FIRSTNAME = "firstname";
  private static final String LASTNAME = "lastname";
  private static final String EMAIL = "email";
  private static final String EMAIL_ADDRESS = "emailAddress";
  private static final String ATTACHMENTS = "attachments";

  private EmailJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given request into an {@link EmailDefinition}.
   *
   * <p>The expected JSON object may contain a {@code subject}, a {@code content},
   * a {@code profiles} array, a {@code users} array and an {@code attachments}
   * array. Missing arrays are simply ignored. User email addresses are
   * sanitized during parsing.
   *
   * @param req the webscript request whose body holds the email JSON object
   * @return the populated email definition
   * @throws IOException if the request body cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
  public static EmailDefinition parse(WebScriptRequest req)
    throws IOException, ParseException {
    EmailDefinition result = new EmailDefinition();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String subject = (String) json.get(SUBJECT);
    result.setSubject(subject);

    String content = (String) json.get(CONTENT);
    result.setContent(content);

    JSONArray profiles = (JSONArray) json.get(PROFILES);
    if (profiles != null) {
      for (Object profile : profiles) {
        if (profile instanceof JSONObject prof) {
          result.getProfiles().add(parseProfile(prof));
        }
      }
    }

    JSONArray users = (JSONArray) json.get(USERS);
    if (users != null) {
      for (Object user : users) {
        if (user instanceof JSONObject u) {
          result.getUsers().add(parseUser(u));
        }
      }
    }

    JSONArray attachments = (JSONArray) json.get(ATTACHMENTS);
    if (attachments != null) {
      for (Object attachment : attachments) {
        result.getAttachments().add(attachment.toString());
      }
    }

    return result;
  }

  /**
   * Builds a {@link User} from its JSON representation.
   *
   * <p>The user's email address, when present, is sanitized through
   * {@link EmailUtil#sanitizeEmailAddresses(String)}; if it is invalid it is
   * logged as a warning and the email is left {@code null}.
   *
   * @param u the JSON object describing the user
   * @return the parsed user
   */
  private static User parseUser(JSONObject u) {
    User result = new User();

    String uid = (String) u.get(USER_ID);
    result.setUserId(uid);

    String fname = (String) u.get(FIRSTNAME);
    result.setFirstname(fname);

    String lname = (String) u.get(LASTNAME);
    result.setLastname(lname);

    String email = (String) u.get(EMAIL);
    if (email != null) {
      String sanitizedEmail = EmailUtil.sanitizeEmailAddresses(email);
      if (sanitizedEmail != null && !sanitizedEmail.isEmpty()) {
        result.setEmail(sanitizedEmail);
      } else {
        if (logger.isWarnEnabled()) {
          logger.warn("Invalid email address in JSON: " + email);
        }
        result.setEmail(null);
      }
    }

    return result;
  }

  /**
   * Builds a {@link Profile} from its JSON representation.
   *
   * @param emailProfile the JSON object describing the profile, expected to
   *     hold an {@code id}, a {@code groupName} and a {@code name}
   * @return the parsed profile
   */
  public static Profile parseProfile(JSONObject emailProfile) {
    Profile profile = new Profile();
    String id = (String) emailProfile.get(ID);
    profile.setId(id);

    String groupName = (String) emailProfile.get(GROUP_NAME);
    profile.setGroupName(groupName);

    String name = (String) emailProfile.get(NAME);
    profile.setName(name);

    return profile;
  }

  /**
   * Parses the JSON body of the given request into a list of
   * {@link DistributionEmailDAO} entries.
   *
   * <p>The request body is expected to be a JSON array of objects, each
   * describing a distribution email. Entries whose email address is missing or
   * invalid are skipped.
   *
   * @param req the webscript request whose body holds the distribution email
   *     JSON array
   * @return the list of parsed distribution emails; never {@code null}
   * @throws IOException if the request body cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
  public static List<DistributionEmailDAO> parseDistributionEmails(
    WebScriptRequest req
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONArray json = (JSONArray) parser.parse(cBody);

    List<DistributionEmailDAO> result = new ArrayList<>();

    if (json != null) {
      for (Object distributionEmail : json) {
        if (distributionEmail instanceof JSONObject distrib) {
          DistributionEmailDAO item = parseDistributionEmail(distrib);
          if (item != null) {
            result.add(item);
          }
        }
      }
    }

    return result;
  }

  /**
   * Builds a single {@link DistributionEmailDAO} from its JSON representation.
   *
   * <p>The optional {@code id} is parsed as an integer. The mandatory
   * {@code emailAddress} is lower-cased and sanitized; if it is absent or
   * invalid the entry is skipped and {@code null} is returned.
   *
   * @param distrib the JSON object describing the distribution email
   * @return the parsed distribution email, or {@code null} if its email address
   *     is missing or invalid
   */
  private static DistributionEmailDAO parseDistributionEmail(
    JSONObject distrib
  ) {
    DistributionEmailDAO item = new DistributionEmailDAO();
    Object idObj = distrib.get(ID);
    if (idObj != null) {
      item.setId(Integer.parseInt(idObj.toString()));
    }

    Object addrObj = distrib.get(EMAIL_ADDRESS);
    if (addrObj == null) {
      return null;
    }

    String emailAddress = addrObj.toString().toLowerCase();
    String sanitizedEmail = EmailUtil.sanitizeEmailAddresses(emailAddress);
    if (sanitizedEmail != null && !sanitizedEmail.isEmpty()) {
      item.setEmailAddress(sanitizedEmail);
      return item;
    }

    if (logger.isWarnEnabled()) {
      logger.warn(
        "Skipping invalid distribution email address: " + emailAddress
      );
    }
    return null;
  }
}
