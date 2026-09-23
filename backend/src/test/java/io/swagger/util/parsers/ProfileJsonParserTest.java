package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Profile;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProfileJsonParserTest {

  private WebScriptRequest req;
  private org.springframework.extensions.surf.util.Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(org.springframework.extensions.surf.util.Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParsePartial_whenValidJson_thenReturnsProfile()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"abc-123\",\"title\":{\"en\":\"English Title\"},\"exported\":true,\"imported\":false,\"permissions\":{\"library\":\"LibAdmin\",\"information\":\"InfManage\"}}";
    when(content.getContent()).thenReturn(json);

    Profile result = ProfileJsonParser.parsePartial(req);

    assertEquals("abc-123", result.getId());
    assertEquals("English Title", result.getTitle().get("en"));
    assertTrue(result.getExported());
    assertFalse(result.getImported());
    assertEquals("LibAdmin", result.getPermissions().get("library"));
    assertEquals("InfManage", result.getPermissions().get("information"));
  }

  @Test
  public void testParsePartial_whenNoId_thenIdRemainsDefault()
    throws IOException, ParseException {
    String json = "{\"title\":{\"fr\":\"Titre\"}}";
    when(content.getContent()).thenReturn(json);

    Profile result = ProfileJsonParser.parsePartial(req);

    assertEquals("", result.getId());
    assertEquals("Titre", result.getTitle().get("fr"));
  }

  @Test(expected = ParseException.class)
  public void testParsePartial_whenTitleMissing_thenThrowsParseException()
    throws IOException, ParseException {
    String json = "{\"id\":\"abc-123\"}";
    when(content.getContent()).thenReturn(json);

    ProfileJsonParser.parsePartial(req);
  }

  @Test
  public void testParsePartial_whenNoPermissions_thenPermissionsEmpty()
    throws IOException, ParseException {
    String json = "{\"title\":{\"en\":\"Test\"}}";
    when(content.getContent()).thenReturn(json);

    Profile result = ProfileJsonParser.parsePartial(req);

    assertTrue(result.getPermissions().isEmpty());
  }

  @Test
  public void testParsePartial_whenExportedImportedNull_thenFieldsNull()
    throws IOException, ParseException {
    String json = "{\"title\":{\"en\":\"Test\"}}";
    when(content.getContent()).thenReturn(json);

    Profile result = ProfileJsonParser.parsePartial(req);

    assertNull(result.getExported());
    assertNull(result.getImported());
  }

  @Test
  public void testConstructor_thenThrowsIllegalStateException()
    throws Exception {
    java.lang.reflect.Constructor<ProfileJsonParser> constructor =
      ProfileJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }
}
