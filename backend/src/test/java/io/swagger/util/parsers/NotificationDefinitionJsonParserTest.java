package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.NotificationDefinition;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NotificationDefinitionJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseJSON_whenProfilesAndUsers_thenParsedCorrectly()
    throws IOException, ParseException {
    String json =
      "{\"profiles\":[{\"notifications\":\"true\",\"profile\":{\"id\":\"p1\",\"groupName\":\"grp1\",\"name\":\"Profile1\"}}]," +
      "\"users\":[{\"notifications\":\"false\",\"user\":{\"userId\":\"user1\"}}]}";
    when(content.getContent()).thenReturn(json);

    NotificationDefinition result = NotificationDefinitionJsonParser.parseJSON(
      req
    );

    assertEquals(1, result.getProfiles().size());
    assertEquals("true", result.getProfiles().get(0).getNotifications());
    assertEquals("p1", result.getProfiles().get(0).getProfile().getId());
    assertEquals(
      "grp1",
      result.getProfiles().get(0).getProfile().getGroupName()
    );
    assertEquals(
      "Profile1",
      result.getProfiles().get(0).getProfile().getName()
    );

    assertEquals(1, result.getUsers().size());
    assertEquals("false", result.getUsers().get(0).getNotifications());
    assertEquals("user1", result.getUsers().get(0).getUser().getUserId());
  }

  @Test
  public void testParseJSON_whenNullProfiles_thenEmptyList()
    throws IOException, ParseException {
    String json =
      "{\"users\":[{\"notifications\":\"true\",\"user\":{\"userId\":\"u1\"}}]}";
    when(content.getContent()).thenReturn(json);

    NotificationDefinition result = NotificationDefinitionJsonParser.parseJSON(
      req
    );

    assertTrue(result.getProfiles().isEmpty());
    assertEquals(1, result.getUsers().size());
  }

  @Test
  public void testParseJSON_whenNullUsers_thenEmptyList()
    throws IOException, ParseException {
    String json =
      "{\"profiles\":[{\"notifications\":\"N\",\"profile\":{\"id\":\"x\",\"groupName\":\"g\",\"name\":\"n\"}}]}";
    when(content.getContent()).thenReturn(json);

    NotificationDefinition result = NotificationDefinitionJsonParser.parseJSON(
      req
    );

    assertTrue(result.getUsers().isEmpty());
    assertEquals(1, result.getProfiles().size());
  }

  @Test
  public void testParseJSON_whenEmptyArrays_thenEmptyLists()
    throws IOException, ParseException {
    String json = "{\"profiles\":[],\"users\":[]}";
    when(content.getContent()).thenReturn(json);

    NotificationDefinition result = NotificationDefinitionJsonParser.parseJSON(
      req
    );

    assertTrue(result.getProfiles().isEmpty());
    assertTrue(result.getUsers().isEmpty());
  }

  @Test(expected = ParseException.class)
  public void testParseJSON_whenInvalidJson_thenParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    NotificationDefinitionJsonParser.parseJSON(req);
  }

  @Test
  public void testConstructor_throwsIllegalStateException() throws Exception {
    java.lang.reflect.Constructor<NotificationDefinitionJsonParser> ctor =
      NotificationDefinitionJsonParser.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    try {
      ctor.newInstance();
      fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }
}
