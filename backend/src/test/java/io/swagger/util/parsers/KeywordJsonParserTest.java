package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.KeywordDefinition;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class KeywordJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseJsonFullKeyword_whenValidJson_thenReturnsKeywordDefinition()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"keyword-123\",\"title\":{\"en\":\"English\",\"fr\":\"French\"}}";
    when(content.getContent()).thenReturn(json);

    KeywordDefinition result = KeywordJsonParser.parseJsonFullKeyword(req);

    assertEquals("keyword-123", result.getId());
    assertNotNull(result.getTitle());
    assertEquals("English", result.getTitle().get("en"));
    assertEquals("French", result.getTitle().get("fr"));
  }

  @Test(expected = ParseException.class)
  public void testParseJsonFullKeyword_whenTitleMissing_thenThrowsParseException()
    throws IOException, ParseException {
    String json = "{\"id\":\"keyword-123\"}";
    when(content.getContent()).thenReturn(json);

    KeywordJsonParser.parseJsonFullKeyword(req);
  }

  @Test(expected = ParseException.class)
  public void testParseJsonFullKeyword_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    KeywordJsonParser.parseJsonFullKeyword(req);
  }

  @Test
  public void testParseJsonFullKeyword_whenEmptyTitle_thenReturnsEmptyI18nProperty()
    throws IOException, ParseException {
    String json = "{\"id\":\"keyword-456\",\"title\":{}}";
    when(content.getContent()).thenReturn(json);

    KeywordDefinition result = KeywordJsonParser.parseJsonFullKeyword(req);

    assertEquals("keyword-456", result.getId());
    assertNotNull(result.getTitle());
    assertTrue(result.getTitle().isEmpty());
  }

  @Test
  public void testParseJsonFullKeyword_whenIdIsNull_thenSetsStringNull()
    throws IOException, ParseException {
    // String.valueOf(null from json.get) returns "null" string, so id is never actually null
    String json = "{\"title\":{\"en\":\"Test\"}}";
    when(content.getContent()).thenReturn(json);

    KeywordDefinition result = KeywordJsonParser.parseJsonFullKeyword(req);

    assertEquals("null", result.getId());
  }
}
