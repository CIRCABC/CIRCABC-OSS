package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.InformationPage;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class InformationJsonParserTest {

  private WebScriptRequest req;
  private org.springframework.extensions.surf.util.Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(org.springframework.extensions.surf.util.Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParse_whenBothFieldsPresent_thenReturnsPopulatedPage()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn(
      "{\"adapt\":true,\"displayOldInformation\":false}"
    );

    InformationPage result = InformationJsonParser.parse(req);

    assertEquals(Boolean.TRUE, result.getAdapt());
    assertEquals(Boolean.FALSE, result.getDisplayOldInformation());
  }

  @Test
  public void testParse_whenFieldsAbsent_thenReturnsDefaults()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{}");

    InformationPage result = InformationJsonParser.parse(req);

    assertNull(result.getAdapt());
    assertEquals(Boolean.FALSE, result.getDisplayOldInformation());
  }

  @Test
  public void testParse_whenOnlyAdaptPresent_thenOnlyAdaptSet()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{\"adapt\":false}");

    InformationPage result = InformationJsonParser.parse(req);

    assertEquals(Boolean.FALSE, result.getAdapt());
    assertEquals(Boolean.FALSE, result.getDisplayOldInformation());
  }

  @Test
  public void testParse_whenOnlyDisplayOldInformationPresent_thenOnlyThatSet()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{\"displayOldInformation\":true}");

    InformationPage result = InformationJsonParser.parse(req);

    assertNull(result.getAdapt());
    assertEquals(Boolean.TRUE, result.getDisplayOldInformation());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    InformationJsonParser.parse(req);
  }

  @Test(expected = IOException.class)
  public void testParse_whenContentThrowsIOException_thenPropagates()
    throws IOException, ParseException {
    when(content.getContent()).thenThrow(new IOException("read error"));

    InformationJsonParser.parse(req);
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructor_throwsIllegalStateException() throws Exception {
    java.lang.reflect.Constructor<InformationJsonParser> ctor =
      InformationJsonParser.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    try {
      ctor.newInstance();
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw (IllegalStateException) e.getCause();
    }
  }
}
