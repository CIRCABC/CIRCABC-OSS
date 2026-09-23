package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class EcordaDaoServiceImplTest {

  private EcordaDaoServiceImpl ecordaDaoService;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    ecordaDaoService = new EcordaDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    setField("sqlSessionTemplate", sqlSessionTemplate);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EcordaDaoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(ecordaDaoService, value);
  }

  @Test
  public void testGetEcordaThemaID_whenResultsExist_thenReturnsList() {
    List<String> expected = Arrays.asList("THEMA1", "THEMA2");
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(
        "CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref",
        "workspace://SpacesStore/test-id"
      );

    List<String> result = ecordaDaoService.getEcordaThemaID(
      "workspace://SpacesStore/test-id"
    );

    assertEquals(expected, result);
    verify(sqlSessionTemplate).selectList(
      "CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref",
      "workspace://SpacesStore/test-id"
    );
  }

  @Test
  public void testGetEcordaThemaID_whenNoResults_thenReturnsEmptyList() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(
        "CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref",
        "workspace://SpacesStore/empty-id"
      );

    List<String> result = ecordaDaoService.getEcordaThemaID(
      "workspace://SpacesStore/empty-id"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetEcordaThemaID_whenNullNodeRef_thenDelegatesToTemplate() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList("CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref", null);

    List<String> result = ecordaDaoService.getEcordaThemaID(null);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testSetSqlSessionTemplate_whenCalled_thenFieldIsSet()
    throws Exception {
    EcordaDaoServiceImpl service = new EcordaDaoServiceImpl();
    SqlSessionTemplate template = mock(SqlSessionTemplate.class);

    service.setSqlSessionTemplate(template);

    Field field = EcordaDaoServiceImpl.class.getDeclaredField(
      "sqlSessionTemplate"
    );
    field.setAccessible(true);
    assertSame(template, field.get(service));
  }
}
