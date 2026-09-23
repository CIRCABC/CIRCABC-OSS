package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.RestoreNodeMetadata;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class RestoreNodeMetadataParserTest {

  private WebScriptRequest request;
  private Content content;

  @Before
  public void setUp() {
    request = mock(WebScriptRequest.class);
    content = mock(Content.class);
    when(request.getContent()).thenReturn(content);
  }

  @Test
  public void testParseJSon_whenBothFieldsPresent_thenReturnsParsedMetadata()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn(
      "{\"archiveNodeId\":\"node-123\",\"targetFolderId\":\"folder-456\"}"
    );

    RestoreNodeMetadata result = RestoreNodeMetadataParser.parseJSon(request);

    assertEquals("node-123", result.getArchiveNodeId());
    assertEquals("folder-456", result.getTargetFolderId());
  }

  @Test
  public void testParseJSon_whenOnlyArchiveNodeId_thenTargetFolderIdIsNull()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{\"archiveNodeId\":\"node-123\"}");

    RestoreNodeMetadata result = RestoreNodeMetadataParser.parseJSon(request);

    assertEquals("node-123", result.getArchiveNodeId());
    assertNull(result.getTargetFolderId());
  }

  @Test
  public void testParseJSon_whenOnlyTargetFolderId_thenArchiveNodeIdIsNull()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn(
      "{\"targetFolderId\":\"folder-456\"}"
    );

    RestoreNodeMetadata result = RestoreNodeMetadataParser.parseJSon(request);

    assertNull(result.getArchiveNodeId());
    assertEquals("folder-456", result.getTargetFolderId());
  }

  @Test
  public void testParseJSon_whenEmptyJson_thenBothFieldsNull()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("{}");

    RestoreNodeMetadata result = RestoreNodeMetadataParser.parseJSon(request);

    assertNull(result.getArchiveNodeId());
    assertNull(result.getTargetFolderId());
  }

  @Test(expected = ParseException.class)
  public void testParseJSon_whenInvalidJson_thenThrowsParseException()
    throws IOException, ParseException {
    when(content.getContent()).thenReturn("not valid json");

    RestoreNodeMetadataParser.parseJSon(request);
  }

  @Test(expected = IOException.class)
  public void testParseJSon_whenContentThrowsIOException_thenPropagates()
    throws IOException, ParseException {
    when(content.getContent()).thenThrow(new IOException("read error"));

    RestoreNodeMetadataParser.parseJSon(request);
  }
}
