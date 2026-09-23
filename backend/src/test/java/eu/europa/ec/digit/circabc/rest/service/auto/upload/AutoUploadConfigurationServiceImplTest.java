package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Configuration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class AutoUploadConfigurationServiceImplTest {

  private AutoUploadConfigurationServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    service = new AutoUploadConfigurationServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testRegisterConfiguration_whenCalled_thenInsertsConfig() {
    Configuration config = new Configuration();
    service.registerConfiguration(config);
    verify(sqlSessionTemplate).insert(
      "AutoUploadConfiguration.insert_configuration",
      config
    );
  }

  @Test
  public void testListConfigurations_whenIgNameProvided_thenReturnsConfigs() {
    Configuration config = new Configuration();
    doReturn(Collections.singletonList(config))
      .when(sqlSessionTemplate)
      .selectList(
        "AutoUploadConfiguration.select_all_configurations",
        "testIg"
      );

    List<Configuration> result = service.listConfigurations("testIg");

    assertEquals(1, result.size());
    assertSame(config, result.get(0));
  }

  @Test
  public void testListConfigurations_whenNoResults_thenReturnsEmptyList() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(
        "AutoUploadConfiguration.select_all_configurations",
        "emptyIg"
      );

    List<Configuration> result = service.listConfigurations("emptyIg");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testDeleteConfiguration_whenCalled_thenDeletesConfig() {
    Configuration config = new Configuration();
    service.deleteConfiguration(config);
    verify(sqlSessionTemplate).delete(
      "AutoUploadConfiguration.delete_configuration",
      config
    );
  }

  @Test
  public void testUpdateConfiguration_whenCalled_thenUpdatesConfig() {
    Configuration config = new Configuration();
    service.updateConfiguration(config);
    verify(sqlSessionTemplate).update(
      "AutoUploadConfiguration.update_configuration",
      config
    );
  }

  @Test
  public void testGetConfigurationById_whenIdProvided_thenReturnsConfig() {
    Configuration config = new Configuration();
    when(
      sqlSessionTemplate.selectOne(
        "AutoUploadConfiguration.select_configuration_by_id",
        "42"
      )
    ).thenReturn(config);

    Configuration result = service.getConfigurationById(42);

    assertSame(config, result);
  }

  @Test
  public void testGetConfigurationById_whenNotFound_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectOne(
        "AutoUploadConfiguration.select_configuration_by_id",
        "999"
      )
    ).thenReturn(null);

    Configuration result = service.getConfigurationById(999);

    assertNull(result);
  }

  @Test
  public void testGetConfigurationByNodeRef_whenNodeRefProvided_thenReturnsConfig() {
    Configuration config = new Configuration();
    NodeRef nodeRef = new NodeRef("workspace://SpacesStore/test-id");
    when(
      sqlSessionTemplate.selectOne(
        "AutoUploadConfiguration.select_configuration_by_file_ref",
        nodeRef.toString()
      )
    ).thenReturn(config);

    Configuration result = service.getConfigurationByNodeRef(nodeRef);

    assertSame(config, result);
  }

  @Test
  public void testGetAllConfigurations_whenCalled_thenReturnsAllConfigs() {
    List<Configuration> configs = Arrays.asList(
      new Configuration(),
      new Configuration()
    );
    doReturn(configs)
      .when(sqlSessionTemplate)
      .selectList("AutoUploadConfiguration.select_all_configurations_all");

    List<Configuration> result = service.getAllConfigurations();

    assertEquals(2, result.size());
  }
}
