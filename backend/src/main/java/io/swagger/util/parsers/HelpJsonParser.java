package io.swagger.util.parsers;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class that parses the JSON request bodies of the "Help" REST endpoints into their
 * corresponding domain models.
 *
 * <p>Each parser reads the raw JSON payload from a {@link WebScriptRequest} and maps the recognised
 * fields onto a {@link HelpCategory}, {@link HelpArticle} or {@link HelpLink}. Multilingual fields
 * (such as {@code title} and {@code content}) are expected to be JSON objects keyed by language
 * code, and only the languages declared in {@link SupportedLanguages#availableLangCodes} are
 * extracted.
 *
 * <p>This class is not meant to be instantiated; all parsing operations are exposed as static
 * methods.
 *
 * @author beaurpi
 */
public class HelpJsonParser {

  /** JSON key holding the (per-language) title object. */
  private static final String TITLE = "title";
  /** JSON key holding the (per-language) content object. */
  private static final String CONTENT = "content";
  /** JSON key holding the hyperlink target of a help link. */
  private static final String HREF = "href";
  /** JSON key holding the identifier of the help entity. */
  private static final String ID = "id";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static helpers
   */
  private HelpJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of a request into a {@link HelpCategory}.
   *
   * <p>Reads the optional {@code id} field and the per-language {@code title} object, keeping only
   * the languages listed in {@link SupportedLanguages#availableLangCodes}.
   *
   * @param req the web script request carrying the JSON payload; must not be {@code null}
   * @return the {@link HelpCategory} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
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
    if (titles instanceof JSONObject titlesObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(titlesObj.get(code)));
        }
      }
    }

    return body;
  }

  /**
   * Parses the JSON body of a request into a {@link HelpArticle}.
   *
   * <p>Reads the optional {@code id} field and the per-language {@code title} and {@code content}
   * objects, keeping only the languages listed in {@link SupportedLanguages#availableLangCodes}.
   *
   * @param req the web script request carrying the JSON payload; must not be {@code null}
   * @return the {@link HelpArticle} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
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
    if (titles instanceof JSONObject titlesObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(titlesObj.get(code)));
        }
      }
    }

    Object contents = json.get(CONTENT);
    if (contents instanceof JSONObject contentsObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (contentsObj.containsKey(code)) {
          body.getContent().put(code, String.valueOf(contentsObj.get(code)));
        }
      }
    }

    return body;
  }

  /**
   * Parses the JSON body of a request into a {@link HelpLink}.
   *
   * <p>Reads the optional {@code id} field, the per-language {@code title} object (keeping only the
   * languages listed in {@link SupportedLanguages#availableLangCodes}) and the {@code href} target.
   *
   * @param req the web script request carrying the JSON payload; must not be {@code null}
   * @return the {@link HelpLink} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
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
    if (titles instanceof JSONObject titlesObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(titlesObj.get(code)));
        }
      }
    }

    Object href = json.get(HREF);
    if (href != null) {
      body.setHref((String) href);
    }

    return body;
  }
}
