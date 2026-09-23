package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LibraryNodeLinkMethodTest {

  private LibraryNodeLinkMethod method;
  private NodeService nodeService;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    method = new LibraryNodeLinkMethod();
    nodeService = mock(NodeService.class);
    circabcConfig = mock(CircabcConfig.class);

    method.setNodeService(nodeService);

    Field field = LibraryNodeLinkMethod.class.getDeclaredField("circabcConfig");
    field.setAccessible(true);
    field.set(method, circabcConfig);
  }

  @Test
  public void testGetResult_whenNodeHasLibraryAspect_thenReturnsFullLink() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      mock(ChildAssociationRef.class)
    );
    when(nodeService.getPrimaryParent(nodeRef).getParentRef()).thenReturn(
      parentRef
    );

    when(
      nodeService.hasAspect(parentRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(nodeService.getPrimaryParent(parentRef)).thenReturn(
      mock(ChildAssociationRef.class)
    );
    when(nodeService.getPrimaryParent(parentRef).getParentRef()).thenReturn(
      groupRef
    );

    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/ui/group/group-id/library/node-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenNodeHasLibraryAspect_contextWithoutTrailingSlash() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      mock(ChildAssociationRef.class)
    );
    when(nodeService.getPrimaryParent(nodeRef).getParentRef()).thenReturn(
      groupRef
    );
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/ui/group/group-id/library/node-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenNodeDoesNotHaveLibraryAspect_thenReturnsLinkWithEmptyGroup() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/ui/group//library/node-id/details",
      result
    );
  }
}
