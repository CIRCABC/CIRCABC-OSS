package io.swagger.util.parsers;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSubcategory;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author beaurpi
 */
public class HelpJsonParser {

  private static final String TITLE = "title";
  private static final String CONTENT = "content";
  private static final String HREF = "href";
  private static final String ID = "id";

  private HelpJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  public static HelpCategory parseCategory(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    HelpCategory body = new HelpCategory();
    if (json.get(ID) != null) {
      body.setId(String.valueOf(json.get(ID)));
    }

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject) {
      JSONObject titlesObj = (JSONObject) titles;
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          String titleValue = String.valueOf(titlesObj.get(code));
          if (
            titleValue != null &&
            !titleValue.trim().isEmpty() &&
            !"null".equals(titleValue)
          ) {
            body.getTitle().put(code, titleValue);
          }
        }
      }

      // If no valid titles were found in supported languages, throw exception
      if (body.getTitle().isEmpty()) {
        throw new IllegalArgumentException(
          "Help category must have at least one non-empty title in a supported language. " +
          "Received title object: " +
          titlesObj.toJSONString()
        );
      }
    } else if (titles == null) {
      throw new IllegalArgumentException("Help category title is required");
    }

    return body;
  }

  public static HelpSubcategory parseSubcategory(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    HelpSubcategory body = new HelpSubcategory();
    if (json.get(ID) != null) {
      body.setId(String.valueOf(json.get(ID)));
    }

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject) {
      JSONObject titlesObj = (JSONObject) titles;
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          String titleValue = String.valueOf(titlesObj.get(code));
          if (
            titleValue != null &&
            !titleValue.trim().isEmpty() &&
            !"null".equals(titleValue)
          ) {
            body.getTitle().put(code, titleValue);
          }
        }
      }

      // If no valid titles were found in supported languages, throw exception
      if (body.getTitle().isEmpty()) {
        throw new IllegalArgumentException(
          "Help subcategory must have at least one non-empty title in a supported language. " +
          "Received title object: " +
          titlesObj.toJSONString()
        );
      }
    } else if (titles == null) {
      throw new IllegalArgumentException("Help subcategory title is required");
    }

    return body;
  }

  public static HelpArticle parseArticle(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    HelpArticle body = new HelpArticle();
    if (json.get(ID) != null) {
      body.setId(String.valueOf(json.get(ID)));
    }

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject) {
      JSONObject titlesObj = (JSONObject) titles;
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          String titleValue = String.valueOf(titlesObj.get(code));
          if (
            titleValue != null &&
            !titleValue.trim().isEmpty() &&
            !"null".equals(titleValue)
          ) {
            body.getTitle().put(code, titleValue);
          }
        }
      }

      // If no valid titles were found in supported languages, throw exception
      if (body.getTitle().isEmpty()) {
        throw new IllegalArgumentException(
          "Help article must have at least one non-empty title in a supported language. " +
          "Received title object: " +
          titlesObj.toJSONString()
        );
      }
    } else if (titles == null) {
      throw new IllegalArgumentException("Help article title is required");
    }

    Object contents = json.get(CONTENT);
    if (contents instanceof JSONObject) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (((JSONObject) contents).containsKey(code)) {
          body
            .getContent()
            .put(code, String.valueOf(((JSONObject) contents).get(code)));
        }
      }
    }

    return body;
  }

  public static HelpLink parseLink(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    HelpLink body = new HelpLink();
    if (json.get(ID) != null) {
      body.setId(String.valueOf(json.get(ID)));
    }

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject) {
      JSONObject titlesObj = (JSONObject) titles;
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          String titleValue = String.valueOf(titlesObj.get(code));
          if (
            titleValue != null &&
            !titleValue.trim().isEmpty() &&
            !"null".equals(titleValue)
          ) {
            body.getTitle().put(code, titleValue);
          }
        }
      }

      // If no valid titles were found in supported languages, throw exception
      if (body.getTitle().isEmpty()) {
        throw new IllegalArgumentException(
          "Help link must have at least one non-empty title in a supported language. " +
          "Received title object: " +
          titlesObj.toJSONString()
        );
      }
    } else if (titles == null) {
      throw new IllegalArgumentException("Help link title is required");
    }

    Object href = json.get(HREF);
    if (href != null) {
      body.setHref((String) href);
    }

    return body;
  }
}
