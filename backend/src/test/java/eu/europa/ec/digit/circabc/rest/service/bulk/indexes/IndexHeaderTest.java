package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class IndexHeaderTest {

  @Test
  public void testGetHeaderName_whenConstructed_thenReturnsName() {
    IndexHeader header = new IndexHeaderImpl("Title", Collections.emptyList());
    assertEquals("Title", header.getHeaderName());
  }

  @Test
  public void testGetHeaderValidators_whenConstructedWithValidators_thenReturnsThem() {
    HeaderValidator v1 = mock(HeaderValidator.class);
    HeaderValidator v2 = mock(HeaderValidator.class);
    List<HeaderValidator> validators = Arrays.asList(v1, v2);

    IndexHeader header = new IndexHeaderImpl("Name", validators);

    assertEquals(2, header.getHeaderValidators().size());
    assertSame(v1, header.getHeaderValidators().get(0));
    assertSame(v2, header.getHeaderValidators().get(1));
  }

  @Test
  public void testGetHeaderValidators_whenEmpty_thenReturnsEmptyList() {
    IndexHeader header = new IndexHeaderImpl("Col", Collections.emptyList());
    assertTrue(header.getHeaderValidators().isEmpty());
  }

  @Test
  public void testGetHeaderName_whenNull_thenReturnsNull() {
    IndexHeader header = new IndexHeaderImpl(null, Collections.emptyList());
    assertNull(header.getHeaderName());
  }

  @Test
  public void testGetHeaderValidators_whenNull_thenReturnsNull() {
    IndexHeader header = new IndexHeaderImpl("X", null);
    assertNull(header.getHeaderValidators());
  }
}
