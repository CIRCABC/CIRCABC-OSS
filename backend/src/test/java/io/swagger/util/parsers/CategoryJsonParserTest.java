package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.AdminContactRequest;
import io.swagger.model.Category;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseSimpleJSON_whenFullPayload_thenAllFieldsParsed()
    throws IOException, ParseException {
    JSONObject title = new JSONObject();
    title.put("en", "English Title");
    title.put("fr", "Titre Français");

    JSONArray emails = new JSONArray();
    emails.add("a@b.com");
    emails.add("c@d.com");

    JSONObject json = new JSONObject();
    json.put("name", "TestCategory");
    json.put("ID", "cat-123");
    json.put("title", title);
    json.put("useSingleContact", "true");
    json.put("contactEmails", emails);

    when(content.getContent()).thenReturn(json.toJSONString());

    Category result = CategoryJsonParser.parseSimpleJSON(req);

    assertEquals("TestCategory", result.getName());
    assertEquals("cat-123", result.getId());
    assertEquals("English Title", result.getTitle().get("en"));
    assertEquals("Titre Français", result.getTitle().get("fr"));
    assertTrue(result.getUseSingleContact());
    assertEquals(2, result.getContactEmails().size());
    assertEquals("a@b.com", result.getContactEmails().get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseSimpleJSON_whenMinimalPayload_thenDefaultsApplied()
    throws IOException, ParseException {
    JSONObject json = new JSONObject();
    json.put("name", "Min");
    json.put("ID", "id-1");

    when(content.getContent()).thenReturn(json.toJSONString());

    Category result = CategoryJsonParser.parseSimpleJSON(req);

    assertEquals("Min", result.getName());
    assertEquals("id-1", result.getId());
    assertTrue(result.getTitle().isEmpty());
    assertFalse(result.getUseSingleContact());
    assertTrue(result.getContactEmails().isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseSimpleJSON_whenEmptyEmailsFiltered_thenExcluded()
    throws IOException, ParseException {
    JSONArray emails = new JSONArray();
    emails.add("");
    emails.add("valid@test.com");
    emails.add("");

    JSONObject json = new JSONObject();
    json.put("name", "N");
    json.put("ID", "I");
    json.put("contactEmails", emails);

    when(content.getContent()).thenReturn(json.toJSONString());

    Category result = CategoryJsonParser.parseSimpleJSON(req);

    assertEquals(1, result.getContactEmails().size());
    assertEquals("valid@test.com", result.getContactEmails().get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseAdminContactRequest_whenFullPayload_thenParsed()
    throws IOException, ParseException {
    JSONObject json = new JSONObject();
    json.put("content", "Hello admin");
    json.put("sendCopy", "true");

    when(content.getContent()).thenReturn(json.toJSONString());

    AdminContactRequest result = CategoryJsonParser.parseAdminContactRequest(
      req
    );

    assertEquals("Hello admin", result.getContent());
    assertTrue(result.getSendCopy());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseAdminContactRequest_whenNullFields_thenDefaults()
    throws IOException, ParseException {
    JSONObject json = new JSONObject();

    when(content.getContent()).thenReturn(json.toJSONString());

    AdminContactRequest result = CategoryJsonParser.parseAdminContactRequest(
      req
    );

    assertNull(result.getContent());
    assertFalse(result.getSendCopy());
  }

  @Test(expected = ParseException.class)
  public void testParseSimpleJSON_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json {{{");

    CategoryJsonParser.parseSimpleJSON(req);
  }
}
