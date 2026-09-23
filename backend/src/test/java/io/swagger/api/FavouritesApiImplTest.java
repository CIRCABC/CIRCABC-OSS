package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.SimpleId;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class FavouritesApiImplTest {

  private FavouritesApiImpl favouritesApi;
  private FavouritesService favouritesService;
  private NodesApi nodesApi;

  @Before
  public void setUp() throws Exception {
    favouritesApi = new FavouritesApiImpl();
    favouritesService = mock(FavouritesService.class);
    nodesApi = mock(NodesApi.class);

    setField(favouritesApi, "favouritesService", favouritesService);
    setField(favouritesApi, "nodesApi", nodesApi);
  }

  @Test
  public void testUsersUserIdFavouritesGet_whenFavouritesExist_thenReturnsPagedNodes() {
    String userName = "testUser";
    NodeRef nodeRef1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node1"
    );
    NodeRef nodeRef2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node2"
    );

    PersonFavourite fav1 = mock(PersonFavourite.class);
    PersonFavourite fav2 = mock(PersonFavourite.class);
    when(fav1.getNodeRef()).thenReturn(nodeRef1);
    when(fav2.getNodeRef()).thenReturn(nodeRef2);

    @SuppressWarnings("unchecked")
    PagingResults<PersonFavourite> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(Arrays.asList(fav1, fav2));
    when(pagingResults.getTotalResultCount()).thenReturn(new Pair<>(2, 2));

    when(
      favouritesService.getPagedFavourites(
        eq(userName),
        anySet(),
        anyList(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    Node node1 = new Node();
    Node node2 = new Node();
    when(nodesApi.getNode(nodeRef1)).thenReturn(node1);
    when(nodesApi.getNode(nodeRef2)).thenReturn(node2);

    PagedNodes result = favouritesApi.usersUserIdFavouritesGet(userName, 0, 10);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertEquals(Long.valueOf(2), result.getTotal());
    assertSame(node1, result.getData().get(0));
    assertSame(node2, result.getData().get(1));
  }

  @Test
  public void testUsersUserIdFavouritesGet_whenNoFavourites_thenReturnsEmptyResult() {
    String userName = "testUser";

    @SuppressWarnings("unchecked")
    PagingResults<PersonFavourite> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(List.of());
    when(pagingResults.getTotalResultCount()).thenReturn(new Pair<>(0, 0));

    when(
      favouritesService.getPagedFavourites(
        eq(userName),
        anySet(),
        anyList(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    PagedNodes result = favouritesApi.usersUserIdFavouritesGet(userName, 0, 10);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(Long.valueOf(0), result.getTotal());
  }

  @Test
  public void testUsersUserIdFavouritesGet_whenTotalCountMismatch_thenTotalIsZero() {
    String userName = "testUser";

    @SuppressWarnings("unchecked")
    PagingResults<PersonFavourite> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(List.of());
    when(pagingResults.getTotalResultCount()).thenReturn(new Pair<>(5, 10));

    when(
      favouritesService.getPagedFavourites(
        eq(userName),
        anySet(),
        anyList(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    PagedNodes result = favouritesApi.usersUserIdFavouritesGet(userName, 0, 10);

    assertEquals(Long.valueOf(0), result.getTotal());
  }

  @Test
  public void testUsersUserIdFavouritesGet_whenPagination_thenCorrectPagingRequest() {
    String userName = "testUser";

    @SuppressWarnings("unchecked")
    PagingResults<PersonFavourite> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(List.of());
    when(pagingResults.getTotalResultCount()).thenReturn(new Pair<>(0, 0));

    when(
      favouritesService.getPagedFavourites(
        eq(userName),
        anySet(),
        anyList(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    favouritesApi.usersUserIdFavouritesGet(userName, 2, 5);

    verify(favouritesService).getPagedFavourites(
      eq(userName),
      anySet(),
      anyList(),
      argThat(pr -> pr.getSkipCount() == 10 && pr.getMaxItems() == 5)
    );
  }

  @Test
  public void testUsersUserIdFavouritesNodeIdDelete_whenCalled_thenRemovesFavourite() {
    String userName = "testUser";
    String nodeId = "abc-123";

    favouritesApi.usersUserIdFavouritesNodeIdDelete(userName, nodeId);

    verify(favouritesService).removeFavourite(
      eq(userName),
      argThat(ref -> ref.getId().equals(nodeId))
    );
  }

  @Test
  public void testUsersUserIdFavouritesPost_whenCalled_thenAddsFavourite() {
    String userName = "testUser";
    SimpleId simpleId = new SimpleId();
    simpleId.setId("xyz-789");

    favouritesApi.usersUserIdFavouritesPost(userName, simpleId);

    verify(favouritesService).addFavourite(
      eq(userName),
      argThat(ref -> ref.getId().equals("xyz-789"))
    );
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
