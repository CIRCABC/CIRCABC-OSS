package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.AppMessage;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessageJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParse_whenAllFieldsPresent_thenReturnsFullAppMessage()
    throws IOException, ParseException {
    String json =
      "{\"id\":42,\"content\":\"Hello\",\"dateClosure\":\"2026-05-04T10:00:00.000Z\",\"level\":\"warning\",\"enabled\":true,\"displayTime\":30}";
    when(content.getContent()).thenReturn(json);

    AppMessage result = AppMessageJsonParser.parse(req);

    assertEquals(Integer.valueOf(42), result.getId());
    assertEquals("Hello", result.getContent());
    assertNotNull(result.getDateClosure());
    assertEquals("warning", result.getLevel());
    assertTrue(result.getEnabled());
    assertEquals(Integer.valueOf(30), result.getDisplayTime());
  }

  @Test
  public void testParse_whenOptionalFieldsMissing_thenUsesDefaults()
    throws IOException, ParseException {
    String json = "{\"content\":\"Test\"}";
    when(content.getContent()).thenReturn(json);

    AppMessage result = AppMessageJsonParser.parse(req);

    assertNull(result.getId());
    assertEquals("Test", result.getContent());
    assertNull(result.getDateClosure());
    assertEquals("info", result.getLevel());
    assertFalse(result.getEnabled());
    assertEquals(Integer.valueOf(15), result.getDisplayTime());
  }

  @Test
  public void testParse_whenIdIsEmptyString_thenIdRemainsNull()
    throws IOException, ParseException {
    String json = "{\"id\":\"\"}";
    when(content.getContent()).thenReturn(json);

    AppMessage result = AppMessageJsonParser.parse(req);

    assertNull(result.getId());
  }

  @Test
  public void testParse_whenDateClosureIsEmptyString_thenDateRemainsNull()
    throws IOException, ParseException {
    String json = "{\"dateClosure\":\"\"}";
    when(content.getContent()).thenReturn(json);

    AppMessage result = AppMessageJsonParser.parse(req);

    assertNull(result.getDateClosure());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not json");

    AppMessageJsonParser.parse(req);
  }

  @Test
  public void testParseBoolean_whenBodyIsTrue_thenReturnsTrue()
    throws IOException {
    when(content.getContent()).thenReturn("true");

    Boolean result = AppMessageJsonParser.parseBoolean(req);

    assertTrue(result);
  }

  @Test
  public void testParseBoolean_whenBodyIsFalse_thenReturnsFalse()
    throws IOException {
    when(content.getContent()).thenReturn("false");

    Boolean result = AppMessageJsonParser.parseBoolean(req);

    assertFalse(result);
  }

  @Test
  public void testParseBoolean_whenBodyIsNull_thenReturnsNull()
    throws IOException {
    when(content.getContent()).thenReturn(null);

    Boolean result = AppMessageJsonParser.parseBoolean(req);

    assertNull(result);
  }
}
