package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.*;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class InterestGroupJsonParserTest {

  private WebScriptRequest mockRequest(String json) throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);
    return req;
  }

  // --- parsePartialJSON ---

  @Test
  public void testParsePartialJSON_whenValidJson_thenReturnsInterestGroup()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"TestGroup\",\"id\":\"ig-1\",\"isPublic\":true,\"isRegistered\":false,\"allowApply\":true," +
      "\"title\":{\"en\":\"English Title\",\"fr\":\"Titre\"},\"description\":{\"en\":\"Desc\"},\"contact\":{\"en\":\"contact@test.com\"}}";
    WebScriptRequest req = mockRequest(json);

    InterestGroup result = InterestGroupJsonParser.parsePartialJSON(req);

    assertEquals("TestGroup", result.getName());
    assertEquals("ig-1", result.getId());
    assertTrue(result.getIsPublic());
    assertFalse(result.getIsRegistered());
    assertTrue(result.getAllowApply());
    assertEquals("English Title", result.getTitle().get("en"));
    assertEquals("Titre", result.getTitle().get("fr"));
    assertEquals("Desc", result.getDescription().get("en"));
    assertEquals("contact@test.com", result.getContact().get("en"));
  }

  @Test
  public void testParsePartialJSON_whenNoTitleOrDescription_thenMapsAreEmpty()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"G\",\"id\":\"1\",\"isPublic\":null,\"isRegistered\":null,\"allowApply\":null}";
    WebScriptRequest req = mockRequest(json);

    InterestGroup result = InterestGroupJsonParser.parsePartialJSON(req);

    assertEquals("G", result.getName());
    assertTrue(result.getTitle().isEmpty());
    assertTrue(result.getDescription().isEmpty());
  }

  @Test(expected = ParseException.class)
  public void testParsePartialJSON_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest("not valid json");
    InterestGroupJsonParser.parsePartialJSON(req);
  }

  // --- parseNewGroup ---

  @Test
  public void testParseNewGroup_whenValidJson_thenReturnsPostModel()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"NewIG\",\"title\":{\"en\":\"Title\"},\"description\":{\"en\":\"Desc\"}," +
      "\"contact\":{\"en\":\"c\"},\"leaders\":[\"user1\",\"user2\"],\"notify\":\"true\",\"notifyText\":{\"en\":\"Hello\"}}";
    WebScriptRequest req = mockRequest(json);

    InterestGroupPostModel result = InterestGroupJsonParser.parseNewGroup(req);

    assertEquals("NewIG", result.getName());
    assertEquals("Title", result.getTitle().get("en"));
    assertEquals("Desc", result.getDescription().get("en"));
    assertEquals(2, result.getLeaders().size());
    assertEquals("user1", result.getLeaders().get(0));
    assertEquals("user2", result.getLeaders().get(1));
    assertTrue(result.getNotify());
    assertEquals("Hello", result.getNotifyText().get("en"));
  }

  @Test
  public void testParseNewGroup_whenNoLeaders_thenLeadersListEmpty()
    throws IOException, ParseException {
    String json =
      "{\"name\":\"IG\",\"title\":{},\"description\":{},\"contact\":{},\"notify\":\"false\",\"notifyText\":{}}";
    WebScriptRequest req = mockRequest(json);

    InterestGroupPostModel result = InterestGroupJsonParser.parseNewGroup(req);

    assertTrue(result.getLeaders().isEmpty());
    assertFalse(result.getNotify());
  }

  // --- parseGroupConfiguration ---

  @Test
  public void testParseGroupConfiguration_whenNewsgroupsPresent_thenParsesFlags()
    throws IOException, ParseException {
    String json =
      "{\"newsgroups\":{\"enableFlagNewTopic\":true,\"enableFlagNewForum\":false,\"ageFlagNewForum\":14,\"ageFlagNewTopic\":3}}";
    WebScriptRequest req = mockRequest(json);

    GroupConfiguration result = InterestGroupJsonParser.parseGroupConfiguration(
      req
    );

    assertNotNull(result.getNewsgroups());
    assertTrue(result.getNewsgroups().getEnableFlagNewTopic());
    assertFalse(result.getNewsgroups().getEnableFlagNewForum());
    assertEquals(
      Integer.valueOf(14),
      result.getNewsgroups().getAgeFlagNewForum()
    );
    assertEquals(
      Integer.valueOf(3),
      result.getNewsgroups().getAgeFlagNewTopic()
    );
  }

  @Test
  public void testParseGroupConfiguration_whenNewsgroupsNull_thenNewsgroupsIsNull()
    throws IOException, ParseException {
    String json = "{}";
    WebScriptRequest req = mockRequest(json);

    GroupConfiguration result = InterestGroupJsonParser.parseGroupConfiguration(
      req
    );

    assertNull(result.getNewsgroups());
  }

  @Test
  public void testParseGroupConfiguration_whenFlagsNull_thenUsesDefaults()
    throws IOException, ParseException {
    String json = "{\"newsgroups\":{}}";
    WebScriptRequest req = mockRequest(json);

    GroupConfiguration result = InterestGroupJsonParser.parseGroupConfiguration(
      req
    );

    assertNotNull(result.getNewsgroups());
    assertFalse(result.getNewsgroups().getEnableFlagNewTopic());
    assertFalse(result.getNewsgroups().getEnableFlagNewForum());
    assertEquals(
      Integer.valueOf(7),
      result.getNewsgroups().getAgeFlagNewForum()
    );
    assertEquals(
      Integer.valueOf(7),
      result.getNewsgroups().getAgeFlagNewTopic()
    );
  }

  // --- parseGroupCreationRequestApproval ---

  @Test
  public void testParseGroupCreationRequestApproval_whenValidJson_thenParsesFields()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"42\",\"agreement\":\"1\",\"argument\":\"Approved\"}";
    WebScriptRequest req = mockRequest(json);

    GroupCreationRequestApproval result =
      InterestGroupJsonParser.parseGroupCreationRequestApproval(req);

    assertEquals(42L, result.getId());
    assertEquals(1, result.getAgreement());
    assertEquals("Approved", result.getArgument());
  }

  @Test
  public void testParseGroupCreationRequestApproval_whenIdNull_thenIdIsZero()
    throws IOException, ParseException {
    String json = "{\"agreement\":\"2\",\"argument\":\"Rejected\"}";
    WebScriptRequest req = mockRequest(json);

    GroupCreationRequestApproval result =
      InterestGroupJsonParser.parseGroupCreationRequestApproval(req);

    assertEquals(0L, result.getId());
    assertEquals(2, result.getAgreement());
    assertEquals("Rejected", result.getArgument());
  }

  // --- parseGroupDeletionRequestApproval ---

  @Test
  public void testParseGroupDeletionRequestApproval_whenValidJson_thenParsesFields()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"99\",\"agreement\":\"0\",\"argument\":\"No reason\"}";
    WebScriptRequest req = mockRequest(json);

    GroupDeletionRequestApproval result =
      InterestGroupJsonParser.parseGroupDeletionRequestApproval(req);

    assertEquals(99L, result.getId());
    assertEquals(0, result.getAgreement());
    assertEquals("No reason", result.getArgument());
  }

  // --- parseGroupDeletionRequest ---

  @Test
  public void testParseGroupDeletionRequest_whenValidJson_thenParsesFields()
    throws IOException, ParseException {
    String json = "{\"justification\":\"No longer needed\"}";
    WebScriptRequest req = mockRequest(json);

    GroupDeletionRequest result =
      InterestGroupJsonParser.parseGroupDeletionRequest(
        req,
        "admin",
        "cat-ref-1",
        "group-123"
      );

    assertEquals("admin", result.getFrom().getUserId());
    assertEquals("cat-ref-1", result.getCategoryRef());
    assertEquals("group-123", result.getGroupId());
    assertEquals("No longer needed", result.getJustification());
    assertNotNull(result.getRequestDate());
  }

  @Test
  public void testParseGroupDeletionRequest_whenNoJustification_thenEmptyString()
    throws IOException, ParseException {
    String json = "{}";
    WebScriptRequest req = mockRequest(json);

    GroupDeletionRequest result =
      InterestGroupJsonParser.parseGroupDeletionRequest(
        req,
        "user1",
        "cat-ref",
        "grp-1"
      );

    assertEquals("", result.getJustification());
  }
}
