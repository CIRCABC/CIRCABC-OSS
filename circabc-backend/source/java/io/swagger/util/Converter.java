package io.swagger.util;

import eu.cec.digit.circabc.repo.app.model.Category;
import eu.cec.digit.circabc.repo.app.model.Header;
import eu.cec.digit.circabc.repo.app.model.InterestGroupItem;
import io.swagger.model.I18nProperty;
import io.swagger.model.InterestGroup;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.servlet.FormData;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

public class Converter {

    private static final StoreRef STORE_REF_WORKSPACE_VERSION2STORE =
            new StoreRef("workspace", "version2Store");

    private Converter() {
        throw new IllegalStateException("Utility class");
    }

    public static I18nProperty toI18NProperty(String text) {
        I18nProperty result = new I18nProperty();
        result.put(I18NUtil.getLocale().toLanguageTag(), text);
        return result;
    }

    public static I18nProperty toI18NProperty(MLText text) {
        I18nProperty result = new I18nProperty();
        for (Entry<Locale, String> entry : text.entrySet()) {
            result.put(entry.getKey().getLanguage(), entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    public static String convertDateToString(Date d) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted
        // "Z"
        // to
        // indicate
        // UTC,
        // no
        // timezone
        // offset
        df.setTimeZone(tz);
        return df.format(d);
    }

    public static I18nProperty convertMlToI18nProperty(Map<String, String> ml) {

        I18nProperty i18n = new I18nProperty();

        for (Entry<String, String> entry : ml.entrySet()) {
            if (entry.getKey() != null && entry.getKey().length() > 2) {
                i18n.put(entry.getKey().substring(0, 2), entry.getValue());
            } else {
                i18n.put(entry.getKey(), entry.getValue());
            }
        }
        
        return i18n;
    }

    public static MLText toMLText(I18nProperty property) {
        MLText result = new MLText();
        for (Entry<String, String> entry : property.entrySet()) {
            if (entry.getValue() != null
                    && !"null".equals(entry.getValue())
                    && !entry.getValue().isEmpty()) {
                result.addValue(new Locale(entry.getKey()), entry.getValue());
            } else {
                result.addValue(new Locale(entry.getKey()), "");
            }
        }
        return result;
    }

    public static MLText toMLText(JSONObject object) throws JSONException {
        MLText result = new MLText();
        @SuppressWarnings("unchecked") final Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            String value = (String) object.get(key);
            if (value != null) {
                result.addValue(new Locale(key), value);
            } else {
                result.addValue(new Locale(key), "");
            }
        }
        return result;
    }

    public static MLText toMLTextEN(String str) {
        MLText result = new MLText();
        result.addValue(Locale.ENGLISH, str);
        return result;
    }

    public static I18nProperty toI18NProperty(JSONObject object) throws JSONException {
        I18nProperty result = new I18nProperty();
        @SuppressWarnings("unchecked") final Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            String value;
            Object valueObject = object.get(key);
            String className = valueObject.getClass().getName();
            if ("org.json.JSONObject$Null".equals(className)) {
                continue;
            }
            value = (String) valueObject;
            result.put(key, value);
        }
        return result;
    }

    /**
     * Get the string value corresponding to the provided MLText property.
     *
     * @param property If null, return an empty string.
     * @param language If null, use English as default.
     */
    public static String getStringOrMLTextValue(Serializable property, String language) {

        if (property == null) {
            return "";
        }

        if (property instanceof String) {
            return (String) property;
        } else if (property instanceof MLText) {

            MLText propertyMLText = (MLText) property;

            Locale locale =
                    (language != null && isValidLanguageLocale(language))
                            ? new Locale(language)
                            : Locale.ENGLISH;

            return propertyMLText.getValue(locale);
        }

        return "";
    }

    /**
     * Check if the provided language is valid for a Locale. Ex. "english" would be valid, "nn",
     * wouldn't
     */
    private static boolean isValidLanguageLocale(String language) {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (language.equals(locale.toString())) {
                return true;
            }
        }
        return false;
    }

    public static Date convertStringToDate(String string) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted
        // "Z"
        // to
        // indicate
        // UTC,
        // no
        // timezone
        // offset
        df.setTimeZone(tz);
        return df.parse(string);
    }

    public static Date convertStringToSimpleDate(String string) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Quoted "Z" to
        // indicate UTC, no
        // timezone offset
        df.setTimeZone(tz);
        return df.parse(string + "'T'00:00'Z'");
    }

    public static I18nProperty toI18NProperty(org.json.simple.JSONObject titles) {

        I18nProperty result = new I18nProperty();

        for (String code : SupportedLanguages.availableLangCodes) {
            if (titles.containsKey(code) && (titles.get(code) != null && !titles.get(code).equals(""))) {
                result.put(code, String.valueOf(titles.get(code)));
            }
        }
        return result;
    }

    public static NodeRef createNodeRefFromId(String id) {
        return new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    public static NodeRef createArchiveNodeRefFromId(String archiveNodeId) {
        return new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archiveNodeId);
    }

    public static NodeRef createVersionNodeRefFromId(String versionNodeId) {
        return new NodeRef(STORE_REF_WORKSPACE_VERSION2STORE, versionNodeId);
    }

    public static String extractNodeRefId(String nodeRef) {
        int pos = nodeRef.lastIndexOf('/');
        if (pos != -1) {
            return nodeRef.substring(pos + 1);
        }
        throw new IllegalArgumentException("Node reference " + nodeRef + " is invalid.");
    }

    public static InterestGroup toInterestGroup(InterestGroupItem interestGroupItem, String uiLang) {
        InterestGroup result = new InterestGroup();
        result.setId(interestGroupItem.getId());
        result.setName(interestGroupItem.getName());
        result.setDescription(new I18nProperty(uiLang, interestGroupItem.getBestTitle()));
        result.setLogoUrl(interestGroupItem.getLogoRef());
        return result;
    }

    public static io.swagger.model.Category toCategory(Category category, String uiLang) {

        io.swagger.model.Category result = new io.swagger.model.Category();
        result.setId(new NodeRef(category.getNodeRef()).getId());
        result.setName(category.getName());
        String title = category.getTitle();
        if (title == null) {
            title = category.getName();
        }
        if (uiLang == null) {
            uiLang = "en";
        }
        result.setTitle(new I18nProperty(uiLang, title));
        if (category.getLogoRef() != null) {
            result.setLogoRef(category.getLogoRef());
        }
        return result;
    }

    public static io.swagger.model.Header toHeader(Header header) {

        io.swagger.model.Header result = new io.swagger.model.Header();
        result.setId(new NodeRef(header.getNodeRef()).getId());
        result.setName(header.getName());
        String description = header.getDescription();
        if (description == null) {
            description = header.getName();
        }
        result.setDescription(new I18nProperty("en", description));
        return result;
    }

    public static String getValue(FormData.FormField field) throws IOException {
        if (field.getIsFile()) {
            return IOUtils.toString(field.getInputStream());
        } else {
            return field.getValue();
        }
    }
}
