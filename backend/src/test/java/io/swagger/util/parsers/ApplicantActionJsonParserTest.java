package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.ApplicantAction;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ApplicantActionJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseJSON_whenValidJson_thenReturnsApplicantAction()
    throws IOException, ParseException {
    String json =
      "{\"username\":\"john\",\"action\":\"approve\",\"message\":\"Welcome\"}";
    when(content.getContent()).thenReturn(json);

    ApplicantAction result = ApplicantActionJsonParser.parseJSON(req);

    assertEquals("john", result.getUsername());
    assertEquals("approve", result.getAction());
    assertEquals("Welcome", result.getMessage());
  }

  @Test
  public void testParseJSON_whenMissingFields_thenReturnsNullStrings()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{}");

    ApplicantAction result = ApplicantActionJsonParser.parseJSON(req);

    assertEquals("null", result.getUsername());
    assertEquals("null", result.getAction());
    assertEquals("null", result.getMessage());
  }

  @Test(expected = ParseException.class)
  public void testParseJSON_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not json");

    ApplicantActionJsonParser.parseJSON(req);
  }

  @Test(expected = IOException.class)
  public void testParseJSON_whenContentThrowsIOException_thenPropagates()
    throws IOException, ParseException {
    when(content.getContent()).thenThrow(new IOException("read error"));

    ApplicantActionJsonParser.parseJSON(req);
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructor_throwsIllegalStateException() throws Exception {
    Constructor<ApplicantActionJsonParser> constructor =
      ApplicantActionJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      throw (IllegalStateException) e.getCause();
    }
  }
}
