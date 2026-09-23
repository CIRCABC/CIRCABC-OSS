package eu.europa.ec.digit.circabc.rest.model;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class PostTypeTest {

  private PostType postType;
  private NodeService nodeService;

  @Before
  public void setUp() throws Exception {
    postType = new PostType();
    nodeService = mock(NodeService.class);
    setField("nodeService", nodeService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostType.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(postType, value);
  }

  @Test
  public void testMakeVersionnable_whenNotArchivedAndNotVersionable_thenAddsAspect() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
    when(childAssocRef.getChildRef()).thenReturn(postRef);
    when(
      nodeService.hasAspect(postRef, ContentModel.ASPECT_VERSIONABLE)
    ).thenReturn(false);

    postType.makeVersionnable(childAssocRef);

    verify(nodeService).addAspect(
      eq(postRef),
      eq(ContentModel.ASPECT_VERSIONABLE),
      anyMap()
    );
  }

  @Test
  public void testMakeVersionnable_whenAlreadyVersionable_thenDoesNotAddAspect() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
    when(childAssocRef.getChildRef()).thenReturn(postRef);
    when(
      nodeService.hasAspect(postRef, ContentModel.ASPECT_VERSIONABLE)
    ).thenReturn(true);

    postType.makeVersionnable(childAssocRef);

    verify(nodeService, never()).addAspect(
      any(NodeRef.class),
      any(QName.class),
      anyMap()
    );
  }

  @Test
  public void testMakeVersionnable_whenArchived_thenDoesNotAddAspect() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "post-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
    when(childAssocRef.getChildRef()).thenReturn(postRef);

    postType.makeVersionnable(childAssocRef);

    verify(nodeService, never()).hasAspect(
      any(NodeRef.class),
      any(QName.class)
    );
    verify(nodeService, never()).addAspect(
      any(NodeRef.class),
      any(QName.class),
      anyMap()
    );
  }

  @Test
  public void testAddBProperties_whenNotArchivedAndNoAspect_thenAddsAspect() {
    NodeRef topicRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "topic-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
    when(childAssocRef.getChildRef()).thenReturn(topicRef);
    when(
      nodeService.hasAspect(
        topicRef,
        io.swagger.model.alfresco.DocumentModel.ASPECT_BPROPERTIES
      )
    ).thenReturn(false);

    postType.addBProperties(childAssocRef);

    verify(nodeService).addAspect(
      eq(topicRef),
      eq(io.swagger.model.alfresco.DocumentModel.ASPECT_BPROPERTIES),
      anyMap()
    );
  }

  @Test
  public void testAddBProperties_whenArchived_thenDoesNotAddAspect() {
    NodeRef topicRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "topic-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
    when(childAssocRef.getChildRef()).thenReturn(topicRef);

    postType.addBProperties(childAssocRef);

    verify(nodeService, never()).addAspect(
      any(NodeRef.class),
      any(QName.class),
      anyMap()
    );
  }
}
