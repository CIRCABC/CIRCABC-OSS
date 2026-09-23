package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpJsonParserTest {

  private WebScriptRequest mockRequest(String json) throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);
    return req;
  }

  @Test
  public void testParseCategory_whenValidJson_thenReturnsCategoryWithIdAndTitle()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"cat-1\",\"title\":{\"en\":\"English Title\",\"fr\":\"Titre Français\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpCategory result = HelpJsonParser.parseCategory(req);

    assertEquals("cat-1", result.getId());
    assertEquals("English Title", result.getTitle().get("en"));
    assertEquals("Titre Français", result.getTitle().get("fr"));
  }

  @Test
  public void testParseCategory_whenNoId_thenIdIsNull()
    throws IOException, ParseException {
    String json = "{\"title\":{\"en\":\"Title\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpCategory result = HelpJsonParser.parseCategory(req);

    assertNull(result.getId());
    assertEquals("Title", result.getTitle().get("en"));
  }

  @Test
  public void testParseCategory_whenNoTitle_thenTitleMapIsEmpty()
    throws IOException, ParseException {
    String json = "{\"id\":\"cat-2\"}";
    WebScriptRequest req = mockRequest(json);

    HelpCategory result = HelpJsonParser.parseCategory(req);

    assertEquals("cat-2", result.getId());
    assertTrue(result.getTitle().isEmpty());
  }

  @Test
  public void testParseArticle_whenValidJson_thenReturnsArticleWithTitleAndContent()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"art-1\",\"title\":{\"en\":\"Article\"},\"content\":{\"en\":\"Body text\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpArticle result = HelpJsonParser.parseArticle(req);

    assertEquals("art-1", result.getId());
    assertEquals("Article", result.getTitle().get("en"));
    assertEquals("Body text", result.getContent().get("en"));
  }

  @Test
  public void testParseArticle_whenNoContent_thenContentMapIsEmpty()
    throws IOException, ParseException {
    String json = "{\"id\":\"art-2\",\"title\":{\"de\":\"Artikel\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpArticle result = HelpJsonParser.parseArticle(req);

    assertEquals("art-2", result.getId());
    assertEquals("Artikel", result.getTitle().get("de"));
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  public void testParseLink_whenValidJson_thenReturnsLinkWithHref()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"link-1\",\"title\":{\"en\":\"Docs\"},\"href\":\"https://example.com\"}";
    WebScriptRequest req = mockRequest(json);

    HelpLink result = HelpJsonParser.parseLink(req);

    assertEquals("link-1", result.getId());
    assertEquals("Docs", result.getTitle().get("en"));
    assertEquals("https://example.com", result.getHref());
  }

  @Test
  public void testParseLink_whenNoHref_thenHrefIsNull()
    throws IOException, ParseException {
    String json = "{\"id\":\"link-2\",\"title\":{\"en\":\"No Link\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpLink result = HelpJsonParser.parseLink(req);

    assertEquals("link-2", result.getId());
    assertNull(result.getHref());
  }

  @Test(expected = ParseException.class)
  public void testParseCategory_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest("not valid json");
    HelpJsonParser.parseCategory(req);
  }

  @Test
  public void testParseCategory_whenUnsupportedLanguage_thenIgnored()
    throws IOException, ParseException {
    String json = "{\"title\":{\"xx\":\"Unknown Lang\",\"en\":\"English\"}}";
    WebScriptRequest req = mockRequest(json);

    HelpCategory result = HelpJsonParser.parseCategory(req);

    assertNull(result.getTitle().get("xx"));
    assertEquals("English", result.getTitle().get("en"));
  }
}
