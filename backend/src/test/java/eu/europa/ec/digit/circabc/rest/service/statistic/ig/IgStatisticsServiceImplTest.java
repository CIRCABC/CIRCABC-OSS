package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.ActivityCountDAO;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.junit.Before;
import org.junit.Test;

public class IgStatisticsServiceImplTest {

  private IgStatisticsServiceImpl service;
  private NodeService nodeService;
  private VersionService versionService;
  private FileFolderService fileFolderService;
  private IgStatisticsDaoService igStatisticsDaoService;
  private LogService logService;
  private CircabcService circabcService;
  private LockService circabcLockService;

  @Before
  public void setUp() throws Exception {
    service = new IgStatisticsServiceImpl();
    nodeService = mock(NodeService.class);
    versionService = mock(VersionService.class);
    fileFolderService = mock(FileFolderService.class);
    igStatisticsDaoService = mock(IgStatisticsDaoService.class);
    logService = mock(LogService.class);
    circabcService = mock(CircabcService.class);
    circabcLockService = mock(LockService.class);

    setField("nodeService", nodeService);
    setField("versionService", versionService);
    setField("fileFolderService", fileFolderService);
    setField("igStatisticsDaoService", igStatisticsDaoService);
    setField("logService", logService);
    setField("circabcService", circabcService);
    setField("circabcLockService", circabcLockService);

    // Initialize AuthenticationUtil for runAs calls
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
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = IgStatisticsServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetIGTitle_whenNodeHasTitle_thenReturnsTitle() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(nodeService.getProperty(igRoot, ContentModel.PROP_TITLE)).thenReturn(
      "My Interest Group"
    );

    String result = service.getIGTitle(igRoot);

    assertEquals("My Interest Group", result);
  }

  @Test
  public void testBuildStatsData_whenDbIdIsNull_thenReturnsNull() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(null);

    IgStatisticsParameter result = service.buildStatsData(igRoot);

    assertNull(result);
  }

  @Test
  public void testBuildStatsData_whenStatisticsUpToDate_thenReturnsCachedData() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    Long igDbId = 100L;
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(igDbId);

    IgStatisticsParameter cached = new IgStatisticsParameter();
    cached.setIgId(igDbId);
    cached.setRequestDate(new Date());
    when(igStatisticsDaoService.getIgStatisticsById(igDbId)).thenReturn(cached);
    // lastUpdate is null means stats are up-to-date
    when(logService.getLastUpdateOnInterestGroup(igDbId)).thenReturn(null);

    IgStatisticsParameter result = service.buildStatsData(igRoot);

    assertNotNull(result);
    assertEquals(igDbId, result.getIgId());
    // Should not recalculate - no insert/update called
    verify(igStatisticsDaoService, never()).insertIGStatistics(
      any(IgStatisticsParameter.class)
    );
    verify(igStatisticsDaoService, never()).updateIGStatistics(
      any(IgStatisticsParameter.class)
    );
  }

  @Test
  public void testBuildStatsData_whenStatisticsOutdated_thenRecalculatesAndUpdates() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    Long igDbId = 100L;
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(igDbId);
    when(nodeService.getProperty(igRoot, ContentModel.PROP_CREATED)).thenReturn(
      new Date()
    );
    when(nodeService.exists(igRoot)).thenReturn(true);
    when(nodeService.getType(igRoot)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getChildAssocs(igRoot)).thenReturn(
      Collections.emptyList()
    );
    when(nodeService.getProperty(igRoot, ContentModel.PROP_NAME)).thenReturn(
      "root"
    );
    when(nodeService.hasAspect(any(NodeRef.class), any())).thenReturn(false);

    // Existing stats that are outdated
    IgStatisticsParameter cached = new IgStatisticsParameter();
    cached.setIgId(igDbId);
    cached.setRequestDate(new Date(1000L)); // old date
    when(igStatisticsDaoService.getIgStatisticsById(igDbId)).thenReturn(cached);
    // lastUpdate is after requestDate -> outdated
    when(logService.getLastUpdateOnInterestGroup(igDbId)).thenReturn(
      new Date()
    );

    when(circabcService.countMembersInIg("ig-1")).thenReturn(5);
    when(circabcLockService.tryLock(anyString())).thenReturn(true);

    IgStatisticsParameter result = service.buildStatsData(igRoot);

    assertNotNull(result);
    assertEquals(igDbId, result.getIgId());
    verify(igStatisticsDaoService).updateIGStatistics(
      any(IgStatisticsParameter.class)
    );
  }

  @Test
  public void testBuildStatsData_whenNoExistingStats_thenInsertsNew() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    Long igDbId = 200L;
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(igDbId);
    when(nodeService.getProperty(igRoot, ContentModel.PROP_CREATED)).thenReturn(
      new Date()
    );
    when(nodeService.exists(igRoot)).thenReturn(true);
    when(nodeService.getType(igRoot)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getChildAssocs(igRoot)).thenReturn(
      Collections.emptyList()
    );
    when(nodeService.getProperty(igRoot, ContentModel.PROP_NAME)).thenReturn(
      "root"
    );

    // No existing stats
    when(igStatisticsDaoService.getIgStatisticsById(igDbId)).thenReturn(null);

    when(circabcService.countMembersInIg("ig-1")).thenReturn(3);
    when(circabcLockService.tryLock(anyString())).thenReturn(true);

    IgStatisticsParameter result = service.buildStatsData(igRoot);

    assertNotNull(result);
    verify(igStatisticsDaoService).insertIGStatistics(
      any(IgStatisticsParameter.class)
    );
  }

  @Test
  public void testGetLibraryStructure_whenLibraryExists_thenReturnsTree() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-1"
    );
    when(
      nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Library")
    ).thenReturn(libraryRef);
    when(fileFolderService.listFolders(libraryRef)).thenReturn(
      Collections.emptyList()
    );

    ServiceTreeRepresentation result = service.getLibraryStructure(igRoot);

    assertNotNull(result);
    assertEquals("library", result.getName());
    assertEquals("Library", result.getChild().getName());
    assertEquals(libraryRef, result.getChild().getNode());
    assertTrue(result.getChild().getChildren().isEmpty());
  }

  @Test
  public void testGetLibraryStructure_whenHasSubfolders_thenBuildsTree() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-1"
    );
    NodeRef subfolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "sub-1"
    );

    when(
      nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Library")
    ).thenReturn(libraryRef);

    FileInfo subfolderInfo = mock(FileInfo.class);
    when(subfolderInfo.getName()).thenReturn("SubFolder");
    when(subfolderInfo.getNodeRef()).thenReturn(subfolderRef);

    List<FileInfo> folders = new ArrayList<>();
    folders.add(subfolderInfo);
    when(fileFolderService.listFolders(libraryRef)).thenReturn(folders);
    when(fileFolderService.listFolders(subfolderRef)).thenReturn(
      Collections.emptyList()
    );

    ServiceTreeRepresentation result = service.getLibraryStructure(igRoot);

    assertEquals(1L, (long) result.getChild().getChildren().size());
    assertEquals("SubFolder", result.getChild().getChildren().get(0).getName());
  }

  @Test
  public void testGetInformationStructure_whenExists_thenReturnsTree() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef infoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-1"
    );
    when(
      nodeService.getChildByName(
        igRoot,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(infoRef);
    when(fileFolderService.listFolders(infoRef)).thenReturn(
      Collections.emptyList()
    );

    ServiceTreeRepresentation result = service.getInformationStructure(igRoot);

    assertNotNull(result);
    assertEquals("Information", result.getChild().getName());
    assertEquals(infoRef, result.getChild().getNode());
  }

  @Test
  public void testGetNewsgroupsStructure_whenExists_thenReturnsTree() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef ngRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ng-1"
    );
    when(
      nodeService.getChildByName(
        igRoot,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(ngRef);
    when(fileFolderService.listFolders(ngRef)).thenReturn(
      Collections.emptyList()
    );

    ServiceTreeRepresentation result = service.getNewsgroupsStructure(igRoot);

    assertNotNull(result);
    assertEquals("newsgroup", result.getName());
    assertEquals("Newsgroup", result.getChild().getName());
    assertEquals(ngRef, result.getChild().getNode());
  }

  @Test
  public void testGetListOfActivityCount_whenCalled_thenDelegatesToLogService() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    Long igDbId = 42L;
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(igDbId);

    List<ActivityCountDAO> expected = new ArrayList<>();
    expected.add(new ActivityCountDAO());
    when(logService.getListOfActivityCountForInterestGroup(igDbId)).thenReturn(
      expected
    );

    List<ActivityCountDAO> result = service.getListOfActivityCount(igRoot);

    assertEquals(expected, result);
    verify(logService).getListOfActivityCountForInterestGroup(igDbId);
  }

  @Test
  public void testGetListOfActivityCount_whenEmpty_thenReturnsEmptyList() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    Long igDbId = 42L;
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(igDbId);
    when(logService.getListOfActivityCountForInterestGroup(igDbId)).thenReturn(
      Collections.emptyList()
    );

    List<ActivityCountDAO> result = service.getListOfActivityCount(igRoot);

    assertTrue(result.isEmpty());
  }
}
