package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.UserMembershipsExpirationRequest;
import io.swagger.model.UserRevocationRequest;
import java.io.IOException;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HistoryJsonParserTest {

  private WebScriptRequest mockRequest(String body) throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(body);
    return req;
  }

  @Test
  public void testParseRevocationRequest_whenAllFieldsPresent_thenParsedCorrectly()
    throws Exception {
    String json =
      "{\"id\":\"42\",\"action\":\"revoke\",\"userIds\":[\"user1\",\"user2\"]," +
      "\"revocationDate\":\"2026-01-15T10:00:00.000Z\",\"requester\":\"admin\"," +
      "\"requestState\":\"1\"}";

    WebScriptRequest req = mockRequest(json);
    UserRevocationRequest result = HistoryJsonParser.parseRevocationRequest(
      req
    );

    assertEquals(Integer.valueOf(42), result.getId());
    assertEquals("revoke", result.getAction());
    assertEquals(2, result.getUserIds().size());
    assertEquals("user1", result.getUserIds().get(0));
    assertEquals("user2", result.getUserIds().get(1));
    assertNotNull(result.getRevocationDate());
    assertEquals("admin", result.getRequester());
    assertEquals(Integer.valueOf(1), result.getRequestState());
  }

  @Test
  public void testParseRevocationRequest_whenOptionalFieldsNull_thenFieldsRemainNull()
    throws Exception {
    String json = "{\"userIds\":[\"u1\"]}";

    WebScriptRequest req = mockRequest(json);
    UserRevocationRequest result = HistoryJsonParser.parseRevocationRequest(
      req
    );

    assertNull(result.getId());
    assertNull(result.getAction());
    assertNull(result.getRevocationDate());
    assertNull(result.getRequester());
    assertNull(result.getRequestState());
    assertEquals(1, result.getUserIds().size());
  }

  @Test(expected = ParseException.class)
  public void testParseRevocationRequest_whenInvalidJson_thenThrowsParseException()
    throws Exception {
    WebScriptRequest req = mockRequest("not valid json");
    HistoryJsonParser.parseRevocationRequest(req);
  }

  @Test
  public void testParseUserMembershipsExpirationRequests_whenValidArray_thenParsedCorrectly()
    throws Exception {
    String json =
      "[{\"userId\":\"john\",\"expirationDate\":\"2026-06-01T00:00:00.000Z\"," +
      "\"memberships\":[{\"profile\":{\"id\":\"prof1\",\"groupName\":\"GROUP_prof1\"}," +
      "\"interestGroup\":{\"id\":\"ig1\"}}]}]";

    WebScriptRequest req = mockRequest(json);
    List<UserMembershipsExpirationRequest> result =
      HistoryJsonParser.parseUserMembershipsExpirationRequests(req);

    assertEquals(1, result.size());
    assertEquals("john", result.get(0).getUserId());
    assertNotNull(result.get(0).getExpirationDate());
    assertEquals(1, result.get(0).getMemberships().size());
    assertEquals(
      "prof1",
      result.get(0).getMemberships().get(0).getProfile().getId()
    );
    assertEquals(
      "GROUP_prof1",
      result.get(0).getMemberships().get(0).getProfile().getGroupName()
    );
    assertEquals(
      "ig1",
      result.get(0).getMemberships().get(0).getInterestGroup().getId()
    );
  }

  @Test
  public void testParseUserMembershipsExpirationRequests_whenEmptyArray_thenReturnsEmptyList()
    throws Exception {
    WebScriptRequest req = mockRequest("[]");
    List<UserMembershipsExpirationRequest> result =
      HistoryJsonParser.parseUserMembershipsExpirationRequests(req);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseUserMembershipsExpirationRequests_whenEmptyMemberships_thenReturnsEmptyMembershipsList()
    throws Exception {
    String json =
      "[{\"userId\":\"jane\",\"expirationDate\":\"2026-06-01T00:00:00.000Z\"," +
      "\"memberships\":[]}]";

    WebScriptRequest req = mockRequest(json);
    List<UserMembershipsExpirationRequest> result =
      HistoryJsonParser.parseUserMembershipsExpirationRequests(req);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getMemberships().isEmpty());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testConstructor_whenInstantiated_thenThrowsException()
    throws Exception {
    java.lang.reflect.Constructor<HistoryJsonParser> constructor =
      HistoryJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getCause() instanceof IllegalStateException) {
        throw new UnsupportedOperationException(e.getCause());
      }
      throw new RuntimeException(e);
    }
  }
}
