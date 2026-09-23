package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateModelException;
import java.util.Collections;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LibraryNodePathMethodTest {

  private LibraryNodePathMethod method;
  private NodeService nodeService;

  @Before
  public void setUp() {
    method = new LibraryNodePathMethod();
    nodeService = mock(NodeService.class);
    method.setNodeService(nodeService);
  }

  @Test
  public void testGetResult_whenPathHasLibraryElements_thenReturnsLibraryPath()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Path path = buildPath(
      "company_home",
      "circabc",
      "category1",
      "ig1",
      "Library",
      "myFolder"
    );
    when(nodeService.getPath(nodeRef)).thenReturn(path);

    String result = method.getResult(nodeRef);

    assertEquals("/myFolder", result);
  }

  @Test
  public void testGetResult_whenPathHasMultipleSubfolders_thenReturnsFullSubpath()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Path path = buildPath(
      "company_home",
      "circabc",
      "category1",
      "ig1",
      "Library",
      "folder1",
      "folder2"
    );
    when(nodeService.getPath(nodeRef)).thenReturn(path);

    String result = method.getResult(nodeRef);

    assertEquals("/folder1/folder2", result);
  }

  @Test
  public void testGetResult_whenPathHasOnlyBaseElements_thenReturnsEmpty()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Path path = buildPath(
      "company_home",
      "circabc",
      "category1",
      "ig1",
      "Library"
    );
    when(nodeService.getPath(nodeRef)).thenReturn(path);

    String result = method.getResult(nodeRef);

    assertEquals("", result);
  }

  @Test
  public void testGetResult_whenEmptyPath_thenReturnsEmpty()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);

    String result = method.getResult(nodeRef);

    assertEquals("", result);
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    Object result = method.exec(Collections.emptyList());

    assertEquals("", result);
  }

  private Path buildPath(String... names) {
    Path path = new Path();
    for (String name : names) {
      Path.Element element = mock(Path.Element.class);
      when(element.getElementString()).thenReturn(name);
      path.append(element);
    }
    return path;
  }
}
