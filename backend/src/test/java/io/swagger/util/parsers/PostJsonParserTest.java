package io.swagger.util.parsers;

import static org.junit.Assert.*;

import io.swagger.model.Comment;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class PostJsonParserTest {

  @Test
  public void testParsePartial_whenValidJson_thenReturnsCommentWithText()
    throws ParseException {
    String json = "{\"text\":\"Hello World\"}";

    Comment result = PostJsonParser.parsePartial(json);

    assertNotNull(result);
    assertEquals("Hello World", result.getText());
  }

  @Test(expected = ParseException.class)
  public void testParsePartial_whenInvalidJson_thenThrowsParseException()
    throws ParseException {
    PostJsonParser.parsePartial("not valid json");
  }

  @Test(expected = NullPointerException.class)
  public void testParsePartial_whenNullInput_thenThrowsException()
    throws ParseException {
    PostJsonParser.parsePartial(null);
  }

  @Test(expected = NullPointerException.class)
  public void testParsePartial_whenMissingTextField_thenThrowsException()
    throws ParseException {
    PostJsonParser.parsePartial("{\"other\":\"value\"}");
  }

  @Test
  public void testParsePartial_whenEmptyText_thenReturnsCommentWithEmptyText()
    throws ParseException {
    String json = "{\"text\":\"\"}";

    Comment result = PostJsonParser.parsePartial(json);

    assertNotNull(result);
    assertEquals("", result.getText());
  }

  @Test
  public void testConstructor_whenInstantiated_thenThrowsIllegalStateException()
    throws Exception {
    java.lang.reflect.Constructor<PostJsonParser> constructor =
      PostJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }
}
