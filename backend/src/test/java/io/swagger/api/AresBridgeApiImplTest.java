package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeDaoService;
import eu.europa.ec.digit.circabc.rest.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.model.RepositoryConfiguration;
import io.swagger.model.db.AresBridgeDAO;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AresBridgeApiImplTest {

  private AresBridgeApiImpl aresBridgeApi;
  private ExternalRepositoriesManagementService externalRepositoriesManagementService;
  private AresBridgeDaoService aresBridgeDaoService;

  private static final String API_KEY = "testApiKey";
  private static final String SECRET = "testSecret";
  private static final String BASE_URL = "http://localhost";
  private static final String NODE_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    aresBridgeApi = new AresBridgeApiImpl();
    externalRepositoriesManagementService = mock(
      ExternalRepositoriesManagementService.class
    );
    aresBridgeDaoService = mock(AresBridgeDaoService.class);

    setField(
      "externalRepositoriesManagementService",
      externalRepositoriesManagementService
    );
    setField("aresBridgeDaoService", aresBridgeDaoService);
    setField("apiKey", API_KEY);
    setField("secret", SECRET);
    setField("baseURL", BASE_URL);
    setField("applicationName", "CIRCABC");
    setField("serviceURL", "http://localhost/service");
    setField("uiURL", "http://localhost/ui");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AresBridgeApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aresBridgeApi, value);
  }

  @Test
  public void testGetTicket_whenValidParams_thenReturnsToken() {
    String result = aresBridgeApi.getTicket("2026-01-01", "GET", "/api/test");
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testGetExternalRepositories_whenCalled_thenDelegatesToService() {
    List<RepositoryConfiguration> expected = Collections.singletonList(
      new RepositoryConfiguration()
    );
    String expectedNodeRef = "workspace://SpacesStore/" + NODE_ID;
    when(
      externalRepositoriesManagementService.getConfiguredRepositories(
        expectedNodeRef
      )
    ).thenReturn(expected);

    Collection<RepositoryConfiguration> result =
      aresBridgeApi.getExternalRepositories(NODE_ID);

    assertEquals(expected, result);
    verify(externalRepositoriesManagementService).getConfiguredRepositories(
      expectedNodeRef
    );
  }

  @Test
  public void testAddExternalRepositories_whenCalled_thenDelegatesToService() {
    String expectedNodeRef = "workspace://SpacesStore/" + NODE_ID;

    aresBridgeApi.addExternalRepositories(NODE_ID, "AresBridge");

    verify(externalRepositoriesManagementService).addRepository(
      eq(expectedNodeRef),
      any(RepositoryConfiguration.class)
    );
  }

  @Test
  public void testDeleteExternalRepository_whenCalled_thenDelegatesToService() {
    String expectedNodeRef = "workspace://SpacesStore/" + NODE_ID;

    aresBridgeApi.deleteExternalRepository(NODE_ID, "AresBridge");

    verify(externalRepositoriesManagementService).removeRepository(
      expectedNodeRef,
      "AresBridge"
    );
  }

  @Test
  public void testGetAvailableExternalRepositories_thenReturnsAresBridge() {
    Collection<String> result =
      aresBridgeApi.getAvailableExternalRepositories();

    assertEquals(1, result.size());
    assertTrue(result.contains("AresBridge"));
  }

  @Test
  public void testValidateAuthorizationHeader_whenValid_thenReturnsTrue() {
    String date = "2026-01-01";
    String path = "/api/test";
    String token = aresBridgeApi.getTicket(date, "POST", path);
    String authHeader = "AresBridge:" + API_KEY + ":" + token;

    boolean result = aresBridgeApi.validateAuthorizationHeader(
      date,
      authHeader,
      path
    );

    assertTrue(result);
  }

  @Test
  public void testValidateAuthorizationHeader_whenInvalidHeader_thenReturnsFalse() {
    boolean result = aresBridgeApi.validateAuthorizationHeader(
      "2026-01-01",
      "InvalidHeader",
      "/api/test"
    );

    assertFalse(result);
  }

  @Test
  public void testValidateToken_whenValid_thenReturnsTrue() {
    String date = "2026-01-01";
    String path = "/api/test";
    String method = "GET";
    String token = aresBridgeApi.getTicket(date, method, path);

    boolean result = aresBridgeApi.validateToken(date, token, path, method);

    assertTrue(result);
  }

  @Test
  public void testValidateToken_whenInvalid_thenReturnsFalse() {
    boolean result = aresBridgeApi.validateToken(
      "2026-01-01",
      "invalidToken",
      "/api/test",
      "GET"
    );

    assertFalse(result);
  }

  @Test
  public void testSaveTransaction_whenCalled_thenDelegatesToDao() {
    aresBridgeApi.saveTransaction(
      "group1",
      "repo1",
      "tx1",
      "node1",
      "1.0",
      "doc.pdf"
    );

    verify(aresBridgeDaoService).saveRequest(
      "group1",
      "tx1",
      "node1",
      "1.0",
      "doc.pdf"
    );
  }

  @Test
  public void testNodeLog_whenCalled_thenDelegatesToDao() {
    List<AresBridgeDAO> expected = Collections.singletonList(
      new AresBridgeDAO()
    );
    when(aresBridgeDaoService.getResponsesByNodeId("node1")).thenReturn(
      expected
    );

    Collection<AresBridgeDAO> result = aresBridgeApi.nodeLog("node1", "name");

    assertEquals(expected, result);
  }

  @Test
  public void testGroupLog_whenCalled_thenDelegatesToDao() {
    List<AresBridgeDAO> expected = Collections.singletonList(
      new AresBridgeDAO()
    );
    when(aresBridgeDaoService.getResponsesByGroupId("group1")).thenReturn(
      expected
    );

    Collection<AresBridgeDAO> result = aresBridgeApi.groupLog("group1", "name");

    assertEquals(expected, result);
  }
}
