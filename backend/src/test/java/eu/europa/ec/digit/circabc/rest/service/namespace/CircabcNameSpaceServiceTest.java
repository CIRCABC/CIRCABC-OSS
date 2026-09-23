package eu.europa.ec.digit.circabc.rest.service.namespace;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Collections;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.junit.Before;
import org.junit.Test;

public class CircabcNameSpaceServiceTest {

  private CircabcNameSpaceService service;

  @Before
  public void setUp() {
    service = mock(CircabcNameSpaceService.class);
  }

  @Test
  public void testCecDigitUri_hasExpectedValue() {
    assertEquals("http://eu.cec.digit", CircabcNameSpaceService.CEC_DIGIT_URI);
  }

  @Test
  public void testInterface_extendsNamespacePrefixResolver() {
    assertTrue(service instanceof NamespacePrefixResolver);
  }

  @Test
  public void testGetNamespaceURI_whenPrefixProvided_thenReturnsUri() {
    when(service.getNamespaceURI("ci")).thenReturn("http://eu.cec.digit");

    String uri = service.getNamespaceURI("ci");

    assertEquals("http://eu.cec.digit", uri);
    verify(service).getNamespaceURI("ci");
  }

  @Test
  public void testGetPrefixes_whenUriProvided_thenReturnsPrefixes() {
    Collection<String> prefixes = Collections.singletonList("ci");
    when(service.getPrefixes("http://eu.cec.digit")).thenReturn(prefixes);

    Collection<String> result = service.getPrefixes("http://eu.cec.digit");

    assertEquals(1, result.size());
    assertTrue(result.contains("ci"));
  }

  @Test
  public void testGetURIs_returnsMappedUris() {
    Collection<String> uris = Collections.singletonList("http://eu.cec.digit");
    when(service.getURIs()).thenReturn(uris);

    Collection<String> result = service.getURIs();

    assertFalse(result.isEmpty());
    assertTrue(result.contains("http://eu.cec.digit"));
  }
}
