package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.exception.PathNotFoundException;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class SimplePathTest {

  private NodeService nodeService;
  private NodeRef rootRef;
  private NodeRef childRef;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    rootRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "root-id");
    childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
  }

  @Test
  public void testConstructor_whenCircabcRoot_thenParentIsNull()
    throws PathNotFoundException {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(rootRef)).thenReturn(aspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_NAME)).thenReturn(
      "Circabc"
    );

    SimplePath path = new SimplePath(nodeService, rootRef);

    assertNull(path.getParent());
    assertEquals("Circabc", path.getName());
    assertEquals(rootRef, path.getNodeRef());
  }

  @Test
  public void testToString_whenCircabcRoot_thenSlashName()
    throws PathNotFoundException {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(rootRef)).thenReturn(aspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_NAME)).thenReturn(
      "Circabc"
    );

    SimplePath path = new SimplePath(nodeService, rootRef);

    assertEquals("/Circabc", path.toString());
  }

  @Test
  public void testConstructor_whenHasPrimaryParent_thenBuildsPath()
    throws PathNotFoundException {
    Set<QName> rootAspects = new HashSet<>();
    rootAspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(rootRef)).thenReturn(rootAspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_NAME)).thenReturn(
      "Root"
    );

    Set<QName> childAspects = new HashSet<>();
    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getAspects(childRef)).thenReturn(childAspects);
    when(nodeService.getPrimaryParent(childRef)).thenReturn(
      new ChildAssociationRef(
        ContentModel.ASSOC_CONTAINS,
        rootRef,
        ContentModel.PROP_NAME,
        childRef
      )
    );
    when(nodeService.getProperty(childRef, ContentModel.PROP_NAME)).thenReturn(
      "doc.pdf"
    );

    SimplePath path = new SimplePath(nodeService, childRef);

    assertNotNull(path.getParent());
    assertEquals("/Root/doc.pdf", path.toString());
  }

  @Test
  public void testConstructor_whenForcedParent_thenUsesIt()
    throws PathNotFoundException {
    Set<QName> rootAspects = new HashSet<>();
    rootAspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(rootRef)).thenReturn(rootAspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_NAME)).thenReturn(
      "Root"
    );

    Set<QName> childAspects = new HashSet<>();
    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getAspects(childRef)).thenReturn(childAspects);
    when(nodeService.getProperty(childRef, ContentModel.PROP_NAME)).thenReturn(
      "file.txt"
    );

    SimplePath path = new SimplePath(nodeService, rootRef, childRef);

    assertEquals("/Root/file.txt", path.toString());
  }

  @Test
  public void testConstructor_whenEventType_thenUsesTitle()
    throws PathNotFoundException {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(EventModel.TYPE_EVENT);
    when(nodeService.getAspects(rootRef)).thenReturn(aspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_TITLE)).thenReturn(
      "My Event"
    );

    SimplePath path = new SimplePath(nodeService, rootRef);

    assertEquals("My Event", path.getName());
  }

  @Test
  public void testConstructor_whenDirectoryServiceType_thenNameIsDirectory()
    throws PathNotFoundException {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(rootRef)).thenReturn(
      CircabcModel.TYPE_DIRECTORY_SERVICE
    );
    when(nodeService.getAspects(rootRef)).thenReturn(aspects);

    SimplePath path = new SimplePath(nodeService, rootRef);

    assertEquals("Directory", path.getName());
  }

  @Test(expected = PathNotFoundException.class)
  public void testConstructor_whenNoPrimaryParent_thenThrows()
    throws PathNotFoundException {
    Set<QName> aspects = new HashSet<>();

    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getAspects(childRef)).thenReturn(aspects);
    when(nodeService.getPrimaryParent(childRef)).thenReturn(null);

    new SimplePath(nodeService, childRef);
  }

  @Test
  public void testConstructor_whenMultilingualContainer_thenResolvesViaTranslation()
    throws PathNotFoundException {
    NodeRef mlRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ml-id"
    );
    NodeRef translationRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "trans-id"
    );

    Set<QName> mlAspects = new HashSet<>();
    when(nodeService.getType(mlRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );
    when(nodeService.getAspects(mlRef)).thenReturn(mlAspects);
    when(nodeService.getProperty(mlRef, ContentModel.PROP_NAME)).thenReturn(
      "MLDoc"
    );

    ChildAssociationRef transAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_MULTILINGUAL_CHILD,
      mlRef,
      ContentModel.PROP_NAME,
      translationRef
    );
    when(
      nodeService.getChildAssocs(
        mlRef,
        ContentModel.ASSOC_MULTILINGUAL_CHILD,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(List.of(transAssoc));

    // The translation's parent is the root
    when(nodeService.getPrimaryParent(translationRef)).thenReturn(
      new ChildAssociationRef(
        ContentModel.ASSOC_CONTAINS,
        rootRef,
        ContentModel.PROP_NAME,
        translationRef
      )
    );

    // Root setup
    Set<QName> rootAspects = new HashSet<>();
    rootAspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);
    when(nodeService.getType(rootRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(rootRef)).thenReturn(rootAspects);
    when(nodeService.getProperty(rootRef, ContentModel.PROP_NAME)).thenReturn(
      "Root"
    );

    SimplePath path = new SimplePath(nodeService, mlRef);

    assertEquals("/Root/MLDoc", path.toString());
  }

  @Test
  public void testConstructor_whenMultilingualContainerEmpty_thenParentIsNull()
    throws PathNotFoundException {
    NodeRef mlRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ml-id"
    );

    Set<QName> mlAspects = new HashSet<>();
    when(nodeService.getType(mlRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );
    when(nodeService.getAspects(mlRef)).thenReturn(mlAspects);
    when(nodeService.getProperty(mlRef, ContentModel.PROP_NAME)).thenReturn(
      "EmptyML"
    );
    when(
      nodeService.getChildAssocs(
        mlRef,
        ContentModel.ASSOC_MULTILINGUAL_CHILD,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    SimplePath path = new SimplePath(nodeService, mlRef);

    assertNull(path.getParent());
    assertEquals("/EmptyML", path.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_whenNullNodeRef_thenThrows()
    throws PathNotFoundException {
    new SimplePath(nodeService, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_whenNullNodeService_thenThrows()
    throws PathNotFoundException {
    new SimplePath(null, rootRef);
  }
}
