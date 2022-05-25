package io.swagger.util.parsers;

import io.swagger.model.*;
import io.swagger.util.SupportedLanguages;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class InterestGroupJsonParser {

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

    private InterestGroupJsonParser() {
        throw new IllegalStateException("Utility class");
    }

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

        JSONObject titles = (JSONObject) json.get(TITLE);
        for (String code : SupportedLanguages.availableLangCodes) {
            if (titles.containsKey(code)) {
                body.getTitle().put(code, String.valueOf(titles.get(code)));
            }
        }

        JSONObject descriptions = (JSONObject) json.get(DESCRIPTION);
        for (String code : SupportedLanguages.availableLangCodes) {
            if (descriptions.containsKey(code)) {
                body.getDescription().put(code, String.valueOf(descriptions.get(code)));
            }
        }

        JSONObject contacts = (JSONObject) json.get(CONTACT);
        for (String code : SupportedLanguages.availableLangCodes) {
            if (contacts.containsKey(code)) {
                body.getContact().put(code, String.valueOf(contacts.get(code)));
            }
        }

        return body;
    }

    public static InterestGroupPostModel parseNewGroup(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        InterestGroupPostModel body = new InterestGroupPostModel();
        body.setName(String.valueOf(json.get(NAME)));

        JSONObject titles = (JSONObject) json.get(TITLE);
        if (titles != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (titles.containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(titles.get(code)));
                }
            }
        }

        JSONObject descriptions = (JSONObject) json.get(DESCRIPTION);
        if (descriptions != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (descriptions.containsKey(code)) {
                    body.getDescription().put(code, String.valueOf(descriptions.get(code)));
                }
            }
        }

        JSONObject contacts = (JSONObject) json.get(CONTACT);
        if (contacts != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (contacts.containsKey(code)) {
                    body.getContact().put(code, String.valueOf(contacts.get(code)));
                }
            }
        }

        JSONArray leaders = (JSONArray) json.get(LEADERS);
        if (leaders != null) {
            int i;
            for (i = 0; i < leaders.size(); i++) {
                body.addLeadersItem(leaders.get(i).toString());
            }
        }

        body.setNotify(Boolean.valueOf(json.get(NOTIFY).toString()));

        JSONObject notifyText = (JSONObject) json.get(NOTIFY_TEXT);
        if (notifyText != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (notifyText.containsKey(code)) {
                    body.getNotifyText().put(code, String.valueOf(notifyText.get(code)));
                }
            }
        }

        return body;
    }

    public static GroupCreationRequest parseGroupCreationRequest(WebScriptRequest req)
            throws IOException, ParseException {

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
            User fromUser = new User();
            fromUser.setUserId(String.valueOf(from.get(USER_ID)));
            fromUser.setFirstname(String.valueOf(from.get(FIRSTNAME)));
            fromUser.setLastname(String.valueOf(from.get(LASTNAME)));
            fromUser.setEmail(String.valueOf(from.get(EMAIL)));
            body.setFrom(fromUser);
        }

        JSONObject titles = (JSONObject) json.get(PROPOSED_TITLE);
        if (titles != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (titles.containsKey(code)) {
                    body.getProposedTitle().put(code, String.valueOf(titles.get(code)));
                }
            }
        }

        JSONObject descriptions = (JSONObject) json.get(PROPOSED_DESCRIPTION);
        if (descriptions != null) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (descriptions.containsKey(code)) {
                    body.getProposedDescription().put(code, String.valueOf(descriptions.get(code)));
                }
            }
        }

        JSONArray leaders = (JSONArray) json.get(LEADERS);
        if (leaders != null) {
            int i;
            for (i = 0; i < leaders.size(); i++) {
                JSONObject user = (JSONObject) leaders.get(i);
                User u = new User();
                u.setUserId(String.valueOf(user.get(USER_ID)));
                u.setFirstname(String.valueOf(user.get(FIRSTNAME)));
                u.setLastname(String.valueOf(user.get(LASTNAME)));
                u.setEmail(String.valueOf(user.get(EMAIL)));
                body.addLeadersItem(u);
            }
        }

        return body;
    }

    public static GroupConfiguration parseGroupConfiguration(WebScriptRequest req)
            throws IOException, ParseException {
        GroupConfiguration result = new GroupConfiguration();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        JSONObject newsgroups = (JSONObject) json.get("newsgroups");

        if (newsgroups != null) {
            GroupConfigurationNewsgroups newsConf = new GroupConfigurationNewsgroups();

            Boolean enableFlagNewTopic = (Boolean) newsgroups.get("enableFlagNewTopic");
            if (enableFlagNewTopic != null) {
                newsConf.setEnableFlagNewTopic(enableFlagNewTopic);
            } else {
                newsConf.setEnableFlagNewTopic(false);
            }

            Boolean enableFlagNewForum = (Boolean) newsgroups.get("enableFlagNewForum");
            if (enableFlagNewForum != null) {
                newsConf.setEnableFlagNewForum(enableFlagNewForum);
            } else {
                newsConf.setEnableFlagNewForum(false);
            }

            Long ageFlagNewForum = (Long) newsgroups.get("ageFlagNewForum");
            if (ageFlagNewForum != null) {
                newsConf.setAgeFlagNewForum(Integer.parseInt(ageFlagNewForum.toString()));
            } else {
                newsConf.setAgeFlagNewForum(7);
            }

            Long ageFlagNewTopic = (Long) newsgroups.get("ageFlagNewTopic");
            if (ageFlagNewTopic != null) {
                newsConf.setAgeFlagNewTopic(Integer.parseInt(ageFlagNewTopic.toString()));
            } else {
                newsConf.setAgeFlagNewTopic(7);
            }

            result.setNewsgroups(newsConf);
        }

        return result;
    }

    public static GroupCreationRequestApproval parseGroupCreationRequestApproval(WebScriptRequest req)
            throws IOException, ParseException {

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
}
