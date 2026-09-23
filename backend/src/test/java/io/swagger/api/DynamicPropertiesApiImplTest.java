package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyType;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import io.swagger.model.I18nProperty;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DynamicPropertiesApiImplTest {

  private static final String NODE_ID = "test-node-id";
  private static final NodeRef NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    NODE_ID
  );

  private DynamicPropertiesApiImpl api;
  private DynamicPropertyService dynamicPropertyService;

  @Before
  public void setUp() throws Exception {
    api = new DynamicPropertiesApiImpl();
    dynamicPropertyService = mock(DynamicPropertyService.class);

    Field field = DynamicPropertiesApiImpl.class.getDeclaredField(
      "dynamicPropertiesService"
    );
    field.setAccessible(true);
    field.set(api, dynamicPropertyService);
  }

  @Test
  public void testGroupsIdDynpropsGet_whenPropertiesExist_thenReturnsList() {
    DynamicProperty dp = mockDynamicProperty(
      "prop-id",
      1L,
      "Property 1",
      DynamicPropertyType.TEXT_FIELD
    );
    when(dynamicPropertyService.getDynamicProperties(NODE_REF)).thenReturn(
      Collections.singletonList(dp)
    );

    List<DynamicPropertyDefinition> result = api.groupsIdDynpropsGet(NODE_ID);

    assertEquals(1, result.size());
    assertEquals("prop-id", result.get(0).getId());
    assertEquals(Long.valueOf(1L), result.get(0).getIndex());
    assertEquals("Property 1", result.get(0).getName());
    assertEquals("TEXT_FIELD", result.get(0).getPropertyType());
  }

  @Test
  public void testGroupsIdDynpropsGet_whenEmpty_thenReturnsEmptyList() {
    when(dynamicPropertyService.getDynamicProperties(NODE_REF)).thenReturn(
      Collections.emptyList()
    );

    List<DynamicPropertyDefinition> result = api.groupsIdDynpropsGet(NODE_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testDynpropsIdGet_whenPropertyExists_thenReturnsDefinition() {
    DynamicProperty dp = mockDynamicProperty(
      NODE_ID,
      2L,
      "My Prop",
      DynamicPropertyType.SELECTION
    );
    when(dynamicPropertyService.getDynamicPropertyByID(NODE_REF)).thenReturn(
      dp
    );

    DynamicPropertyDefinition result = api.dynpropsIdGet(NODE_ID);

    assertNotNull(result);
    assertEquals(NODE_ID, result.getId());
    assertEquals("SELECTION", result.getPropertyType());
  }

  @Test
  public void testDynpropsIdDelete_whenCalled_thenDeletesProperty() {
    DynamicProperty dp = mock(DynamicProperty.class);
    when(dynamicPropertyService.getDynamicPropertyByID(NODE_REF)).thenReturn(
      dp
    );

    api.dynpropsIdDelete(NODE_ID);

    verify(dynamicPropertyService).deleteDynamicProperty(dp);
  }

  @Test
  public void testGroupsIdDynpropsPost_whenTextField_thenCreatesProperty() {
    DynamicPropertyDefinition body = new DynamicPropertyDefinition();
    body.setTitle(new I18nProperty());
    body.setPropertyType("TEXT_FIELD");

    DynamicProperty created = mockDynamicProperty(
      "new-id",
      3L,
      "New Prop",
      DynamicPropertyType.TEXT_FIELD
    );
    when(
      dynamicPropertyService.addDynamicProperty(eq(NODE_REF), any())
    ).thenReturn(created);

    DynamicPropertyDefinition result = api.groupsIdDynpropsPost(NODE_ID, body);

    assertNotNull(result);
    assertEquals("new-id", result.getId());
    verify(dynamicPropertyService).addDynamicProperty(eq(NODE_REF), any());
  }

  @Test
  public void testGroupsIdDynpropsPost_whenSelection_thenFormatsValues() {
    DynamicPropertyDefinition body = new DynamicPropertyDefinition();
    body.setTitle(new I18nProperty());
    body.setPropertyType("SELECTION");
    body.setPossibleyValues(Arrays.asList("val1", "val2", "val3"));

    DynamicProperty created = mockDynamicProperty(
      "sel-id",
      4L,
      "Selection Prop",
      DynamicPropertyType.SELECTION
    );
    when(
      dynamicPropertyService.addDynamicProperty(eq(NODE_REF), any())
    ).thenReturn(created);

    DynamicPropertyDefinition result = api.groupsIdDynpropsPost(NODE_ID, body);

    assertNotNull(result);
    verify(dynamicPropertyService).addDynamicProperty(eq(NODE_REF), any());
  }

  @Test
  public void testDynpropsIdPut_whenTextFieldType_thenUpdatesLabelOnly() {
    String propId = "put-node-id";
    NodeRef propRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      propId
    );

    DynamicPropertyDefinition body = new DynamicPropertyDefinition();
    body.setId(propId);
    body.setTitle(new I18nProperty());
    body.setPropertyType("TEXT_FIELD");
    body.setUpdatedValues(Collections.emptyList());

    DynamicProperty dp = mockDynamicProperty(
      propId,
      1L,
      "Updated",
      DynamicPropertyType.TEXT_FIELD
    );
    when(dynamicPropertyService.getDynamicPropertyByID(propRef)).thenReturn(dp);

    DynamicPropertyDefinition result = api.dynpropsIdPut(propId, body);

    verify(dynamicPropertyService).updateDynamicPropertyLabel(eq(dp), any());
    verify(dynamicPropertyService, never()).updateDynamicPropertyValidValues(
      any(),
      any(),
      anyBoolean(),
      any(),
      any()
    );
    assertNotNull(result);
  }

  @Test
  public void testDynpropsIdPut_whenSelectionType_thenUpdatesValues() {
    String propId = "sel-node-id";
    NodeRef propRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      propId
    );

    DynamicPropertyDefinitionUpdatedValues unchanged =
      new DynamicPropertyDefinitionUpdatedValues();
    unchanged.setStatus("");
    unchanged.setOld("existing");
    unchanged.setNewValue("existing");

    DynamicPropertyDefinitionUpdatedValues edited =
      new DynamicPropertyDefinitionUpdatedValues();
    edited.setStatus("edited");
    edited.setOld("oldVal");
    edited.setNewValue("newVal");

    DynamicPropertyDefinitionUpdatedValues deleted =
      new DynamicPropertyDefinitionUpdatedValues();
    deleted.setStatus("deleted");
    deleted.setOld("removedVal");
    deleted.setNewValue("");

    DynamicPropertyDefinition body = new DynamicPropertyDefinition();
    body.setId(propId);
    body.setTitle(new I18nProperty());
    body.setPropertyType("SELECTION");
    body.setUpdatedValues(Arrays.asList(unchanged, edited, deleted));

    DynamicProperty dp = mockDynamicProperty(
      propId,
      1L,
      "Sel Prop",
      DynamicPropertyType.SELECTION
    );
    when(dynamicPropertyService.getDynamicPropertyByID(propRef)).thenReturn(dp);

    DynamicPropertyDefinition result = api.dynpropsIdPut(propId, body);

    verify(dynamicPropertyService).updateDynamicPropertyLabel(eq(dp), any());
    verify(dynamicPropertyService).updateDynamicPropertyValidValues(
      eq(dp),
      any(),
      eq(true),
      any(),
      any()
    );
    assertNotNull(result);
  }

  private DynamicProperty mockDynamicProperty(
    String id,
    Long index,
    String name,
    DynamicPropertyType type
  ) {
    DynamicProperty dp = mock(DynamicProperty.class);
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(dp.getId()).thenReturn(ref);
    when(dp.getIndex()).thenReturn(index);
    when(dp.getName()).thenReturn(name);
    when(dp.getType()).thenReturn(type);
    when(dp.getLabel()).thenReturn(new MLText("label"));
    when(dp.getListOfValidValues()).thenReturn(Collections.emptyList());
    return dp;
  }
}
