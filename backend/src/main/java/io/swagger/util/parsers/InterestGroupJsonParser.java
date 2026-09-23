package io.swagger.util.parsers;

import io.swagger.model.*;
import io.swagger.util.EmailUtil;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class that parses the JSON payloads of Interest Group related REST
 * requests into their corresponding domain model objects.
 *
 * <p>Each {@code parse*} method reads the body of the supplied
 * {@link WebScriptRequest}, deserializes it with the {@code json-simple}
 * parser and maps the resulting JSON structure onto a specific model type
 * (for example {@link InterestGroup}, {@link InterestGroupPostModel},
 * {@link GroupCreationRequest} or {@link GroupConfiguration}). Internationalized
 * text fields (title, description, contact, ...) are copied only for the
 * language codes declared in {@link SupportedLanguages}, and e-mail addresses
 * are sanitized through {@link EmailUtil} before being stored.
 *
 * <p>This class is stateless and not meant to be instantiated; all behavior is
 * exposed through {@code static} methods.
 *
 * @author beaurpi
 */
public class InterestGroupJsonParser {

  /** Logger used to report invalid or discarded input (e.g. bad e-mails). */
  private static final Log logger = LogFactory.getLog(
    InterestGroupJsonParser.class
  );

  // JSON property names expected in the parsed request bodies.
  private static final String EMAIL = "email";
  private static final String LASTNAME = "lastname";
  private static final String FIRSTNAME = "firstname";
  private static final String USER_ID = "userId";
  private static final String JUSTIFICATION = "justification";
  private static final String ID = "id";
  private static final String IS_PUBLIC = "isPublic";
  private static final String IS_REGISTERED = "isRegistered";
  private static final String ALLOW_APPLY = "allowApply";
  private static final String NAME = "name";
  private static final String PROPOSED_NAME = "proposedName";
  private static final String TITLE = "title";
  private static final String PROPOSED_TITLE = "proposedTitle";
  private static final String DESCRIPTION = "description";
  private static final String PROPOSED_DESCRIPTION = "proposedDescription";
  private static final String CONTACT = "contact";
  private static final String LEADERS = "leaders";
  private static final String NOTIFY = "notify";
  private static final String NOTIFY_TEXT = "notifyText";
  private static final String CATEGORY_REF = "categoryRef";
  private static final String AGREEMENT = "agreement";
  private static final String ARGUMENT = "argument";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static
   *     members
   */
  private InterestGroupJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Copies the localized values found in the given JSON object into the target
   * map, keeping only the language codes supported by the application.
   *
   * @param jsonMap the JSON object holding language-code/value pairs; may be
   *     {@code null}, in which case the target map is left untouched
   * @param targetMap the internationalization map to populate
   */
  private static void populateI18nMap(
    JSONObject jsonMap,
    Map<String, String> targetMap
  ) {
    if (jsonMap == null) {
      return;
    }
    for (String code : SupportedLanguages.availableLangCodes) {
      if (jsonMap.containsKey(code)) {
        targetMap.put(code, String.valueOf(jsonMap.get(code)));
      }
    }
  }

  /**
   * Builds a {@link User} from a JSON object, sanitizing its e-mail address.
   *
   * <p>The user id, first name and last name are copied verbatim. The e-mail is
   * validated and sanitized via {@link EmailUtil#sanitizeEmailAddresses(String)};
   * if it is invalid a warning is logged and the e-mail is left {@code null}.
   *
   * @param userJson the JSON object describing the user
   * @param context a short description of the calling context, used in log
   *     messages to identify the source of an invalid e-mail
   * @return the populated {@link User} instance
   */
  private static User parseUserWithEmail(JSONObject userJson, String context) {
    User user = new User();
    user.setUserId(String.valueOf(userJson.get(USER_ID)));
    user.setFirstname(String.valueOf(userJson.get(FIRSTNAME)));
    user.setLastname(String.valueOf(userJson.get(LASTNAME)));

    String email = String.valueOf(userJson.get(EMAIL));
    if (email != null && !"null".equals(email)) {
      String sanitizedEmail = EmailUtil.sanitizeEmailAddresses(email);
      if (sanitizedEmail != null && !sanitizedEmail.isEmpty()) {
        user.setEmail(sanitizedEmail);
      } else {
        if (logger.isWarnEnabled()) {
          logger.warn("Invalid email in " + context + ": " + email);
        }
        user.setEmail(null);
      }
    }
    return user;
  }

  /**
   * Parses a partial Interest Group update payload.
   *
   * <p>Reads the identity flags ({@code isPublic}, {@code isRegistered},
   * {@code allowApply}), name, id and the localized title, description and
   * contact fields from the request body.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link InterestGroup} populated from the request
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static InterestGroup parsePartialJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    InterestGroup body = new InterestGroup();
    body.setName(String.valueOf(json.get(NAME)));
    body.setId(String.valueOf(json.get(ID)));
    body.setIsPublic((Boolean) json.get(IS_PUBLIC));
    body.setIsRegistered((Boolean) json.get(IS_REGISTERED));
    body.setAllowApply((Boolean) json.get(ALLOW_APPLY));

    populateI18nMap((JSONObject) json.get(TITLE), body.getTitle());
    populateI18nMap((JSONObject) json.get(DESCRIPTION), body.getDescription());
    populateI18nMap((JSONObject) json.get(CONTACT), body.getContact());

    return body;
  }

  /**
   * Parses the payload used to create a new Interest Group.
   *
   * <p>Populates the name, the localized title, description and contact fields,
   * the list of leader identifiers, the notification flag and the localized
   * notification text.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link InterestGroupPostModel} populated from the request
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static InterestGroupPostModel parseNewGroup(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    InterestGroupPostModel body = new InterestGroupPostModel();
    body.setName(String.valueOf(json.get(NAME)));

    populateI18nMap((JSONObject) json.get(TITLE), body.getTitle());
    populateI18nMap((JSONObject) json.get(DESCRIPTION), body.getDescription());
    populateI18nMap((JSONObject) json.get(CONTACT), body.getContact());

    JSONArray leaders = (JSONArray) json.get(LEADERS);
    if (leaders != null) {
      for (int i = 0; i < leaders.size(); i++) {
        body.addLeadersItem(leaders.get(i).toString());
      }
    }

    body.setNotify(Boolean.valueOf(json.get(NOTIFY).toString()));

    populateI18nMap((JSONObject) json.get(NOTIFY_TEXT), body.getNotifyText());

    return body;
  }

  /**
   * Parses a request to create a new Interest Group that must go through an
   * approval workflow.
   *
   * <p>Reads the optional request id, the proposed name, the parent category
   * reference, the justification, the requesting user (with sanitized e-mail),
   * the localized proposed title and description, and the list of proposed
   * leaders.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link GroupCreationRequest} populated from the request
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static GroupCreationRequest parseGroupCreationRequest(
    WebScriptRequest req
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    GroupCreationRequest body = new GroupCreationRequest();
    if (json.get(ID) != null) {
      body.setId(Integer.parseInt(String.valueOf(json.get(ID))));
    }
    body.setProposedName(String.valueOf(json.get(PROPOSED_NAME)));
    body.setCategoryRef(String.valueOf((json.get(CATEGORY_REF))));
    body.setJustification(String.valueOf(json.get(JUSTIFICATION)));

    JSONObject from = (JSONObject) json.get("from");
    if (from != null) {
      body.setFrom(parseUserWithEmail(from, "GroupCreationRequest from user"));
    }

    populateI18nMap(
      (JSONObject) json.get(PROPOSED_TITLE),
      body.getProposedTitle()
    );
    populateI18nMap(
      (JSONObject) json.get(PROPOSED_DESCRIPTION),
      body.getProposedDescription()
    );

    parseLeaders(json, body);

    return body;
  }

  /**
   * Extracts the list of proposed leaders from the JSON and appends the valid
   * ones to the given creation request.
   *
   * <p>A leader is kept when it has a valid (sanitized) e-mail address, or when
   * it has no e-mail but carries a user id (in which case its e-mail is set to
   * {@code null}).
   *
   * @param json the root JSON object containing the {@code leaders} array
   * @param body the creation request to which valid leaders are added
   */
  private static void parseLeaders(JSONObject json, GroupCreationRequest body) {
    JSONArray leaders = (JSONArray) json.get(LEADERS);
    if (leaders == null) {
      return;
    }
    for (int i = 0; i < leaders.size(); i++) {
      JSONObject userJson = (JSONObject) leaders.get(i);
      User u = parseUserWithEmail(userJson, "leader");

      String email = String.valueOf(userJson.get(EMAIL));
      if (email != null && !"null".equals(email)) {
        if (u.getEmail() != null) {
          body.addLeadersItem(u);
        }
      } else if (u.getUserId() != null && !"null".equals(u.getUserId())) {
        u.setEmail(null);
        body.addLeadersItem(u);
      }
    }
  }

  /**
   * Parses the newsgroup-related configuration of an Interest Group.
   *
   * <p>Only the {@code newsgroups} section is read. Missing values fall back to
   * defaults: the "new topic"/"new forum" flags default to {@code false} and
   * the corresponding age thresholds default to {@code 7} days.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link GroupConfiguration} populated from the request
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static GroupConfiguration parseGroupConfiguration(WebScriptRequest req)
    throws IOException, ParseException {
    GroupConfiguration result = new GroupConfiguration();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    JSONObject newsgroups = (JSONObject) json.get("newsgroups");

    if (newsgroups != null) {
      GroupConfigurationNewsgroups newsConf =
        new GroupConfigurationNewsgroups();

      Boolean enableFlagNewTopic = (Boolean) newsgroups.get(
        "enableFlagNewTopic"
      );
      if (enableFlagNewTopic != null) {
        newsConf.setEnableFlagNewTopic(enableFlagNewTopic);
      } else {
        newsConf.setEnableFlagNewTopic(false);
      }

      Boolean enableFlagNewForum = (Boolean) newsgroups.get(
        "enableFlagNewForum"
      );
      if (enableFlagNewForum != null) {
        newsConf.setEnableFlagNewForum(enableFlagNewForum);
      } else {
        newsConf.setEnableFlagNewForum(false);
      }

      Long ageFlagNewForum = (Long) newsgroups.get("ageFlagNewForum");
      if (ageFlagNewForum != null) {
        newsConf.setAgeFlagNewForum(
          Integer.parseInt(ageFlagNewForum.toString())
        );
      } else {
        newsConf.setAgeFlagNewForum(7);
      }

      Long ageFlagNewTopic = (Long) newsgroups.get("ageFlagNewTopic");
      if (ageFlagNewTopic != null) {
        newsConf.setAgeFlagNewTopic(
          Integer.parseInt(ageFlagNewTopic.toString())
        );
      } else {
        newsConf.setAgeFlagNewTopic(7);
      }

      result.setNewsgroups(newsConf);
    }

    return result;
  }

  /**
   * Parses the approval (or rejection) decision for a group creation request.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link GroupCreationRequestApproval} holding the request id,
   *     the agreement decision and the associated argument
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static GroupCreationRequestApproval parseGroupCreationRequestApproval(
    WebScriptRequest req
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    GroupCreationRequestApproval body = new GroupCreationRequestApproval();
    Object requestId = json.get(ID);
    if (requestId != null) {
      body.setId(Long.parseLong(requestId.toString()));
    }

    Object agreement = json.get(AGREEMENT);
    if (agreement != null) {
      body.setAgreement(Integer.parseInt(agreement.toString()));
    }

    body.setArgument(String.valueOf(json.get(ARGUMENT)));

    return body;
  }

  /**
   * Builds a group deletion request from the supplied contextual data and the
   * JSON body.
   *
   * <p>The requesting user, category reference and group id are taken from the
   * method arguments, the request date is set to the current time, and the
   * justification is read from the JSON body (defaulting to an empty string
   * when absent).
   *
   * @param req the web script request carrying the JSON body
   * @param from the user id of the requester
   * @param categoryRef the reference of the category the group belongs to
   * @param groupId the id of the group to delete
   * @return the {@link GroupDeletionRequest} populated from the inputs
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static GroupDeletionRequest parseGroupDeletionRequest(
    WebScriptRequest req,
    String from,
    String categoryRef,
    String groupId
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);
    GroupDeletionRequest body = new GroupDeletionRequest();
    User fromUser = new User();
    fromUser.setUserId(from);
    body.setFrom(fromUser);
    body.setCategoryRef(categoryRef);
    body.setRequestDate(DateTime.now());
    Object justification = json.get(JUSTIFICATION);
    body.setJustification(
      justification != null ? getString(json, JUSTIFICATION) : ""
    );
    body.setGroupId(groupId);

    return body;
  }

  /**
   * Returns the string value associated with the given key in the JSON object.
   *
   * @param json the JSON object to read from
   * @param key the property name to look up
   * @return the value's string representation, or an empty string if the value
   *     is {@code null}
   */
  private static String getString(JSONObject json, String key) {
    Object value = json.get(key);
    return value != null ? value.toString() : "";
  }

  /**
   * Parses the approval (or rejection) decision for a group deletion request.
   *
   * @param req the web script request carrying the JSON body
   * @return the {@link GroupDeletionRequestApproval} holding the request id,
   *     the agreement decision and the associated argument
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON
   */
  public static GroupDeletionRequestApproval parseGroupDeletionRequestApproval(
    WebScriptRequest req
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    GroupDeletionRequestApproval body = new GroupDeletionRequestApproval();
    Object requestId = json.get(ID);
    if (requestId != null) {
      body.setId(Long.parseLong(requestId.toString()));
    }

    Object agreement = json.get(AGREEMENT);
    if (agreement != null) {
      body.setAgreement(Integer.parseInt(agreement.toString()));
    }

    body.setArgument(String.valueOf(json.get(ARGUMENT)));

    return body;
  }
}
