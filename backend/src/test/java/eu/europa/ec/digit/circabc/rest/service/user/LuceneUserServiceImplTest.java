package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.alfresco.UserModel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class LuceneUserServiceImplTest {

  private LuceneUserServiceImpl service;
  private PersonService personService;
  private NodeService nodeService;
  private SearchService searchService;

  @Before
  public void setUp() throws Exception {
    service = new LuceneUserServiceImpl();
    personService = mock(PersonService.class);
    nodeService = mock(NodeService.class);
    searchService = mock(SearchService.class);

    setField("personService", personService);
    setField("nodeService", nodeService);
    setField("searchService", searchService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LuceneUserServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetLDAPUserDataByUid_whenPersonExists_thenReturnsUserData() {
    String userId = "testuser";
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );

    when(personService.personExists(userId)).thenReturn(true);
    when(personService.getPerson(userId)).thenReturn(nodeRef);
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL)).thenReturn(
      "john@test.com"
    );
    when(
      nodeService.getProperty(nodeRef, UserModel.PROP_ECAS_USER_NAME)
    ).thenReturn("jdoe");
    when(nodeService.getProperty(nodeRef, UserModel.PROP_DOMAIN)).thenReturn(
      "ec.europa.eu"
    );

    CircabcUserDataBean result = service.getLDAPUserDataByUid(userId);

    assertNotNull(result);
    assertEquals("testuser", result.getUserName());
    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("john@test.com", result.getEmail());
    assertEquals("jdoe", result.getEcasUserName());
    assertEquals("ec.europa.eu", result.getDomain());
  }

  @Test
  public void testGetLDAPUserDataByUid_whenPersonDoesNotExist_thenReturnsNull() {
    when(personService.personExists("unknown")).thenReturn(false);

    CircabcUserDataBean result = service.getLDAPUserDataByUid("unknown");

    assertNull(result);
  }

  @Test
  public void testGetLDAPUserIDByIdMonikerEmailCn_whenUsersFound_thenReturnsUserIds() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user1"
    );
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList(userRef));
    when(nodeService.exists(userRef)).thenReturn(true);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("user1");

    List<String> result = service.getLDAPUserIDByIdMonikerEmailCn(
      "user1",
      null,
      null,
      null,
      false
    );

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0));
  }

  @Test
  public void testGetLDAPUserIDByIdMonikerEmailCn_whenNodeDoesNotExist_thenSkipped() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "deleted"
    );
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList(userRef));
    when(nodeService.exists(userRef)).thenReturn(false);

    List<String> result = service.getLDAPUserIDByIdMonikerEmailCn(
      "user1",
      null,
      null,
      null,
      false
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetUsersByDomainFirstNameLastNameEmail_whenUsersFound_thenReturnsRecords() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user1"
    );
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList(userRef));
    when(nodeService.exists(userRef)).thenReturn(true);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("Jane");
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Smith");
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("jsmith");
    when(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL)).thenReturn(
      "jane@test.com"
    );

    List<SearchResultRecord> result =
      service.getUsersByDomainFirstNameLastNameEmail(
        "ec.europa.eu",
        "Jane",
        false
      );

    assertEquals(1, result.size());
    assertEquals("jsmith", result.get(0).getUserName());
    assertEquals("Jane", result.get(0).getFirstName());
    assertEquals("Smith", result.get(0).getLastName());
    assertEquals("jane@test.com", result.get(0).getEmail());
  }

  @Test
  public void testGetUsersByMailDomain_whenUsersFound_thenReturnsRecords() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user1"
    );
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList(userRef));
    when(nodeService.exists(userRef)).thenReturn(true);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("Bob");
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Jones");
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("bjones");
    when(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL)).thenReturn(
      "bob@test.com"
    );

    List<SearchResultRecord> result = service.getUsersByMailDomain(
      "bob",
      "ec.europa.eu",
      false
    );

    assertEquals(1, result.size());
    assertEquals("bjones", result.get(0).getUserName());
    assertEquals("bob@test.com", result.get(0).getEmail());
  }

  @Test
  public void testGetUsersByMailDomain_whenNoResults_thenReturnsEmptyList() {
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList());

    List<SearchResultRecord> result = service.getUsersByMailDomain(
      "nobody",
      "domain",
      false
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetLDAPUserIDByIdMonikerEmailCn_withConjunction_thenUsesAnd() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user1"
    );
    ResultSet resultSet = mock(ResultSet.class);

    when(searchService.query(any(SearchParameters.class))).thenReturn(
      resultSet
    );
    when(resultSet.getNodeRefs()).thenReturn(Arrays.asList(userRef));
    when(nodeService.exists(userRef)).thenReturn(true);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("user1");

    List<String> result = service.getLDAPUserIDByIdMonikerEmailCn(
      "user1",
      null,
      "test@mail.com",
      "Smith",
      true
    );

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0));
  }
}
