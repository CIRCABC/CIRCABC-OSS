package eu.europa.ec.digit.circabc.rest.dynamic.authority.ibatis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.dynamic.authority.CircabcPermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class CircabcDynamicAuthorityDAOImplTest {

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
    perm.setLibraryPermission("LibAdmin");
    List<CircabcPermission> expected = Arrays.asList(perm);

    doReturn(expected)
      .when(template)
      .selectList(
        eq("dynamic-authority.select_GroupPermission"),
        any(Map.class)
      );

    List<CircabcPermission> result = dao.getGroupPermission(
      "workspace://SpacesStore/node-id",
      "user1"
    );

    assertEquals(1, result.size());
    assertEquals("LibAdmin", result.get(0).getLibraryPermission());
    verify(template).selectList(
      eq("dynamic-authority.select_GroupPermission"),
      any(Map.class)
    );
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
      "workspace://SpacesStore/node-id",
      "user1"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testIsCategoryAdmin_whenAdmin_thenReturnsTrue() {
    when(
      template.selectOne(
        eq("dynamic-authority.select_IsCategoryAdmin"),
        any(Map.class)
      )
    ).thenReturn(1);

    boolean result = dao.isCategoryAdmin(
      "workspace://SpacesStore/node-id",
      "admin"
    );

    assertTrue(result);
  }

  @Test
  public void testIsCategoryAdmin_whenNotAdmin_thenReturnsFalse() {
    when(
      template.selectOne(
        eq("dynamic-authority.select_IsCategoryAdmin"),
        any(Map.class)
      )
    ).thenReturn(0);

    boolean result = dao.isCategoryAdmin(
      "workspace://SpacesStore/node-id",
      "user1"
    );

    assertFalse(result);
  }

  @Test
  public void testIsCategoryAdmin_whenNegativeResult_thenReturnsFalse() {
    when(
      template.selectOne(
        eq("dynamic-authority.select_IsCategoryAdmin"),
        any(Map.class)
      )
    ).thenReturn(-1);

    boolean result = dao.isCategoryAdmin(
      "workspace://SpacesStore/node-id",
      "user1"
    );

    assertFalse(result);
  }
}
