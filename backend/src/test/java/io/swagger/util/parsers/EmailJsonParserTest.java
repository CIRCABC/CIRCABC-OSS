package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.EmailDefinition;
import io.swagger.model.Profile;
import io.swagger.model.db.DistributionEmailDAO;
import java.io.IOException;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EmailJsonParserTest {

  private WebScriptRequest mockRequest(String body) throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(body);
    return req;
  }

  @Test
  public void testParse_whenFullPayload_thenAllFieldsParsed()
    throws IOException, ParseException {
    String json =
      "{\"subject\":\"Hello\",\"content\":\"Body text\"," +
      "\"profiles\":[{\"id\":\"p1\",\"groupName\":\"grp\",\"name\":\"Admin\"}]," +
      "\"users\":[{\"userId\":\"u1\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@example.com\"}]," +
      "\"attachments\":[\"node1\",\"node2\"]}";

    WebScriptRequest req = mockRequest(json);
    EmailDefinition result = EmailJsonParser.parse(req);

    assertEquals("Hello", result.getSubject());
    assertEquals("Body text", result.getContent());
    assertEquals(1, result.getProfiles().size());
    assertEquals("p1", result.getProfiles().get(0).getId());
    assertEquals("grp", result.getProfiles().get(0).getGroupName());
    assertEquals("Admin", result.getProfiles().get(0).getName());
    assertEquals(1, result.getUsers().size());
    assertEquals("u1", result.getUsers().get(0).getUserId());
    assertEquals("John", result.getUsers().get(0).getFirstname());
    assertEquals("Doe", result.getUsers().get(0).getLastname());
    assertEquals("john@example.com", result.getUsers().get(0).getEmail());
    assertEquals(2, result.getAttachments().size());
    assertEquals("node1", result.getAttachments().get(0));
  }

  @Test
  public void testParse_whenNullOptionalArrays_thenEmptyLists()
    throws IOException, ParseException {
    String json = "{\"subject\":\"Sub\",\"content\":\"Con\"}";

    WebScriptRequest req = mockRequest(json);
    EmailDefinition result = EmailJsonParser.parse(req);

    assertEquals("Sub", result.getSubject());
    assertEquals("Con", result.getContent());
    assertTrue(result.getProfiles().isEmpty());
    assertTrue(result.getUsers().isEmpty());
    assertTrue(result.getAttachments().isEmpty());
  }

  @Test
  public void testParse_whenInvalidEmail_thenEmailIsNull()
    throws IOException, ParseException {
    String json =
      "{\"subject\":\"S\",\"content\":\"C\"," +
      "\"users\":[{\"userId\":\"u1\",\"firstname\":\"A\",\"lastname\":\"B\",\"email\":\"not-an-email\"}]}";

    WebScriptRequest req = mockRequest(json);
    EmailDefinition result = EmailJsonParser.parse(req);

    assertEquals(1, result.getUsers().size());
    assertNull(result.getUsers().get(0).getEmail());
  }

  @Test
  public void testParseProfile_whenValidJson_thenProfileReturned() {
    JSONObject obj = new JSONObject();
    obj.put("id", "prof1");
    obj.put("groupName", "myGroup");
    obj.put("name", "Editor");

    Profile profile = EmailJsonParser.parseProfile(obj);

    assertEquals("prof1", profile.getId());
    assertEquals("myGroup", profile.getGroupName());
    assertEquals("Editor", profile.getName());
  }

  @Test
  public void testParseDistributionEmails_whenValidArray_thenParsed()
    throws IOException, ParseException {
    String json =
      "[{\"id\":1,\"emailAddress\":\"test@example.com\"}," +
      "{\"id\":2,\"emailAddress\":\"other@example.com\"}]";

    WebScriptRequest req = mockRequest(json);
    List<DistributionEmailDAO> result = EmailJsonParser.parseDistributionEmails(
      req
    );

    assertEquals(2, result.size());
    assertEquals(Integer.valueOf(1), result.get(0).getId());
    assertEquals("test@example.com", result.get(0).getEmailAddress());
    assertEquals(Integer.valueOf(2), result.get(1).getId());
    assertEquals("other@example.com", result.get(1).getEmailAddress());
  }

  @Test
  public void testParseDistributionEmails_whenInvalidEmail_thenSkipped()
    throws IOException, ParseException {
    String json = "[{\"id\":1,\"emailAddress\":\"bad email\"}]";

    WebScriptRequest req = mockRequest(json);
    List<DistributionEmailDAO> result = EmailJsonParser.parseDistributionEmails(
      req
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseDistributionEmails_whenMissingEmailAddress_thenSkipped()
    throws IOException, ParseException {
    String json = "[{\"id\":1}]";

    WebScriptRequest req = mockRequest(json);
    List<DistributionEmailDAO> result = EmailJsonParser.parseDistributionEmails(
      req
    );

    assertTrue(result.isEmpty());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenMalformedJson_thenParseException()
    throws IOException, ParseException {
    WebScriptRequest req = mockRequest("not json at all {{{");
    EmailJsonParser.parse(req);
  }
}
