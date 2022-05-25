package io.swagger.util.parsers;

import io.swagger.model.*;
import io.swagger.util.Converter;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author beaurpi
 */
public class HistoryJsonParser {

    private static final String ALFRESCO_GROUP_NAME = "groupName";
    private static final String ID = "id";
    private static final String USER_ID = "userId";
    private static final String USER_IDS = "userIds";
    private static final String REVOCATION_DATE = "revocationDate";
    private static final String EXPIRATION_DATE = "expirationDate";
    private static final String MEMBERSHIPS = "memberships";
    private static final String PROFILE = "profile";
    private static final String REQUESTER = "requester";
    private static final String REQUEST_STATE = "requestState";
    private static final String ACTION = "action";
    private static final String GROUP = "interestGroup";

    private HistoryJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static UserRevocationRequest parseRevocationRequest(WebScriptRequest req)
            throws IOException, ParseException {

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

    public static List<UserMembershipsExpirationRequest> parseUserMembershipsExpirationRequests(
            WebScriptRequest req) throws IOException, ParseException, java.text.ParseException {
        List<UserMembershipsExpirationRequest> result = new ArrayList<>();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(cBody);

        if (json != null && !json.isEmpty()) {
            for (Object o : json) {
                JSONObject requestItem = (JSONObject) o;
                if (requestItem != null) {
                    UserMembershipsExpirationRequest item = new UserMembershipsExpirationRequest();
                    String userId = (String) requestItem.get(USER_ID);
                    item.setUserId(userId);

                    String date = (String) requestItem.get(EXPIRATION_DATE);
                    item.setExpirationDate(Converter.convertStringToDate(date));

                    List<InterestGroupProfile> memberships = new ArrayList<>();
                    JSONArray jsonMemberships = (JSONArray) requestItem.get(MEMBERSHIPS);
                    if (jsonMemberships != null && !jsonMemberships.isEmpty()) {
                        for (Object jsonMembership : jsonMemberships) {
                            InterestGroupProfile groupProfile = new InterestGroupProfile();
                            JSONObject membershipItem = (JSONObject) jsonMembership;
                            JSONObject profileItem = (JSONObject) membershipItem.get(PROFILE);
                            String alfrescoGroupName = (String) profileItem.get(ALFRESCO_GROUP_NAME);
                            String profileId = (String) profileItem.get(ID);
                            Profile profile = new Profile();
                            profile.setId(profileId);
                            profile.setGroupName(alfrescoGroupName);
                            groupProfile.setProfile(profile);

                            JSONObject groupItem = (JSONObject) membershipItem.get(GROUP);
                            String groupId = (String) groupItem.get(ID);
                            InterestGroup group = new InterestGroup();
                            group.setId(groupId);
                            groupProfile.setInterestGroup(group);

                            if (groupId != null && profileId != null && alfrescoGroupName != null) {
                                memberships.add(groupProfile);
                            }
                        }
                    }
                    item.setMemberships(memberships);
                    result.add(item);
                }
            }
        }

        return result;
    }
}
