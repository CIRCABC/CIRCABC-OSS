package io.swagger.util.parsers;

import io.swagger.model.*;
import io.swagger.util.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Stateless utility that parses JSON request bodies related to membership
 * history operations into their corresponding domain models.
 *
 * <p>It reads the raw JSON payload from an incoming {@link WebScriptRequest}
 * and converts it into {@link UserRevocationRequest} instances (for user
 * revocation requests) or {@link UserMembershipsExpirationRequest} instances
 * (for membership expiration requests). The expected JSON attribute names are
 * defined by the private {@code String} constants of this class.
 *
 * <p>This class is not meant to be instantiated; all functionality is exposed
 * through {@code static} methods.
 *
 * @author beaurpi
 */
public class HistoryJsonParser {

  /** JSON attribute holding the Alfresco group name of a profile. */
  private static final String ALFRESCO_GROUP_NAME = "groupName";
  /** JSON attribute holding an entity identifier. */
  private static final String ID = "id";
  /** JSON attribute holding a single user identifier. */
  private static final String USER_ID = "userId";
  /** JSON attribute holding an array of user identifiers. */
  private static final String USER_IDS = "userIds";
  /** JSON attribute holding the revocation date. */
  private static final String REVOCATION_DATE = "revocationDate";
  /** JSON attribute holding the membership expiration date. */
  private static final String EXPIRATION_DATE = "expirationDate";
  /** JSON attribute holding the array of memberships. */
  private static final String MEMBERSHIPS = "memberships";
  /** JSON attribute holding the profile object. */
  private static final String PROFILE = "profile";
  /** JSON attribute holding the requester identifier. */
  private static final String REQUESTER = "requester";
  /** JSON attribute holding the state of the request. */
  private static final String REQUEST_STATE = "requestState";
  /** JSON attribute holding the requested action. */
  private static final String ACTION = "action";
  /** JSON attribute holding the interest group object. */
  private static final String GROUP = "interestGroup";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class must not be
   *     instantiated
   */
  private HistoryJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given request into a
   * {@link UserRevocationRequest}.
   *
   * <p>The payload is expected to be a JSON object. Recognised attributes are
   * {@code id}, {@code action}, {@code userIds}, {@code revocationDate},
   * {@code requester} and {@code requestState}; any missing attribute is simply
   * left unset on the returned object.
   *
   * @param req the web script request whose content holds the JSON payload
   * @return the {@link UserRevocationRequest} built from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static UserRevocationRequest parseRevocationRequest(
    WebScriptRequest req
  ) throws IOException, ParseException {
    UserRevocationRequest request = new UserRevocationRequest();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String id = (String) json.get(ID);
    if (id != null) {
      request.setId(Integer.parseInt(id));
    }

    String action = (String) json.get(ACTION);
    if (action != null) {
      request.setAction(action);
    }

    JSONArray userids = (JSONArray) json.get(USER_IDS);
    for (Object userId : userids) {
      if (userId != null) {
        request.getUserIds().add(userId.toString());
      }
    }

    String revocationDate = (String) json.get(REVOCATION_DATE);
    if (revocationDate != null) {
      request.setRevocationDate(new DateTime(revocationDate));
    }

    String requester = (String) json.get(REQUESTER);
    if (requester != null) {
      request.setRequester(requester);
    }

    String requestState = (String) json.get(REQUEST_STATE);
    if (requestState != null) {
      request.setRequestState(Integer.parseInt(requestState));
    }

    return request;
  }

  /**
   * Parses the JSON body of the given request into a list of
   * {@link UserMembershipsExpirationRequest} items.
   *
   * <p>The payload is expected to be a JSON array; each element is converted
   * via {@link #parseExpirationRequestItem(JSONObject)}. A {@code null} or
   * empty array yields an empty list.
   *
   * @param req the web script request whose content holds the JSON payload
   * @return the list of parsed expiration requests, never {@code null}
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   * @throws java.text.ParseException if an expiration date cannot be parsed
   */
  public static List<
    UserMembershipsExpirationRequest
  > parseUserMembershipsExpirationRequests(WebScriptRequest req)
    throws IOException, ParseException, java.text.ParseException {
    List<UserMembershipsExpirationRequest> result = new ArrayList<>();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONArray json = (JSONArray) parser.parse(cBody);

    if (json != null && !json.isEmpty()) {
      for (Object o : json) {
        JSONObject requestItem = (JSONObject) o;
        if (requestItem != null) {
          result.add(parseExpirationRequestItem(requestItem));
        }
      }
    }

    return result;
  }

  /**
   * Converts a single JSON object into a
   * {@link UserMembershipsExpirationRequest}.
   *
   * @param requestItem the JSON object describing one expiration request;
   *     expected to hold {@code userId}, {@code expirationDate} and
   *     {@code memberships} attributes
   * @return the populated expiration request item
   * @throws java.text.ParseException if the expiration date cannot be parsed
   */
  private static UserMembershipsExpirationRequest parseExpirationRequestItem(
    JSONObject requestItem
  ) throws java.text.ParseException {
    UserMembershipsExpirationRequest item =
      new UserMembershipsExpirationRequest();
    item.setUserId((String) requestItem.get(USER_ID));
    item.setExpirationDate(
      Converter.convertStringToDate((String) requestItem.get(EXPIRATION_DATE))
    );
    item.setMemberships(
      parseMemberships((JSONArray) requestItem.get(MEMBERSHIPS))
    );
    return item;
  }

  /**
   * Converts a JSON array of membership objects into a list of
   * {@link InterestGroupProfile} instances.
   *
   * <p>Each membership entry must provide a {@code profile} object (with an
   * {@code id} and {@code groupName}) and an {@code interestGroup} object (with
   * an {@code id}); entries missing any of these values are skipped.
   *
   * @param jsonMemberships the JSON array of membership objects, may be
   *     {@code null} or empty
   * @return the list of interest group profiles, never {@code null}
   */
  private static List<InterestGroupProfile> parseMemberships(
    JSONArray jsonMemberships
  ) {
    List<InterestGroupProfile> memberships = new ArrayList<>();
    if (jsonMemberships == null || jsonMemberships.isEmpty()) {
      return memberships;
    }
    for (Object jsonMembership : jsonMemberships) {
      JSONObject membershipItem = (JSONObject) jsonMembership;
      JSONObject profileItem = (JSONObject) membershipItem.get(PROFILE);
      String alfrescoGroupName = (String) profileItem.get(ALFRESCO_GROUP_NAME);
      String profileId = (String) profileItem.get(ID);

      JSONObject groupItem = (JSONObject) membershipItem.get(GROUP);
      String groupId = (String) groupItem.get(ID);

      if (groupId != null && profileId != null && alfrescoGroupName != null) {
        InterestGroupProfile groupProfile = new InterestGroupProfile();
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setGroupName(alfrescoGroupName);
        groupProfile.setProfile(profile);
        InterestGroup group = new InterestGroup();
        group.setId(groupId);
        groupProfile.setInterestGroup(group);
        memberships.add(groupProfile);
      }
    }
    return memberships;
  }
}
