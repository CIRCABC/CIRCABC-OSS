package io.swagger.util.parsers;

import io.swagger.model.AdminContactRequest;
import io.swagger.model.Category;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that reads the raw JSON body of an incoming {@link WebScriptRequest}
 * and maps it onto CIRCABC domain objects related to categories.
 *
 * <p>It exposes stateless helpers to build a {@link Category} from a category payload
 * and an {@link AdminContactRequest} from a "contact the administrator" payload. Parsing
 * relies on the {@code json-simple} library and only extracts the fields recognised by
 * CIRCABC, silently ignoring unknown or missing properties.
 *
 * <p>This class is not meant to be instantiated.
 *
 * @author beaurpi
 */
public class CategoryJsonParser {

  /** JSON key holding the message body of an administrator contact request. */
  private static final String CONTENT = "content";
  /** JSON key holding the category identifier. */
  private static final String ID = "ID";
  /** JSON key holding the category name. */
  private static final String NAME = "name";
  /** JSON key indicating whether a copy of the contact message should be sent to the sender. */
  private static final String SEND_COPY = "sendCopy";
  /** JSON key holding the localized titles object (language code to title). */
  private static final String TITLE = "title";
  /** JSON key indicating whether a single contact should be used for the category. */
  private static final String USE_SINGLE_CONTACT = "useSingleContact";
  /** JSON key holding the array of contact email addresses. */
  private static final String CONTACT_EMAILS = "contactEmails";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static helpers
   */
  private CategoryJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given request into a {@link Category}.
   *
   * <p>The following fields are extracted when present: {@code name}, {@code ID},
   * {@code title} (a map of supported language codes to localized titles, filtered by
   * {@link SupportedLanguages#availableLangCodes}), {@code useSingleContact} and
   * {@code contactEmails} (blank entries are discarded). Absent fields are left at their
   * default values on the returned object.
   *
   * @param req the web script request whose content contains the category JSON payload
   * @return a {@link Category} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static Category parseSimpleJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Category body = new Category();
    body.setName(String.valueOf(json.get(NAME)));
    body.setId(String.valueOf(json.get(ID)));

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject titlesObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(titlesObj.get(code)));
        }
      }
    }

    Object useSingleContact = json.get(USE_SINGLE_CONTACT);
    if (useSingleContact != null) {
      body.setUseSingleContact(
        Boolean.parseBoolean(useSingleContact.toString())
      );
    }

    Object contactEmails = json.get(CONTACT_EMAILS);
    if (contactEmails instanceof JSONArray emails) {
      List<String> emailList = new ArrayList<>();
      for (Object email : emails) {
        if (!"".equals(email)) {
          emailList.add(email.toString());
        }
      }
      body.setContactEmails(emailList);
    }

    return body;
  }

  /**
   * Parses the JSON body of the given request into an {@link AdminContactRequest}.
   *
   * <p>The {@code content} field is mapped to the message body and the {@code sendCopy}
   * field to the flag controlling whether a copy is sent back to the sender. Absent fields
   * are left at their default values on the returned object.
   *
   * @param req the web script request whose content contains the contact request JSON payload
   * @return an {@link AdminContactRequest} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static AdminContactRequest parseAdminContactRequest(
    WebScriptRequest req
  ) throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    AdminContactRequest result = new AdminContactRequest();
    Object content = json.get(CONTENT);
    if (content != null) {
      result.setContent(content.toString());
    }

    Object sendCopy = json.get(SEND_COPY);
    if (sendCopy != null) {
      result.setSendCopy(Boolean.parseBoolean(sendCopy.toString()));
    }

    return result;
  }
}
