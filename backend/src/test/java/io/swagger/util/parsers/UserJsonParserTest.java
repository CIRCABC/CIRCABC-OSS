package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.PreferenceConfiguration;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UserJsonParserTest {

  private static final String VALID_JSON =
    "{\"library\":{\"column\":{\"name\":true,\"version\":false,\"modification\":true," +
    "\"creation\":false,\"size\":true,\"expiration\":false,\"status\":true," +
    "\"description\":false,\"author\":true,\"title\":false,\"securityRanking\":true}," +
    "\"listing\":{\"page\":2,\"limit\":25,\"sort\":\"name\"}}}";

  @Test
  public void testParsePreference_whenValidJson_thenAllFieldsParsed() {
    PreferenceConfiguration result = UserJsonParser.parsePreference(VALID_JSON);

    assertTrue(result.getLibrary().getColumn().getName());
    assertFalse(result.getLibrary().getColumn().getVersion());
    assertTrue(result.getLibrary().getColumn().getModification());
    assertFalse(result.getLibrary().getColumn().getCreation());
    assertTrue(result.getLibrary().getColumn().getSize());
    assertFalse(result.getLibrary().getColumn().getExpiration());
    assertTrue(result.getLibrary().getColumn().getStatus());
    assertFalse(result.getLibrary().getColumn().getDescription());
    assertTrue(result.getLibrary().getColumn().getAuthor());
    assertFalse(result.getLibrary().getColumn().getTitle());
    assertTrue(result.getLibrary().getColumn().getSecurityRanking());
    assertEquals(2, (int) result.getLibrary().getListing().getPage());
    assertEquals(25, (int) result.getLibrary().getListing().getLimit());
    assertEquals("name", result.getLibrary().getListing().getSort());
  }

  @Test
  public void testParsePreference_whenInvalidJson_thenReturnsEmptyPreference() {
    PreferenceConfiguration result = UserJsonParser.parsePreference(
      "not valid json"
    );

    assertNotNull(result);
    assertNotNull(result.getLibrary());
  }

  @Test
  public void testParsePreference_whenWebScriptRequest_thenDelegatesToStringParser()
    throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(VALID_JSON);

    PreferenceConfiguration result = UserJsonParser.parsePreference(req);

    assertTrue(result.getLibrary().getColumn().getName());
    assertEquals("name", result.getLibrary().getListing().getSort());
  }

  @Test
  public void testParsePreferenceAsJson_whenValidJson_thenReturnsParsedObject() {
    JSONObject result = UserJsonParser.parsePreferenceAsJson(VALID_JSON);

    assertNotNull(result);
    assertNotNull(result.get("library"));
  }

  @Test
  public void testParsePreferenceAsJson_whenInvalidJson_thenReturnsEmptyObject() {
    JSONObject result = UserJsonParser.parsePreferenceAsJson("invalid");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructor_whenInstantiated_thenThrowsIllegalStateException()
    throws Exception {
    java.lang.reflect.Constructor<UserJsonParser> constructor =
      UserJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw (IllegalStateException) e.getCause();
    }
  }
}
