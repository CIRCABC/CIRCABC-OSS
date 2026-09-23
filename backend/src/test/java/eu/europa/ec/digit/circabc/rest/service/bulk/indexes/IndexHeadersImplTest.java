package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class IndexHeadersImplTest {

  private IndexHeadersImpl indexHeaders;

  @Before
  public void setUp() {
    indexHeaders = new IndexHeadersImpl();
  }

  @Test
  public void testConstructor_default_thenEmptyHeaders() {
    assertTrue(indexHeaders.getHeaders().isEmpty());
  }

  @Test
  public void testConstructor_withHeaders_thenHeadersAdded() {
    IndexHeadersImpl result = new IndexHeadersImpl(
      new String[] { "col1", "col2", "col3" }
    );
    List<IndexHeader> headers = result.getHeaders();
    assertEquals(3, headers.size());
    assertEquals("col1", headers.get(0).getHeaderName());
    assertEquals("col2", headers.get(1).getHeaderName());
    assertEquals("col3", headers.get(2).getHeaderName());
  }

  @Test
  public void testConstructor_withDuplicateHeaders_thenDuplicateSkipped() {
    IndexHeadersImpl result = new IndexHeadersImpl(
      new String[] { "col1", "col2", "col1" }
    );
    assertEquals(2, result.getHeaders().size());
    assertEquals("col1", result.getHeaders().get(0).getHeaderName());
    assertEquals("col2", result.getHeaders().get(1).getHeaderName());
  }

  @Test
  public void testAddHeader_thenHeaderAppended() {
    IndexHeader header = new IndexHeaderImpl("test", null);
    indexHeaders.addHeader(header);
    assertEquals(1, indexHeaders.getHeaders().size());
    assertSame(header, indexHeaders.getHeaders().get(0));
  }

  @Test
  public void testAddHeader_multiple_thenOrderPreserved() {
    indexHeaders.addHeader(new IndexHeaderImpl("a", null));
    indexHeaders.addHeader(new IndexHeaderImpl("b", null));
    indexHeaders.addHeader(new IndexHeaderImpl("c", null));
    assertEquals("a", indexHeaders.getHeaders().get(0).getHeaderName());
    assertEquals("b", indexHeaders.getHeaders().get(1).getHeaderName());
    assertEquals("c", indexHeaders.getHeaders().get(2).getHeaderName());
  }

  @Test
  public void testGetHeader_whenExists_thenReturnsHeader() {
    IndexHeader header = new IndexHeaderImpl("target", null);
    indexHeaders.addHeader(new IndexHeaderImpl("other", null));
    indexHeaders.addHeader(header);
    assertSame(header, indexHeaders.getHeader("target"));
  }

  @Test
  public void testGetHeader_whenNotExists_thenReturnsNull() {
    indexHeaders.addHeader(new IndexHeaderImpl("existing", null));
    assertNull(indexHeaders.getHeader("nonexistent"));
  }

  @Test
  public void testGetHeader_whenEmpty_thenReturnsNull() {
    assertNull(indexHeaders.getHeader("anything"));
  }
}
