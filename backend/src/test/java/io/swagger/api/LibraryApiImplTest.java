package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Node;
import io.swagger.model.Profile;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;

public class LibraryApiImplTest {

  private LibraryApiImpl libraryApi;
  private NodeService nodeService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;
  private NodesApi nodesApi;
  private ProfilesApi profilesApi;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  @Before
  public void setUp() throws Exception {
    libraryApi = new LibraryApiImpl();

    nodeService = mock(NodeService.class);
    searchService = mock(SearchService.class);
    apiToolBox = mock(ApiToolBox.class);
    nodesApi = mock(NodesApi.class);
    profilesApi = mock(ProfilesApi.class);

    setField("nodeService", nodeService);
    setField("searchService", searchService);
    setField("apiToolBox", apiToolBox);
    setField("nodesApi", nodesApi);
    setField("profilesApi", profilesApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LibraryApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(libraryApi, value);
  }

  @Test
  public void testGetLockedNodes_whenResultsExist_thenReturnsNodes() {
    when(apiToolBox.getPathFromSpaceRef(TEST_REF, true)).thenReturn(
      "/app:company_home/cm:circabc"
    );

    NodeRef lockedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "locked-id"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(lockedRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    Node expectedNode = new Node();
    when(nodesApi.getNode(lockedRef)).thenReturn(expectedNode);

    List<Node> result = libraryApi.getLockedNodes(TEST_ID);

    assertEquals(1, result.size());
    assertSame(expectedNode, result.get(0));
    verify(searchService).query(any(SearchParameters.class));
  }

  @Test
  public void testGetLockedNodes_whenNoResults_thenReturnsEmptyList() {
    when(apiToolBox.getPathFromSpaceRef(TEST_REF, true)).thenReturn(
      "/app:company_home"
    );

    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.emptyList());
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    List<Node> result = libraryApi.getLockedNodes(TEST_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetSharedNodes_whenResultsExist_thenReturnsParentNodes() {
    when(apiToolBox.getPathFromSpaceRef(TEST_REF, true)).thenReturn(
      "/app:company_home/cm:circabc"
    );

    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(containerRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(childAssoc);

    Node expectedNode = new Node();
    when(nodesApi.getNode(parentRef)).thenReturn(expectedNode);

    List<Node> result = libraryApi.getSharedNodes(TEST_ID);

    assertEquals(1, result.size());
    assertSame(expectedNode, result.get(0));
    verify(nodeService).getPrimaryParent(containerRef);
  }

  @Test
  public void testGetSharedNodes_whenNoResults_thenReturnsEmptyList() {
    when(apiToolBox.getPathFromSpaceRef(TEST_REF, true)).thenReturn(
      "/app:company_home"
    );

    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.emptyList());
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    List<Node> result = libraryApi.getSharedNodes(TEST_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetSharedProfiles_whenExportedProfilesExist_thenReturnsOnlyExported() {
    Profile exported = mock(Profile.class);
    when(exported.getExported()).thenReturn(Boolean.TRUE);

    Profile notExported = mock(Profile.class);
    when(notExported.getExported()).thenReturn(Boolean.FALSE);

    Profile nullExported = mock(Profile.class);
    when(nullExported.getExported()).thenReturn(null);

    List<Profile> allProfiles = Arrays.asList(
      exported,
      notExported,
      nullExported
    );
    when(profilesApi.groupsIdProfilesGet(TEST_ID, null, false)).thenReturn(
      allProfiles
    );

    List<Profile> result = libraryApi.getSharedProfiles(TEST_ID);

    assertEquals(1, result.size());
    assertSame(exported, result.get(0));
  }

  @Test
  public void testGetSharedProfiles_whenNoExportedProfiles_thenReturnsEmptyList() {
    Profile notExported = mock(Profile.class);
    when(notExported.getExported()).thenReturn(Boolean.FALSE);

    when(profilesApi.groupsIdProfilesGet(TEST_ID, null, false)).thenReturn(
      Collections.singletonList(notExported)
    );

    List<Profile> result = libraryApi.getSharedProfiles(TEST_ID);

    assertTrue(result.isEmpty());
  }
}
