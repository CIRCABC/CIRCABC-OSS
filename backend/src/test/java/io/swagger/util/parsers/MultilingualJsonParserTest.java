package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.MultilingualAspectMetadata;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class MultilingualJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseAspectMetadata_whenValidJson_thenReturnsParsedMetadata()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn(
      "{\"pivotLang\":\"en\",\"author\":\"John\"}"
    );

    MultilingualAspectMetadata result =
      MultilingualJsonParser.parseAspectMetadata(req);

    assertEquals("en", result.getPivotLang());
    assertEquals("John", result.getAuthor());
  }

  @Test
  public void testParseAspectMetadata_whenFieldsMissing_thenReturnsNullStrings()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{}");

    MultilingualAspectMetadata result =
      MultilingualJsonParser.parseAspectMetadata(req);

    assertEquals("null", result.getPivotLang());
    assertEquals("null", result.getAuthor());
  }

  @Test(expected = ParseException.class)
  public void testParseAspectMetadata_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not json");

    MultilingualJsonParser.parseAspectMetadata(req);
  }

  @Test(expected = IOException.class)
  public void testParseAspectMetadata_whenIOException_thenPropagates()
    throws IOException, ParseException {
    when(content.getContent()).thenThrow(new IOException("read error"));

    MultilingualJsonParser.parseAspectMetadata(req);
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructor_whenInstantiated_thenThrowsIllegalState()
    throws Exception {
    java.lang.reflect.Constructor<MultilingualJsonParser> ctor =
      MultilingualJsonParser.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    try {
      ctor.newInstance();
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw (IllegalStateException) e.getCause();
    }
  }
}
