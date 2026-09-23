package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.auto.upload.AutoUploadManagementService;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class AutoUploadApiImplTest {

  private static final String TEST_NODE_ID =
    "00000000-0000-0000-0000-000000000001";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_NODE_ID
  );

  private AutoUploadApiImpl autoUploadApi;
  private AutoUploadManagementService autoUploadManagementService;
  private NodeService nodeService;
  private ApiToolBox apiToolBox;

  @Before
  public void setUp() throws Exception {
    autoUploadApi = new AutoUploadApiImpl();
    autoUploadManagementService = mock(AutoUploadManagementService.class);
    nodeService = mock(NodeService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField(
      autoUploadApi,
      "autoUploadManagementService",
      autoUploadManagementService
    );
    setField(autoUploadApi, "nodeService", nodeService);
    setField(autoUploadApi, "apiToolBox", apiToolBox);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      TEST_NODE_REF
    );
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  // --- getAutoUploadEntries ---

  @Test
  public void testGetAutoUploadEntries_whenValidIgAndAmountZero_thenReturnsAll()
    throws SQLException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);

    Configuration conf1 = new Configuration();
    Configuration conf2 = new Configuration();
    List<Configuration> configs = Arrays.asList(conf1, conf2);

    when(
      autoUploadManagementService.listConfigurations(TEST_NODE_REF.toString())
    ).thenReturn(configs);

    PagedAutoUploadConfiguration result = autoUploadApi.getAutoUploadEntries(
      TEST_NODE_ID,
      0,
      0
    );

    assertEquals(2, result.getTotal());
    assertEquals(2, result.getData().size());
  }

  @Test
  public void testGetAutoUploadEntries_whenPaginated_thenReturnsSubset()
    throws SQLException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);

    Configuration conf1 = new Configuration();
    Configuration conf2 = new Configuration();
    Configuration conf3 = new Configuration();
    List<Configuration> configs = Arrays.asList(conf1, conf2, conf3);

    when(
      autoUploadManagementService.listConfigurations(TEST_NODE_REF.toString())
    ).thenReturn(configs);

    PagedAutoUploadConfiguration result = autoUploadApi.getAutoUploadEntries(
      TEST_NODE_ID,
      1,
      1
    );

    assertEquals(3, result.getTotal());
    assertEquals(1, result.getData().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAutoUploadEntries_whenIgDoesNotExist_thenThrows() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    autoUploadApi.getAutoUploadEntries(TEST_NODE_ID, 0, 10);
  }

  @Test
  public void testGetAutoUploadEntries_whenSqlException_thenReturnsEmpty()
    throws SQLException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(
      autoUploadManagementService.listConfigurations(TEST_NODE_REF.toString())
    ).thenThrow(new SQLException("DB error"));

    PagedAutoUploadConfiguration result = autoUploadApi.getAutoUploadEntries(
      TEST_NODE_ID,
      0,
      0
    );

    assertEquals(0, result.getTotal());
    assertTrue(result.getData().isEmpty());
  }

  // --- removeAutoUploadEntry ---

  @Test
  public void testRemoveAutoUploadEntry_whenValid_thenDeletesCalled()
    throws SQLException {
    Configuration conf = new Configuration();
    conf.setIdConfiguration(42L);
    conf.setIgName(TEST_NODE_REF.toString());
    when(autoUploadManagementService.getConfigurationById(42)).thenReturn(conf);

    autoUploadApi.removeAutoUploadEntry(TEST_NODE_ID, 42L);

    verify(autoUploadManagementService).deleteConfiguration(conf);
  }

  @Test(expected = SwaggerRuntimeException.class)
  public void testRemoveAutoUploadEntry_whenSqlException_thenThrows()
    throws SQLException {
    when(autoUploadManagementService.getConfigurationById(1)).thenThrow(
      new SQLException("DB error")
    );

    autoUploadApi.removeAutoUploadEntry(TEST_NODE_ID, 1L);
  }

  // --- toggleAutoUploadEntry ---

  @Test
  public void testToggleAutoUploadEntry_whenEnable_thenStatusSetTo1()
    throws SQLException {
    Configuration conf = new Configuration();
    conf.setStatus(0);
    conf.setIgName(TEST_NODE_REF.toString());
    when(autoUploadManagementService.getConfigurationById(5)).thenReturn(conf);

    autoUploadApi.toggleAutoUploadEntry(TEST_NODE_ID, 5L, true);

    assertEquals(Integer.valueOf(1), conf.getStatus());
    verify(autoUploadManagementService).updateConfiguration(conf);
  }

  @Test
  public void testToggleAutoUploadEntry_whenDisable_thenStatusSetTo0()
    throws SQLException {
    Configuration conf = new Configuration();
    conf.setStatus(1);
    conf.setIgName(TEST_NODE_REF.toString());
    when(autoUploadManagementService.getConfigurationById(5)).thenReturn(conf);

    autoUploadApi.toggleAutoUploadEntry(TEST_NODE_ID, 5L, false);

    assertEquals(Integer.valueOf(0), conf.getStatus());
    verify(autoUploadManagementService).updateConfiguration(conf);
  }

  @Test(expected = SwaggerRuntimeException.class)
  public void testToggleAutoUploadEntry_whenSqlException_thenThrows()
    throws SQLException {
    when(autoUploadManagementService.getConfigurationById(1)).thenThrow(
      new SQLException("DB error")
    );

    autoUploadApi.toggleAutoUploadEntry(TEST_NODE_ID, 1L, true);
  }

  // --- getAutoUploadEntry ---

  @Test
  public void testGetAutoUploadEntry_whenNodeExists_thenReturnsConfig()
    throws SQLException {
    Configuration expected = new Configuration();
    expected.setIgName(TEST_NODE_REF.toString());
    when(
      autoUploadManagementService.getConfigurationByNodeRef(TEST_NODE_REF)
    ).thenReturn(expected);

    Configuration result = autoUploadApi.getAutoUploadEntry(
      TEST_NODE_ID,
      TEST_NODE_ID
    );

    assertSame(expected, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAutoUploadEntry_whenNodeDoesNotExist_thenThrows() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    autoUploadApi.getAutoUploadEntry(TEST_NODE_ID, TEST_NODE_ID);
  }

  @Test(expected = SwaggerRuntimeException.class)
  public void testGetAutoUploadEntry_whenSqlException_thenThrows()
    throws SQLException {
    when(
      autoUploadManagementService.getConfigurationByNodeRef(TEST_NODE_REF)
    ).thenThrow(new SQLException("DB error"));

    autoUploadApi.getAutoUploadEntry(TEST_NODE_ID, TEST_NODE_ID);
  }

  // --- addAutoUploadEntry ---

  @Test(expected = IllegalArgumentException.class)
  public void testAddAutoUploadEntry_whenNullBody_thenThrows() {
    autoUploadApi.addAutoUploadEntry(TEST_NODE_ID, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAutoUploadEntry_whenEmptyBody_thenThrows() {
    autoUploadApi.addAutoUploadEntry(TEST_NODE_ID, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAutoUploadEntry_whenInvalidJson_thenThrows() {
    autoUploadApi.addAutoUploadEntry(TEST_NODE_ID, "not valid json");
  }
}
