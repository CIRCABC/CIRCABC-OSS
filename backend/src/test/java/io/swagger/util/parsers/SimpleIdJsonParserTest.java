package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.SimpleId;
import java.io.IOException;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SimpleIdJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParse_whenValidJson_thenReturnsSimpleId() throws Exception {
    when(content.getContent()).thenReturn("{\"id\":\"abc-123\"}");

    SimpleId result = SimpleIdJsonParser.parse(req);

    assertEquals("abc-123", result.getId());
  }

  @Test
  public void testParse_whenIdIsNull_thenReturnsSimpleIdWithNullId()
    throws Exception {
    when(content.getContent()).thenReturn("{\"id\":null}");

    SimpleId result = SimpleIdJsonParser.parse(req);

    assertNull(result.getId());
  }

  @Test
  public void testParse_whenIdKeyMissing_thenReturnsSimpleIdWithNullId()
    throws Exception {
    when(content.getContent()).thenReturn("{\"other\":\"value\"}");

    SimpleId result = SimpleIdJsonParser.parse(req);

    assertNull(result.getId());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenInvalidJson_thenThrowsParseException()
    throws Exception {
    when(content.getContent()).thenReturn("not json");

    SimpleIdJsonParser.parse(req);
  }

  @Test(expected = IOException.class)
  public void testParse_whenIOException_thenThrows() throws Exception {
    when(content.getContent()).thenThrow(new IOException("read error"));

    SimpleIdJsonParser.parse(req);
  }

  @Test
  public void testParseListOfId_whenValidArray_thenReturnsList()
    throws Exception {
    when(content.getContent()).thenReturn("[\"id1\", \"id2\", \"id3\"]");

    List<String> result = SimpleIdJsonParser.parseListOfId(req);

    assertEquals(3, result.size());
    assertEquals("id1", result.get(0));
    assertEquals("id2", result.get(1));
    assertEquals("id3", result.get(2));
  }

  @Test
  public void testParseListOfId_whenEmptyArray_thenReturnsEmptyList()
    throws Exception {
    when(content.getContent()).thenReturn("[]");

    List<String> result = SimpleIdJsonParser.parseListOfId(req);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseListOfId_whenArrayContainsNull_thenSkipsNull()
    throws Exception {
    when(content.getContent()).thenReturn("[\"id1\", null, \"id3\"]");

    List<String> result = SimpleIdJsonParser.parseListOfId(req);

    assertEquals(2, result.size());
    assertEquals("id1", result.get(0));
    assertEquals("id3", result.get(1));
  }

  @Test
  public void testParseListOfId_whenValuesHaveWhitespace_thenTrims()
    throws Exception {
    when(content.getContent()).thenReturn("[\" id1 \", \"id2 \"]");

    List<String> result = SimpleIdJsonParser.parseListOfId(req);

    assertEquals("id1", result.get(0));
    assertEquals("id2", result.get(1));
  }

  @Test(expected = ParseException.class)
  public void testParseListOfId_whenInvalidJson_thenThrowsParseException()
    throws Exception {
    when(content.getContent()).thenReturn("not json");

    SimpleIdJsonParser.parseListOfId(req);
  }
}
