package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class IndexHeaderImplTest {

  @Test
  public void testGetHeaderName_whenConstructed_thenReturnsName() {
    IndexHeaderImpl header = new IndexHeaderImpl(
      "TestHeader",
      Collections.emptyList()
    );
    assertEquals("TestHeader", header.getHeaderName());
  }

  @Test
  public void testGetHeaderValidators_whenConstructed_thenReturnsValidators() {
    HeaderValidator v1 = mock(HeaderValidator.class);
    HeaderValidator v2 = mock(HeaderValidator.class);
    List<HeaderValidator> validators = Arrays.asList(v1, v2);

    IndexHeaderImpl header = new IndexHeaderImpl("H", validators);

    assertEquals(2, header.getHeaderValidators().size());
    assertSame(v1, header.getHeaderValidators().get(0));
    assertSame(v2, header.getHeaderValidators().get(1));
  }

  @Test
  public void testGetHeaderName_whenNull_thenReturnsNull() {
    IndexHeaderImpl header = new IndexHeaderImpl(null, Collections.emptyList());
    assertNull(header.getHeaderName());
  }

  @Test
  public void testGetHeaderValidators_whenNull_thenReturnsNull() {
    IndexHeaderImpl header = new IndexHeaderImpl("H", null);
    assertNull(header.getHeaderValidators());
  }

  @Test
  public void testGetHeaderValidators_whenEmpty_thenReturnsEmptyList() {
    IndexHeaderImpl header = new IndexHeaderImpl("H", Collections.emptyList());
    assertTrue(header.getHeaderValidators().isEmpty());
  }
}
