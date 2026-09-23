package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.dynamic.authority.ibatis.CircabcDynamicAuthorityDAOImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class CircabcDynamicAuthorityDAOTest {

  private CircabcDynamicAuthorityDAOImpl dao;
  private SqlSessionTemplate template;

  @Before
  public void setUp() {
    template = mock(SqlSessionTemplate.class);
    dao = new CircabcDynamicAuthorityDAOImpl();
    dao.setSqlSessionTemplate(template);
  }

  @Test
  public void testGetGroupPermission_whenResultsExist_thenReturnsList() {
    CircabcPermission perm = new CircabcPermission();
    perm.setLibraryPermission("LibManageOwn");
    List<CircabcPermission> expected = List.of(perm);

    doReturn(expected)
      .when(template)
      .selectList(
        eq("dynamic-authority.select_GroupPermission"),
        any(Map.class)
      );

    List<CircabcPermission> result = dao.getGroupPermission(
      "workspace://SpacesStore/test-id",
      "testuser"
    );

    assertEquals(1, result.size());
    assertEquals("LibManageOwn", result.get(0).getLibraryPermission());
  }

  @Test
  public void testGetGroupPermission_whenNoResults_thenReturnsEmptyList() {
    doReturn(Collections.emptyList())
      .when(template)
      .selectList(
        eq("dynamic-authority.select_GroupPermission"),
        any(Map.class)
      );

    List<CircabcPermission> result = dao.getGroupPermission(
      "workspace://SpacesStore/test-id",
      "testuser"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testIsCategoryAdmin_whenResultGreaterThanZero_thenReturnsTrue() {
    doReturn(1)
      .when(template)
      .selectOne(
        eq("dynamic-authority.select_IsCategoryAdmin"),
        any(Map.class)
      );

    assertTrue(dao.isCategoryAdmin("workspace://SpacesStore/test-id", "admin"));
  }

  @Test
  public void testIsCategoryAdmin_whenResultIsZero_thenReturnsFalse() {
    doReturn(0)
      .when(template)
      .selectOne(
        eq("dynamic-authority.select_IsCategoryAdmin"),
        any(Map.class)
      );

    assertFalse(dao.isCategoryAdmin("workspace://SpacesStore/test-id", "user"));
  }

  @Test
  public void testGetGroupPermission_passesCorrectParams() {
    doReturn(Collections.emptyList())
      .when(template)
      .selectList(anyString(), any(Map.class));

    dao.getGroupPermission("workspace://SpacesStore/node-123", "john");

    verify(template).selectList(
      eq("dynamic-authority.select_GroupPermission"),
      argThat(arg -> {
        Map<String, Object> params = (Map<String, Object>) arg;
        return (
          "workspace://SpacesStore/node-123".equals(params.get("nodeRef")) &&
          "john".equals(params.get("userName"))
        );
      })
    );
  }
}
