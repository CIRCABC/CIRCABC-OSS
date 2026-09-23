package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.ExternalRepositoryTransaction;
import io.swagger.model.TicketRequestInfo;
import java.io.IOException;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class AresBridgeJsonParserTest {

  private WebScriptRequest req;
  private org.springframework.extensions.surf.util.Content content;

  @Before
  public void setUp() {
    req = mock(WebScriptRequest.class);
    content = mock(org.springframework.extensions.surf.util.Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testParse_whenAllFieldsPresent_thenReturnsPopulatedResult()
    throws IOException, ParseException {
    String json =
      "{\"requestDate\":\"2026-05-04\",\"httpVerb\":\"GET\",\"path\":\"/api/test\"}";
    when(content.getContent()).thenReturn(json);

    TicketRequestInfo result = AresBridgeJsonParser.parse(req);

    assertEquals("2026-05-04", result.getRequestDate());
    assertEquals("GET", result.getHttpVerb());
    assertEquals("/api/test", result.getPath());
  }

  @Test
  public void testParse_whenFieldsNull_thenFieldsRemainNull()
    throws IOException, ParseException {
    String json = "{}";
    when(content.getContent()).thenReturn(json);

    TicketRequestInfo result = AresBridgeJsonParser.parse(req);

    assertNull(result.getRequestDate());
    assertNull(result.getHttpVerb());
    assertNull(result.getPath());
  }

  @Test(expected = ParseException.class)
  public void testParse_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    AresBridgeJsonParser.parse(req);
  }

  @Test
  public void testParseTransaction_whenNodesPresent_thenReturnsList()
    throws IOException, ParseException {
    String json =
      "{\"transactionId\":\"tx-123\",\"nodes\":[{\"id\":\"node-1\",\"name\":\"doc.pdf\",\"properties\":{\"versionLabel\":\"1.0\"}}]}";
    when(content.getContent()).thenReturn(json);

    List<ExternalRepositoryTransaction> result =
      AresBridgeJsonParser.parseTransaction(req);

    assertEquals(1, result.size());
    ExternalRepositoryTransaction tx = result.get(0);
    assertEquals("tx-123", tx.getTransactionId());
    assertEquals("node-1", tx.getNodeId());
    assertEquals("doc.pdf", tx.getName());
    assertEquals("1.0", tx.getVersionLabel());
  }

  @Test
  public void testParseTransaction_whenNodesNull_thenReturnsEmptyList()
    throws IOException, ParseException {
    String json = "{\"transactionId\":\"tx-456\"}";
    when(content.getContent()).thenReturn(json);

    List<ExternalRepositoryTransaction> result =
      AresBridgeJsonParser.parseTransaction(req);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseTransaction_whenTransactionIdNull_thenUsesEmptyString()
    throws IOException, ParseException {
    String json =
      "{\"nodes\":[{\"id\":\"n1\",\"name\":\"file.txt\",\"properties\":{\"versionLabel\":\"2.0\"}}]}";
    when(content.getContent()).thenReturn(json);

    List<ExternalRepositoryTransaction> result =
      AresBridgeJsonParser.parseTransaction(req);

    assertEquals(1, result.size());
    assertEquals("", result.get(0).getTransactionId());
  }

  @Test
  public void testParseTransaction_whenMultipleNodes_thenReturnsAll()
    throws IOException, ParseException {
    String json =
      "{\"transactionId\":\"tx-789\",\"nodes\":[" +
      "{\"id\":\"n1\",\"name\":\"a.pdf\",\"properties\":{\"versionLabel\":\"1.0\"}}," +
      "{\"id\":\"n2\",\"name\":\"b.pdf\",\"properties\":{\"versionLabel\":\"2.0\"}}" +
      "]}";
    when(content.getContent()).thenReturn(json);

    List<ExternalRepositoryTransaction> result =
      AresBridgeJsonParser.parseTransaction(req);

    assertEquals(2, result.size());
    assertEquals("n1", result.get(0).getNodeId());
    assertEquals("n2", result.get(1).getNodeId());
  }
}
