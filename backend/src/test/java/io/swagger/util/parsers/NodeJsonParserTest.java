package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Node;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodeJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseContentJSON_whenValidJson_thenReturnsPopulatedNode()
    throws ParseException {
    String json =
      "{\"name\":\"test.pdf\"," +
      "\"title\":{\"en\":\"English Title\",\"fr\":\"Titre Français\"}," +
      "\"description\":{\"en\":\"Desc\"}," +
      "\"properties\":{\"issue_date\":\"2024-01-01\",\"expiration_date\":\"2025-01-01\"," +
      "\"reference\":\"REF-001\",\"author\":\"John\",\"mimetype\":\"application/pdf\"," +
      "\"encoding\":\"UTF-8\",\"status\":\"DRAFT\",\"security\":\"PUBLIC\",\"url\":\"http://example.com\"}}";

    Node result = NodeJsonParser.parseContentJSON(json);

    assertEquals("test.pdf", result.getName());
    assertEquals("English Title", result.getTitle().get("en"));
    assertEquals("Titre Français", result.getTitle().get("fr"));
    assertEquals("Desc", result.getDescription().get("en"));
    assertEquals("2024-01-01", result.getProperties().get("issue_date"));
    assertEquals("2025-01-01", result.getProperties().get("expiration_date"));
    assertEquals("REF-001", result.getProperties().get("reference"));
    assertEquals("John", result.getProperties().get("author"));
    assertEquals("application/pdf", result.getProperties().get("mimetype"));
    assertEquals("UTF-8", result.getProperties().get("encoding"));
    assertEquals("DRAFT", result.getProperties().get("status"));
    assertEquals("PUBLIC", result.getProperties().get("security"));
    assertEquals("http://example.com", result.getProperties().get("url"));
  }

  @Test
  public void testParseContentJSON_whenDynAttrPresent_thenIncludesInProperties()
    throws ParseException {
    String json =
      "{\"name\":\"doc.pdf\"," +
      "\"title\":{}," +
      "\"description\":{}," +
      "\"properties\":{\"issue_date\":\"d\",\"expiration_date\":\"d\"," +
      "\"reference\":\"r\",\"author\":\"a\",\"status\":\"s\",\"security\":\"s\",\"url\":\"u\"," +
      "\"dynAttr1\":\"value1\",\"dynAttr5\":\"value5\"}}";

    Node result = NodeJsonParser.parseContentJSON(json);

    assertEquals("value1", result.getProperties().get("dynAttr1"));
    assertEquals("value5", result.getProperties().get("dynAttr5"));
    assertNull(result.getProperties().get("dynAttr2"));
  }

  @Test(expected = ParseException.class)
  public void testParseContentJSON_whenInvalidJson_thenThrowsParseException()
    throws ParseException {
    NodeJsonParser.parseContentJSON("not valid json");
  }

  @Test
  public void testParsePostJSON_whenMessagePresent_thenIncludesMessage()
    throws ParseException {
    String json =
      "{\"name\":\"post\"," +
      "\"title\":{}," +
      "\"description\":{}," +
      "\"properties\":{\"issue_date\":\"d\",\"expiration_date\":\"d\"," +
      "\"reference\":\"r\",\"author\":\"a\",\"status\":\"s\",\"security\":\"s\",\"url\":\"u\"," +
      "\"message\":\"Hello World\"}}";

    Node result = NodeJsonParser.parsePostJSON(json);

    assertEquals("post", result.getName());
    assertEquals("Hello World", result.getProperties().get("message"));
  }

  @Test
  public void testParsePostJSON_whenMessageAbsent_thenNoMessageProperty()
    throws ParseException {
    String json =
      "{\"name\":\"post\"," +
      "\"title\":{}," +
      "\"description\":{}," +
      "\"properties\":{\"issue_date\":\"d\",\"expiration_date\":\"d\"," +
      "\"reference\":\"r\",\"author\":\"a\",\"status\":\"s\",\"security\":\"s\",\"url\":\"u\"}}";

    Node result = NodeJsonParser.parsePostJSON(json);

    assertNull(result.getProperties().get("message"));
  }

  @Test
  public void testParseBasicJSON_whenValidJson_thenReturnsNodeWithName()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{\"name\":\"basic-node\"}");

    Node result = NodeJsonParser.parseBasicJSON(req);

    assertEquals("basic-node", result.getName());
  }

  @Test
  public void testParseSimpleJSON_whenTitleAndDescription_thenPopulatesI18n()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"simple\"," +
      "\"title\":{\"en\":\"Title EN\",\"de\":\"Titel DE\"}," +
      "\"description\":{\"en\":\"Desc EN\"}}";
    when(content.getContent()).thenReturn(json);

    Node result = NodeJsonParser.parseSimpleJSON(req);

    assertEquals("simple", result.getName());
    assertEquals("Title EN", result.getTitle().get("en"));
    assertEquals("Titel DE", result.getTitle().get("de"));
    assertEquals("Desc EN", result.getDescription().get("en"));
  }

  @Test
  public void testParseSimpleJSON_whenNoTitleOrDescription_thenEmptyMaps()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{\"name\":\"minimal\"}");

    Node result = NodeJsonParser.parseSimpleJSON(req);

    assertEquals("minimal", result.getName());
    assertTrue(result.getTitle().isEmpty());
    assertTrue(result.getDescription().isEmpty());
  }

  @Test
  public void testParseUrlBasicJSON_whenValidJson_thenReturnsNodeWithUrl()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"link-node\",\"properties\":{\"url\":\"http://test.com\"}}";
    when(content.getContent()).thenReturn(json);

    Node result = NodeJsonParser.parseUrlBasicJSON(req);

    assertEquals("link-node", result.getName());
    assertEquals("http://test.com", result.getProperties().get("url"));
  }

  @Test(expected = ParseException.class)
  public void testParseBasicJSON_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("invalid");

    NodeJsonParser.parseBasicJSON(req);
  }
}
