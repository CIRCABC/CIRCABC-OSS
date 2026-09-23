package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.PermissionDefinition;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PermissionDefinitionJsonParserTest {

  private WebScriptRequest mockRequest(String json) throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);
    return req;
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructor_whenInstantiated_thenThrowsIllegalState()
    throws Exception {
    Constructor<PermissionDefinitionJsonParser> c =
      PermissionDefinitionJsonParser.class.getDeclaredConstructor();
    c.setAccessible(true);
    try {
      c.newInstance();
    } catch (InvocationTargetException e) {
      throw (Exception) e.getCause();
    }
  }

  @Test
  public void testParseJSON_whenFullPayload_thenParsesCorrectly()
    throws IOException, ParseException {
    String json =
      "{" +
      "\"inherited\": false," +
      "\"permissions\": {" +
      "\"profiles\": [{" +
      "\"permission\": \"LibAdmin\"," +
      "\"profile\": {\"id\": \"prof1\", \"groupName\": \"GROUP_PROF1\", \"name\": \"Admin\"}" +
      "}]," +
      "\"users\": [{" +
      "\"permission\": \"LibManageOwn\"," +
      "\"user\": {\"userId\": \"user1\"}" +
      "}]" +
      "}" +
      "}";
    WebScriptRequest req = mockRequest(json);

    PermissionDefinition result = PermissionDefinitionJsonParser.parseJSON(req);

    assertFalse(result.getInherited());
    assertEquals(1, result.getPermissions().getProfiles().size());
    assertEquals(
      "LibAdmin",
      result.getPermissions().getProfiles().get(0).getPermission()
    );
    assertEquals(
      "prof1",
      result.getPermissions().getProfiles().get(0).getProfile().getId()
    );
    assertEquals(
      "GROUP_PROF1",
      result.getPermissions().getProfiles().get(0).getProfile().getGroupName()
    );
    assertEquals(
      "Admin",
      result.getPermissions().getProfiles().get(0).getProfile().getName()
    );
    assertEquals(1, result.getPermissions().getUsers().size());
    assertEquals(
      "LibManageOwn",
      result.getPermissions().getUsers().get(0).getPermission()
    );
    assertEquals(
      "user1",
      result.getPermissions().getUsers().get(0).getUser().getUserId()
    );
  }

  @Test
  public void testParseJSON_whenInheritedNull_thenDefaultsToTrue()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest(
      "{\"permissions\": {\"profiles\": [], \"users\": []}}"
    );

    PermissionDefinition result = PermissionDefinitionJsonParser.parseJSON(req);

    assertTrue(result.getInherited());
  }

  @Test
  public void testParseJSON_whenInheritedTrue_thenReturnsTrue()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest(
      "{\"inherited\": true, \"permissions\": {\"profiles\": [], \"users\": []}}"
    );

    PermissionDefinition result = PermissionDefinitionJsonParser.parseJSON(req);

    assertTrue(result.getInherited());
  }

  @Test
  public void testParseJSON_whenNullProfilesAndUsers_thenEmptyLists()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest(
      "{\"inherited\": true, \"permissions\": {}}"
    );

    PermissionDefinition result = PermissionDefinitionJsonParser.parseJSON(req);

    assertTrue(result.getPermissions().getProfiles().isEmpty());
    assertTrue(result.getPermissions().getUsers().isEmpty());
  }

  @Test
  public void testParseJSON_whenMultipleProfilesAndUsers_thenAllParsed()
    throws IOException, ParseException {
    String json =
      "{" +
      "\"inherited\": false," +
      "\"permissions\": {" +
      "\"profiles\": [" +
      "{\"permission\": \"LibAdmin\", \"profile\": {\"id\": \"p1\", \"groupName\": \"G1\", \"name\": \"N1\"}}," +
      "{\"permission\": \"LibAccess\", \"profile\": {\"id\": \"p2\", \"groupName\": \"G2\", \"name\": \"N2\"}}" +
      "]," +
      "\"users\": [" +
      "{\"permission\": \"LibManageOwn\", \"user\": {\"userId\": \"u1\"}}," +
      "{\"permission\": \"LibFullEdit\", \"user\": {\"userId\": \"u2\"}}" +
      "]" +
      "}" +
      "}";
    WebScriptRequest req = mockRequest(json);

    PermissionDefinition result = PermissionDefinitionJsonParser.parseJSON(req);

    assertEquals(2, result.getPermissions().getProfiles().size());
    assertEquals(2, result.getPermissions().getUsers().size());
    assertEquals(
      "u2",
      result.getPermissions().getUsers().get(1).getUser().getUserId()
    );
  }

  @Test(expected = ParseException.class)
  public void testParseJSON_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest("not valid json");

    PermissionDefinitionJsonParser.parseJSON(req);
  }
}
