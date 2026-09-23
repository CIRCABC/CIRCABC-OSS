package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.News;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NewsJsonParserTest {

  private WebScriptRequest req;
  private Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParse_whenValidTextPattern_thenReturnsNews()
    throws Exception {
    String json =
      "{\"id\":\"123\",\"content\":\"<p>Hello</p>\",\"pattern\":\"text\"," +
      "\"layout\":\"normal\",\"size\":2,\"title\":{\"en\":\"Title EN\"}," +
      "\"url\":\"\"}";
    when(content.getContent()).thenReturn(json);

    News result = NewsJsonParser.parse(req);

    assertEquals("123", result.getId());
    assertEquals("<p>Hello</p>", result.getContent());
    assertEquals(News.PatternEnum.TEXT, result.getPattern());
    assertEquals(News.LayoutEnum.NORMAL, result.getLayout());
    assertEquals(Integer.valueOf(2), result.getSize());
    assertNotNull(result.getTitle());
  }

  @Test
  public void testParse_whenIframePattern_thenSetsEmptyTitle()
    throws Exception {
    String json =
      "{\"pattern\":\"iframe\",\"layout\":\"important\",\"size\":1," +
      "\"url\":\"http://example.com\"}";
    when(content.getContent()).thenReturn(json);

    News result = NewsJsonParser.parse(req);

    assertEquals(News.PatternEnum.IFRAME, result.getPattern());
    assertEquals(News.LayoutEnum.IMPORTANT, result.getLayout());
    assertNotNull(result.getTitle());
    assertTrue(result.getTitle().isEmpty());
    assertEquals("http://example.com", result.getUrl());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenNonIframePatternWithNullTitle_thenThrowsParseException()
    throws Exception {
    String json = "{\"pattern\":\"text\",\"layout\":\"normal\",\"size\":1}";
    when(content.getContent()).thenReturn(json);

    NewsJsonParser.parse(req);
  }

  @Test(expected = IOException.class)
  public void testParse_whenInvalidUrl_thenThrowsIOException()
    throws Exception {
    String json =
      "{\"pattern\":\"iframe\",\"layout\":\"normal\",\"size\":1," +
      "\"url\":\"not-a-valid-url\"}";
    when(content.getContent()).thenReturn(json);

    NewsJsonParser.parse(req);
  }

  @Test
  public void testParse_whenUrlIsEmpty_thenUrlNotSet() throws Exception {
    String json =
      "{\"pattern\":\"iframe\",\"layout\":\"normal\",\"size\":3,\"url\":\"\"}";
    when(content.getContent()).thenReturn(json);

    News result = NewsJsonParser.parse(req);

    assertNull(result.getUrl());
  }

  @Test
  public void testConstructor_thenThrowsIllegalStateException()
    throws Exception {
    java.lang.reflect.Constructor<NewsJsonParser> constructor =
      NewsJsonParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }
}
