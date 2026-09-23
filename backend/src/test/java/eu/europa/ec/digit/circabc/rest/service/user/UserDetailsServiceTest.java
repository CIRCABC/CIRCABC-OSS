package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.api.CircabcApi;
import io.swagger.model.alfresco.UserModel;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class UserDetailsServiceTest {

  private UserDetailsServiceImpl service;
  private NodeService nodeService;
  private PersonService personService;
  private CircabcApi circabcApi;
  private NodePreferencesService nodePreferencesService;

  private NodeRef personRef;

  @Before
  public void setUp() throws Exception {
    service = new UserDetailsServiceImpl();
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);
    circabcApi = mock(CircabcApi.class);
    nodePreferencesService = mock(NodePreferencesService.class);

    setField("nodeService", nodeService);
    setField("personService", personService);
    setField("circabcApi", circabcApi);
    setField("nodePreferencesService", nodePreferencesService);

    personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserDetailsServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetUserDetails_byNodeRef_returnsPopulatedDetails()
    throws Exception {
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_USERNAME)
    ).thenReturn("jdoe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("jdoe@ec.europa.eu");
    when(nodeService.getProperty(personRef, UserModel.PROP_PHONE)).thenReturn(
      "+32123456"
    );
    when(
      nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR)
    ).thenReturn(Collections.emptyList());

    NodeRef rootRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(rootRef);
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        rootRef,
        "users",
        "preferences",
        "avatar"
      )
    ).thenReturn(null);

    UserDetails result = service.getUserDetails(personRef);

    assertEquals("jdoe", result.getUserName());
    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("jdoe@ec.europa.eu", result.getEmail());
    assertEquals("+32123456", result.getPhone());
  }

  @Test
  public void testGetUserDetails_byUsername_whenUserExists() throws Exception {
    when(personService.personExists("jdoe")).thenReturn(true);
    when(personService.getPerson("jdoe")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_USERNAME)
    ).thenReturn("jdoe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("jdoe@ec.europa.eu");
    when(nodeService.getProperty(personRef, UserModel.PROP_PHONE)).thenReturn(
      null
    );
    when(
      nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR)
    ).thenReturn(Collections.emptyList());

    NodeRef rootRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(rootRef);
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        rootRef,
        "users",
        "preferences",
        "avatar"
      )
    ).thenReturn(null);

    UserDetails result = service.getUserDetails("jdoe");

    assertEquals("jdoe", result.getUserName());
  }

  @Test(expected = InvalidArgumentException.class)
  public void testGetUserDetails_byUsername_whenUserNotFound() {
    when(personService.personExists("unknown")).thenReturn(false);
    service.getUserDetails("unknown");
  }

  @Test
  public void testGetAvatar_whenAssociationExists_returnsAvatarRef() {
    NodeRef avatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-id"
    );
    AssociationRef assocRef = new AssociationRef(
      null,
      personRef,
      ContentModel.ASSOC_AVATAR,
      avatarRef
    );
    when(
      nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR)
    ).thenReturn(List.of(assocRef));

    NodeRef result = service.getAvatar(personRef);

    assertEquals(avatarRef, result);
  }

  @Test
  public void testGetAvatar_whenNoAssociation_returnsDefaultAvatar()
    throws Exception {
    when(
      nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR)
    ).thenReturn(Collections.emptyList());

    NodeRef rootRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    NodeRef defaultAvatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "default-avatar"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(rootRef);
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        rootRef,
        "users",
        "preferences",
        "avatar"
      )
    ).thenReturn(defaultAvatarRef);

    NodeRef result = service.getAvatar(personRef);

    assertEquals(defaultAvatarRef, result);
  }

  @Test
  public void testGetDefaultAvatar_cachesResult() throws Exception {
    NodeRef rootRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    NodeRef defaultAvatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "default-avatar"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(rootRef);
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        rootRef,
        "users",
        "preferences",
        "avatar"
      )
    ).thenReturn(defaultAvatarRef);

    NodeRef first = service.getDefaultAvatar();
    NodeRef second = service.getDefaultAvatar();

    assertEquals(defaultAvatarRef, first);
    assertSame(first, second);
    verify(nodePreferencesService, times(1)).getDefaultConfigurationFile(
      rootRef,
      "users",
      "preferences",
      "avatar"
    );
  }

  @Test
  public void testRemoveAvatar_removesAssociationsAndChildren() {
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-img"
    );
    NodeRef avatarTarget = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-target"
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(
      nodeService.getChildAssocs(
        personRef,
        ContentModel.ASSOC_PREFERENCE_IMAGE,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(List.of(childAssoc));

    AssociationRef avatarAssoc = new AssociationRef(
      null,
      personRef,
      ContentModel.ASSOC_AVATAR,
      avatarTarget
    );
    when(
      nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR)
    ).thenReturn(List.of(avatarAssoc));

    service.removeAvatar(personRef);

    verify(nodeService).deleteNode(childRef);
    verify(nodeService).removeAssociation(
      personRef,
      avatarTarget,
      ContentModel.ASSOC_AVATAR
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateUserDetails_whenNodeRefMismatch_thenThrows() {
    NodeRef otherRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-id"
    );
    UserDetails details = new UserDetails();
    details.setNodeRef(otherRef);

    service.updateUserDetails(personRef, details);
  }
}
