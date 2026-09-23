package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.InformationPage;
import io.swagger.model.News;
import io.swagger.model.PagedNews;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class InformationApiImplTest {

  private InformationApiImpl informationApi;
  private NodeService secureNodeService;
  private FileFolderService fileFolderService;
  private SpacesApi spacesApi;
  private PermissionService permissionService;
  private AuthorityService authorityService;
  private AuthenticationService authenticationService;

  private static final String TEST_ID = "test-id-123";
  private static final NodeRef IG_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );
  private static final NodeRef INFO_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "info-id"
  );

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    informationApi = new InformationApiImpl();

    secureNodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    spacesApi = mock(SpacesApi.class);
    permissionService = mock(PermissionService.class);
    authorityService = mock(AuthorityService.class);
    authenticationService = mock(AuthenticationService.class);

    setField("secureNodeService", secureNodeService);
    setField("fileFolderService", fileFolderService);
    setField("spacesApi", spacesApi);
    setField("permissionService", permissionService);
    setField("authorityService", authorityService);
    setField("authenticationService", authenticationService);

    informationApi.setWebRoolUrl("http://localhost:8080/");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = InformationApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(informationApi, value);
  }

  @Test
  public void testGroupsIdInformationGet_whenHttpIndexPage_thenReturnsHttpUrl() {
    String httpUrl = "http://external.example.com/page.html";
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "categ-id"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);

    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);
    when(
      secureNodeService.getProperty(INFO_REF, CircabcModel.PROP_INF_INDEX_PAGE)
    ).thenReturn(httpUrl);
    // indexFileFound: split("Information/") on httpUrl gives length 1, path = httpUrl
    // tokens: "http:", "", "external.example.com", "page.html"
    // We need getChildByName to return non-null for each token
    when(
      secureNodeService.getChildByName(
        any(NodeRef.class),
        eq(ContentModel.ASSOC_CONTAINS),
        anyString()
      )
    ).thenReturn(INFO_REF);
    // Re-stub the specific call for "Information" child lookup
    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);
    when(
      secureNodeService.getProperty(IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("MyIG");
    when(secureNodeService.getPrimaryParent(IG_REF)).thenReturn(parentAssoc);
    when(parentAssoc.getParentRef()).thenReturn(categRef);
    when(
      secureNodeService.getProperty(categRef, ContentModel.PROP_NAME)
    ).thenReturn("MyCategory");
    when(
      secureNodeService.getProperty(INFO_REF, CircabcModel.PROP_INF_ADAPT)
    ).thenReturn("true");
    when(
      secureNodeService.getProperty(
        INFO_REF,
        CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION
      )
    ).thenReturn("false");
    when(permissionService.getPermissions(INFO_REF)).thenReturn(
      Collections.emptySet()
    );

    InformationPage result = informationApi.groupsIdInformationGet(TEST_ID);

    // The URL contains "http" so it's returned directly
    assertEquals(httpUrl, result.getUrl());
    assertTrue(result.getAdapt());
    assertFalse(result.getDisplayOldInformation());
  }

  @Test
  public void testGroupsIdInformationGet_whenEmptyIndexPage_thenUrlIsNull() {
    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);
    when(
      secureNodeService.getProperty(INFO_REF, CircabcModel.PROP_INF_INDEX_PAGE)
    ).thenReturn("");
    when(
      secureNodeService.getProperty(INFO_REF, CircabcModel.PROP_INF_ADAPT)
    ).thenReturn("false");
    when(
      secureNodeService.getProperty(
        INFO_REF,
        CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION
      )
    ).thenReturn(null);
    when(permissionService.getPermissions(INFO_REF)).thenReturn(
      Collections.emptySet()
    );

    InformationPage result = informationApi.groupsIdInformationGet(TEST_ID);

    assertNull(result.getUrl());
  }

  @Test
  public void testNewsIdDelete_whenValidId_thenDeletesNode() {
    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "news-id"
    );

    informationApi.newsIdDelete("news-id");

    verify(secureNodeService).deleteNode(newsRef);
  }

  @Test
  public void testNewsIdGet_whenNoAspect_thenReturnsNull() {
    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "news-id"
    );
    when(
      secureNodeService.hasAspect(newsRef, CircabcModel.ASPECT_INFORMATION_NEWS)
    ).thenReturn(false);

    News result = informationApi.newsIdGet("news-id");

    assertNull(result);
  }

  @Test
  public void testNewsIdGet_whenValidNews_thenReturnsPopulatedNews() {
    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "news-id"
    );
    Date now = new Date();

    when(
      secureNodeService.hasAspect(newsRef, CircabcModel.ASPECT_INFORMATION_NEWS)
    ).thenReturn(true);
    when(
      secureNodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_CONTENT)
    ).thenReturn("News content");
    when(
      secureNodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_PATTERN)
    ).thenReturn("text");
    when(
      secureNodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_LAYOUT)
    ).thenReturn("normal");
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_TITLE)
    ).thenReturn("News Title");
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_MODIFIED)
    ).thenReturn(now);
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_CREATED)
    ).thenReturn(now);
    when(
      secureNodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_SIZE)
    ).thenReturn("3");
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_MODIFIER)
    ).thenReturn("admin");
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_CREATOR)
    ).thenReturn("admin");
    when(
      secureNodeService.getProperty(newsRef, ContentModel.PROP_OWNER)
    ).thenReturn("admin");
    when(
      secureNodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_URL)
    ).thenReturn(null);
    when(permissionService.getAllSetPermissions(newsRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(Collections.emptySet());

    News result = informationApi.newsIdGet("news-id");

    assertNotNull(result);
    assertEquals("News content", result.getContent());
    assertEquals("news-id", result.getId());
    assertEquals(News.PatternEnum.TEXT, result.getPattern());
    assertEquals(News.LayoutEnum.NORMAL, result.getLayout());
    assertEquals(Integer.valueOf(3), result.getSize());
    assertEquals("admin", result.getModifier());
    assertEquals("admin", result.getCreator());
  }

  @Test
  public void testGroupsIdInformationPut_whenAdaptAndDisplaySet_thenUpdatesProperties() {
    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);

    InformationPage body = new InformationPage();
    body.setAdapt(true);
    body.setDisplayOldInformation(false);

    informationApi.groupsIdInformationPut(TEST_ID, body);

    verify(secureNodeService).setProperty(
      INFO_REF,
      CircabcModel.PROP_INF_ADAPT,
      true
    );
    verify(secureNodeService).setProperty(
      INFO_REF,
      CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION,
      false
    );
  }

  @Test
  public void testGroupsIdInformationPut_whenNullValues_thenSkipsUpdate() {
    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);

    InformationPage body = new InformationPage();
    body.setDisplayOldInformation(null);

    informationApi.groupsIdInformationPut(TEST_ID, body);

    verify(secureNodeService, never()).setProperty(
      eq(INFO_REF),
      eq(CircabcModel.PROP_INF_ADAPT),
      any()
    );
    verify(secureNodeService, never()).setProperty(
      eq(INFO_REF),
      eq(CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION),
      any()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGroupsIdInformationNewsGet_whenNoNews_thenReturnsEmptyList() {
    when(
      secureNodeService.getChildByName(
        IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(INFO_REF);

    PagingResults<FileInfo> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(Collections.emptyList());
    when(
      fileFolderService.list(
        eq(INFO_REF),
        eq(false),
        eq(true),
        any(),
        any(),
        any()
      )
    ).thenReturn(pagingResults);
    when(
      secureNodeService.getChildAssocs(eq(INFO_REF), any(Set.class))
    ).thenReturn(Collections.emptyList());

    PagedNews result = informationApi.groupsIdInformationNewsGet(
      TEST_ID,
      10,
      0
    );

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(Long.valueOf(0), result.getTotal());
  }
}
