package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.alfresco.service.cmr.repository.ContentReader;
import org.junit.Before;
import org.junit.Test;

public class InputSourceWrapperTest {

  private ContentReader contentReader;
  private InputSourceWrapper wrapper;

  @Before
  public void setUp() {
    contentReader = mock(ContentReader.class);
    wrapper = new InputSourceWrapper(contentReader);
  }

  @Test
  public void testGetInputStream_whenExistsAndNotClosed_thenReturnsStream()
    throws IOException {
    InputStream expected = new ByteArrayInputStream("data".getBytes());
    when(contentReader.exists()).thenReturn(true);
    when(contentReader.isClosed()).thenReturn(false);
    when(contentReader.getContentInputStream()).thenReturn(expected);

    InputStream result = wrapper.getInputStream();

    assertSame(expected, result);
    verify(contentReader, never()).getReader();
  }

  @Test
  public void testGetInputStream_whenExistsAndClosed_thenGetsNewReader()
    throws IOException {
    ContentReader newReader = mock(ContentReader.class);
    InputStream expected = new ByteArrayInputStream("data".getBytes());
    when(contentReader.exists()).thenReturn(true);
    when(contentReader.isClosed()).thenReturn(true);
    when(contentReader.getReader()).thenReturn(newReader);
    when(newReader.getContentInputStream()).thenReturn(expected);

    InputStream result = wrapper.getInputStream();

    assertSame(expected, result);
    verify(contentReader).getReader();
  }

  @Test
  public void testGetInputStream_whenNotExists_thenReturnsNull()
    throws IOException {
    when(contentReader.exists()).thenReturn(false);

    InputStream result = wrapper.getInputStream();

    assertNull(result);
  }
}
