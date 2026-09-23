package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class IndexHeadersTest {

  private IndexHeadersImpl indexHeaders;

  @Before
  public void setUp() {
    indexHeaders = new IndexHeadersImpl();
  }

  @Test
  public void testGetHeaders_whenEmpty_thenReturnsEmptyList() {
    List<IndexHeader> headers = indexHeaders.getHeaders();
    assertNotNull(headers);
    assertTrue(headers.isEmpty());
  }

  @Test
  public void testAddHeader_whenHeaderAdded_thenListContainsIt() {
    IndexHeader header = new IndexHeaderImpl("name", null);
    indexHeaders.addHeader(header);

    assertEquals(1, indexHeaders.getHeaders().size());
    assertEquals("name", indexHeaders.getHeaders().get(0).getHeaderName());
  }

  @Test
  public void testGetHeader_whenExists_thenReturnsHeader() {
    indexHeaders.addHeader(new IndexHeaderImpl("col1", null));
    indexHeaders.addHeader(new IndexHeaderImpl("col2", null));

    IndexHeader result = indexHeaders.getHeader("col2");
    assertNotNull(result);
    assertEquals("col2", result.getHeaderName());
  }

  @Test
  public void testGetHeader_whenNotExists_thenReturnsNull() {
    indexHeaders.addHeader(new IndexHeaderImpl("col1", null));

    assertNull(indexHeaders.getHeader("nonexistent"));
  }

  @Test
  public void testConstructorWithArray_whenNoDuplicates_thenAddsAll() {
    IndexHeadersImpl headers = new IndexHeadersImpl(
      new String[] { "a", "b", "c" }
    );

    assertEquals(3, headers.getHeaders().size());
    assertNotNull(headers.getHeader("a"));
    assertNotNull(headers.getHeader("b"));
    assertNotNull(headers.getHeader("c"));
  }

  @Test
  public void testConstructorWithArray_whenDuplicatePresent_thenSkipsDuplicate() {
    IndexHeadersImpl headers = new IndexHeadersImpl(
      new String[] { "a", "b", "a" }
    );

    assertEquals(2, headers.getHeaders().size());
    assertEquals("a", headers.getHeaders().get(0).getHeaderName());
    assertEquals("b", headers.getHeaders().get(1).getHeaderName());
  }
}
