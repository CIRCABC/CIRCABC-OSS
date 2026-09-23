package eu.europa.ec.digit.circabc.rest.model;

import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.DocumentModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class UrlModelAspectTest {

  private UrlModelAspect urlModelAspect;
  private PolicyComponent policyComponent;
  private BehaviourFilter policyBehaviourFilter;
  private ContentService contentService;
  private NodeRef nodeRef;

  @Before
  public void setUp() throws Exception {
    urlModelAspect = new UrlModelAspect();
    policyComponent = mock(PolicyComponent.class);
    policyBehaviourFilter = mock(BehaviourFilter.class);
    contentService = mock(ContentService.class);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");

    setField("policyComponent", policyComponent);
    setField("policyBehaviourFilter", policyBehaviourFilter);
    setField("contentService", contentService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UrlModelAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(urlModelAspect, value);
  }

  @Test
  public void testInit_whenCalled_thenBindsBehaviours() {
    urlModelAspect.init();

    verify(policyComponent, times(2)).bindClassBehaviour(
      any(QName.class),
      eq(DocumentModel.ASPECT_URLABLE),
      any()
    );
  }

  @Test(expected = AlfrescoRuntimeException.class)
  public void testOnContentUpdate_whenCalled_thenThrowsException() {
    urlModelAspect.onContentUpdate(nodeRef, true);
  }

  @Test
  public void testOnUpdateProperties_whenNewUrlSet_thenWritesContent() {
    String newUrl = "http://example.com";
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    Map<QName, Serializable> before = new HashMap<>();
    Map<QName, Serializable> after = new HashMap<>();
    after.put(DocumentModel.PROP_URL, newUrl);

    urlModelAspect.onUpdateProperties(nodeRef, before, after);

    verify(policyBehaviourFilter).disableBehaviour(
      nodeRef,
      DocumentModel.ASPECT_URLABLE
    );
    verify(writer).setMimetype(MimetypeMap.MIMETYPE_HTML);
    verify(writer).setEncoding("UTF-8");
    verify(writer).putContent(
      MessageFormat.format(UrlModelAspect.URL_FIXED_CONTENT, newUrl)
    );
    verify(policyBehaviourFilter).enableBehaviour(
      nodeRef,
      DocumentModel.ASPECT_URLABLE
    );
  }

  @Test
  public void testOnUpdateProperties_whenAfterIsNull_thenDoesNothing() {
    urlModelAspect.onUpdateProperties(nodeRef, null, null);

    verifyNoInteractions(contentService);
  }

  @Test
  public void testOnUpdateProperties_whenUrlUnchanged_thenDoesNothing() {
    String url = "http://example.com";
    Map<QName, Serializable> before = new HashMap<>();
    before.put(DocumentModel.PROP_URL, url);
    Map<QName, Serializable> after = new HashMap<>();
    after.put(DocumentModel.PROP_URL, url);

    urlModelAspect.onUpdateProperties(nodeRef, before, after);

    verifyNoInteractions(contentService);
  }

  @Test
  public void testOnUpdateProperties_whenNewUrlIsNull_thenDoesNothing() {
    Map<QName, Serializable> before = new HashMap<>();
    Map<QName, Serializable> after = new HashMap<>();
    after.put(DocumentModel.PROP_URL, null);

    urlModelAspect.onUpdateProperties(nodeRef, before, after);

    verifyNoInteractions(contentService);
  }

  @Test
  public void testOnUpdateProperties_whenBeforeIsNull_thenWritesContent() {
    String newUrl = "http://example.com";
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    Map<QName, Serializable> after = new HashMap<>();
    after.put(DocumentModel.PROP_URL, newUrl);

    urlModelAspect.onUpdateProperties(nodeRef, null, after);

    verify(writer).putContent(
      MessageFormat.format(UrlModelAspect.URL_FIXED_CONTENT, newUrl)
    );
  }
}
