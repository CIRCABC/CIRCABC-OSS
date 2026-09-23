package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class MailSendRequestTest {

  private MailSendRequest request;

  @Before
  public void setUp() {
    request = new MailSendRequest();
  }

  @Test
  public void testContent_whenSet_thenReturnsNodeRef() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    request.setContent(nodeRef);
    assertEquals(nodeRef, request.getContent());
  }

  @Test
  public void testFrom_whenSet_thenReturnsValue() {
    request.setFrom("sender@example.com");
    assertEquals("sender@example.com", request.getFrom());
  }

  @Test
  public void testTo_whenSet_thenReturnsValue() {
    request.setTo("recipient@example.com");
    assertEquals("recipient@example.com", request.getTo());
  }

  @Test
  public void testOthers_whenSet_thenReturnsList() {
    List<String> others = Arrays.asList("a@example.com", "b@example.com");
    request.setOthers(others);
    assertEquals(others, request.getOthers());
  }

  @Test
  public void testReplyTo_whenSet_thenReturnsValue() {
    request.setReplyTo("reply@example.com");
    assertEquals("reply@example.com", request.getReplyTo());
  }

  @Test
  public void testSubject_whenSet_thenReturnsValue() {
    request.setSubject("Test Subject");
    assertEquals("Test Subject", request.getSubject());
  }

  @Test
  public void testBody_whenSet_thenReturnsValue() {
    request.setBody("<p>Hello</p>");
    assertEquals("<p>Hello</p>", request.getBody());
  }

  @Test
  public void testHtml_whenSet_thenReturnsValue() {
    request.setHtml(true);
    assertTrue(request.isHtml());
  }

  @Test
  public void testHtml_defaultIsFalse() {
    assertFalse(request.isHtml());
  }

  @Test
  public void testAttachments_whenSet_thenReturnsList() {
    NodeRef ref1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "att-1"
    );
    NodeRef ref2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "att-2"
    );
    List<NodeRef> attachments = Arrays.asList(ref1, ref2);
    request.setAttachments(attachments);
    assertEquals(attachments, request.getAttachments());
  }

  @Test
  public void testUseBCC_whenSet_thenReturnsValue() {
    request.setUseBCC(true);
    assertTrue(request.isUseBCC());
  }

  @Test
  public void testUseBCC_defaultIsFalse() {
    assertFalse(request.isUseBCC());
  }

  @Test
  public void testFileAttachments_whenSet_thenReturnsList() {
    List<File> files = Collections.singletonList(new File("/tmp/test.pdf"));
    request.setFileAttachments(files);
    assertEquals(files, request.getFileAttachments());
  }

  @Test
  public void testAllFields_whenNull_thenReturnsNull() {
    assertNull(request.getContent());
    assertNull(request.getFrom());
    assertNull(request.getTo());
    assertNull(request.getOthers());
    assertNull(request.getReplyTo());
    assertNull(request.getSubject());
    assertNull(request.getBody());
    assertNull(request.getAttachments());
    assertNull(request.getFileAttachments());
  }
}
