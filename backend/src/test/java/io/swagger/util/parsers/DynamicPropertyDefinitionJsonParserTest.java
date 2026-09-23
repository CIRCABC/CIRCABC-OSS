package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class DynamicPropertyDefinitionJsonParserTest {

  private WebScriptRequest req;
  private org.springframework.extensions.surf.util.Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(org.springframework.extensions.surf.util.Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParseJsonDynamicPropertyDefinition_whenValidJson_thenReturnsPopulatedObject()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"123\",\"title\":{\"en\":\"English Title\",\"fr\":\"Titre Français\"}," +
      "\"propertyType\":\"TEXT\",\"possibleValues\":[\"val1\",\"val2\"]," +
      "\"updatedValues\":[{\"_new\":\"newVal\",\"old\":\"oldVal\",\"status\":\"modified\"}]}";
    when(content.getContent()).thenReturn(json);

    DynamicPropertyDefinition result =
      DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
        req
      );

    assertEquals("123", result.getId());
    assertEquals("TEXT", result.getPropertyType());
    assertEquals("English Title", result.getTitle().get("en"));
    assertEquals("Titre Français", result.getTitle().get("fr"));
    assertEquals(2, result.getPossibleValues().size());
    assertEquals("val1", result.getPossibleValues().get(0));
    assertEquals("val2", result.getPossibleValues().get(1));
    assertEquals(1, result.getUpdatedValues().size());
    DynamicPropertyDefinitionUpdatedValues update = result
      .getUpdatedValues()
      .get(0);
    assertEquals("newVal", update.getNewValue());
    assertEquals("oldVal", update.getOld());
    assertEquals("modified", update.getStatus());
  }

  @Test(expected = ParseException.class)
  public void testParseJsonDynamicPropertyDefinition_whenTitleMissing_thenThrowsParseException()
    throws IOException, ParseException {
    String json = "{\"id\":\"123\",\"propertyType\":\"TEXT\"}";
    when(content.getContent()).thenReturn(json);

    DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(req);
  }

  @Test
  public void testParseJsonDynamicPropertyDefinition_whenNoPossibleValues_thenEmptyList()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"456\",\"title\":{\"en\":\"Title\"},\"propertyType\":\"SELECTION\"}";
    when(content.getContent()).thenReturn(json);

    DynamicPropertyDefinition result =
      DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
        req
      );

    assertTrue(result.getPossibleValues().isEmpty());
    assertTrue(result.getUpdatedValues().isEmpty());
  }

  @Test
  public void testParseJsonDynamicPropertyDefinition_whenNoUpdatedValues_thenEmptyList()
    throws IOException, ParseException {
    String json =
      "{\"id\":\"789\",\"title\":{\"de\":\"Titel\"},\"propertyType\":\"DATE\"," +
      "\"possibleValues\":[\"a\"]}";
    when(content.getContent()).thenReturn(json);

    DynamicPropertyDefinition result =
      DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
        req
      );

    assertTrue(result.getUpdatedValues().isEmpty());
    assertEquals(1, result.getPossibleValues().size());
    assertEquals("a", result.getPossibleValues().get(0));
  }

  @Test(expected = ParseException.class)
  public void testParseJsonDynamicPropertyDefinition_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(req);
  }
}
