package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.ActivityCountDAO;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.junit.Before;
import org.junit.Test;

public class IgStatisticsServiceTest {

  private IgStatisticsServiceImpl service;
  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private IgStatisticsDaoService igStatisticsDaoService;
  private LogService logService;
  private CircabcService circabcService;
  private LockService circabcLockService;

  @Before
  public void setUp() throws Exception {
    service = new IgStatisticsServiceImpl();
    nodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    igStatisticsDaoService = mock(IgStatisticsDaoService.class);
    logService = mock(LogService.class);
    circabcService = mock(CircabcService.class);
    circabcLockService = mock(LockService.class);

    setField("nodeService", nodeService);
    setField("versionService", mock(VersionService.class));
    setField("fileFolderService", fileFolderService);
    setField("igStatisticsDaoService", igStatisticsDaoService);
    setField("logService", logService);
    setField("circabcService", circabcService);
    setField("circabcLockService", circabcLockService);

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
  public void testGetIGTitle() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(nodeService.getProperty(igRoot, ContentModel.PROP_TITLE)).thenReturn(
      "Test IG"
    );
    assertEquals("Test IG", service.getIGTitle(igRoot));
  }

  @Test
  public void testGetLibraryStructure() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef libraryNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-1"
    );
    when(
      nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Library")
    ).thenReturn(libraryNode);
    when(fileFolderService.listFolders(libraryNode)).thenReturn(
      Collections.emptyList()
    );

    ServiceTreeRepresentation result = service.getLibraryStructure(igRoot);
    assertNotNull(result);
    assertEquals("Library", result.getChild().getName());
  }

  @Test
  public void testGetListOfActivityCount() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    List<ActivityCountDAO> expected = Collections.singletonList(
      new ActivityCountDAO()
    );
    when(logService.getListOfActivityCountForInterestGroup(100L)).thenReturn(
      expected
    );

    List<ActivityCountDAO> result = service.getListOfActivityCount(igRoot);
    assertEquals(expected, result);
  }

  @Test
  public void testBuildStatsData_whenIgDbIdIsNull_thenReturnsNull() {
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID)
    ).thenReturn(null);

    assertNull(service.buildStatsData(igRoot));
  }
}
