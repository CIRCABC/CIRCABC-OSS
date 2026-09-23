package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class AgendaNodeLinkMethodTest {

  private AgendaNodeLinkMethod agendaNodeLinkMethod;
  private CircabcConfig circabcConfig;
  private NodeService nodeService;

  @Before
  public void setUp() throws Exception {
    agendaNodeLinkMethod = new AgendaNodeLinkMethod();
    circabcConfig = mock(CircabcConfig.class);
    nodeService = mock(NodeService.class);

    agendaNodeLinkMethod.setNodeService(nodeService);

    Field configField = AgendaNodeLinkMethod.class.getDeclaredField(
      "circabcConfig"
    );
    configField.setAccessible(true);
    configField.set(agendaNodeLinkMethod, circabcConfig);
  }

  @Test
  public void testGetResult_whenNodeHasEventAspect_thenReturnsCorrectLink() {
    NodeRef eventNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-id"
    );
    NodeRef parentNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );

    when(
      nodeService.hasAspect(eventNode, CircabcModel.ASPECT_EVENT)
    ).thenReturn(true);
    ChildAssociationRef eventToParent = mock(ChildAssociationRef.class);
    when(eventToParent.getParentRef()).thenReturn(parentNode);
    when(nodeService.getPrimaryParent(eventNode)).thenReturn(eventToParent);

    when(
      nodeService.hasAspect(parentNode, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    ChildAssociationRef parentToIgRoot = mock(ChildAssociationRef.class);
    when(parentToIgRoot.getParentRef()).thenReturn(igRoot);
    when(nodeService.getPrimaryParent(parentNode)).thenReturn(parentToIgRoot);

    when(nodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = agendaNodeLinkMethod.getResult(eventNode);

    assertEquals(
      "https://circabc.europa.eu/ui/group/ig-root-id/agenda/event-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenNodeIsEventType_thenReturnsCorrectLink() {
    NodeRef eventNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-id"
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );

    when(
      nodeService.hasAspect(eventNode, CircabcModel.ASPECT_EVENT)
    ).thenReturn(false);
    when(nodeService.getType(eventNode)).thenReturn(EventModel.TYPE_EVENT);

    ChildAssociationRef eventToIgRoot = mock(ChildAssociationRef.class);
    when(eventToIgRoot.getParentRef()).thenReturn(igRoot);
    when(nodeService.getPrimaryParent(eventNode)).thenReturn(eventToIgRoot);

    when(nodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    String result = agendaNodeLinkMethod.getResult(eventNode);

    assertEquals(
      "https://circabc.europa.eu/ui/group/ig-root-id/agenda/event-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenNodeIsNotEvent_thenReturnsLinkWithEmptyGroupId() {
    NodeRef node = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );

    when(nodeService.hasAspect(node, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(nodeService.getType(node)).thenReturn(CircabcModel.ASPECT_IGROOT);

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = agendaNodeLinkMethod.getResult(node);

    assertEquals(
      "https://circabc.europa.eu/ui/group//agenda/node-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenContextDoesNotEndWithSlash_thenAddsSlashBeforeGroup() {
    NodeRef eventNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-id"
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );

    when(
      nodeService.hasAspect(eventNode, CircabcModel.ASPECT_EVENT)
    ).thenReturn(true);
    ChildAssociationRef assoc = mock(ChildAssociationRef.class);
    when(assoc.getParentRef()).thenReturn(igRoot);
    when(nodeService.getPrimaryParent(eventNode)).thenReturn(assoc);
    when(nodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    String result = agendaNodeLinkMethod.getResult(eventNode);

    assertEquals(
      "https://circabc.europa.eu/ui/group/ig-root-id/agenda/event-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenContextEndsWithSlash_thenNoDoubleSlash() {
    NodeRef eventNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-id"
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );

    when(
      nodeService.hasAspect(eventNode, CircabcModel.ASPECT_EVENT)
    ).thenReturn(true);
    ChildAssociationRef assoc = mock(ChildAssociationRef.class);
    when(assoc.getParentRef()).thenReturn(igRoot);
    when(nodeService.getPrimaryParent(eventNode)).thenReturn(assoc);
    when(nodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = agendaNodeLinkMethod.getResult(eventNode);

    assertFalse(result.contains("//group"));
    assertTrue(result.contains("/ui/group/"));
  }
}
