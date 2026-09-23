package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.exception.DynamicPropertyException;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DynamicPropertyModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class DynamicPropertyServiceImplTest {

  private DynamicPropertyServiceImpl service;
  private NodeService nodeService;
  private PermissionService permissionService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;
  private SimpleCache<Long, List<DynamicProperty>> dynamicPropertyCache;

  private NodeRef igNodeRef;
  private NodeRef containerNodeRef;
  private NodeRef dpNodeRef;

  private static final QName ASSOC_IG_DYNAMICPROPERTIESCONTAINER =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "igDpContainer"
    );

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    service = new DynamicPropertyServiceImpl();
    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    searchService = mock(SearchService.class);
    apiToolBox = mock(ApiToolBox.class);
    dynamicPropertyCache = mock(SimpleCache.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("searchService", searchService);
    setField("apiToolBox", apiToolBox);
    service.setDynamicPropertyCache(dynamicPropertyCache);

    igNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");
    containerNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    dpNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dp-id");

    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DynamicPropertyServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test(expected = NullPointerException.class)
  public void testAddDynamicProperty_whenNullProperty_thenThrowsNPE() {
    service.addDynamicProperty(igNodeRef, null);
  }

  @Test
  public void testAddDynamicProperty_whenValidProperty_thenCreatesNode() {
    MLText label = new MLText(Locale.ENGLISH, "Test");
    DynamicProperty dp = new DynamicPropertyImpl(
      null,
      null,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );

    // container exists
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerNodeRef);
    when(
      nodeService.getChildAssocs(
        igNodeRef,
        ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(containerAssoc));

    // no existing children (index computation)
    when(
      nodeService.getChildAssocs(
        containerNodeRef,
        DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    // createNode
    ChildAssociationRef newAssoc = mock(ChildAssociationRef.class);
    when(newAssoc.getChildRef()).thenReturn(dpNodeRef);
    when(
      nodeService.createNode(
        eq(containerNodeRef),
        eq(DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY),
        eq(DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY),
        eq(DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY),
        any()
      )
    ).thenReturn(newAssoc);

    // getDynamicPropertyByID expectations
    when(nodeService.getType(dpNodeRef)).thenReturn(
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY
    );
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      )
    ).thenReturn(1L);
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL
      )
    ).thenReturn(label);
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_TYPE
      )
    ).thenReturn("TEXT_FIELD");

    DynamicProperty result = service.addDynamicProperty(igNodeRef, dp);

    assertNotNull(result);
    assertEquals(Long.valueOf(1L), result.getIndex());
    assertEquals(DynamicPropertyType.TEXT_FIELD, result.getType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddDynamicProperty_whenIndexAlreadyUsed_thenThrows() {
    MLText label = new MLText(Locale.ENGLISH, "Test");
    DynamicProperty dp = new DynamicPropertyImpl(
      1L,
      null,
      label,
      DynamicPropertyType.TEXT_FIELD,
      null
    );

    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerNodeRef);
    when(
      nodeService.getChildAssocs(
        igNodeRef,
        ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(containerAssoc));

    // existing child with same index
    NodeRef existingDp = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-dp"
    );
    ChildAssociationRef existingAssoc = mock(ChildAssociationRef.class);
    when(existingAssoc.getChildRef()).thenReturn(existingDp);
    when(
      nodeService.getChildAssocs(
        containerNodeRef,
        DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(existingAssoc));
    when(
      nodeService.getProperty(
        existingDp,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      )
    ).thenReturn(1L);

    service.addDynamicProperty(igNodeRef, dp);
  }

  @Test
  public void testGetDynamicProperties_whenCached_thenReturnsCachedList() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    List<DynamicProperty> cached = Collections.singletonList(
      mock(DynamicProperty.class)
    );
    when(dynamicPropertyCache.get(100L)).thenReturn(cached);

    List<DynamicProperty> result = service.getDynamicProperties(igNodeRef);

    assertSame(cached, result);
    verify(nodeService, never()).getChildAssocs(
      any(NodeRef.class),
      eq(ASSOC_IG_DYNAMICPROPERTIESCONTAINER),
      eq(RegexQNamePattern.MATCH_ALL)
    );
  }

  @Test
  public void testGetDynamicProperties_whenNoContainer_thenReturnsEmptyList() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(dynamicPropertyCache.get(100L)).thenReturn(null);
    when(
      nodeService.getChildAssocs(
        igNodeRef,
        ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    List<DynamicProperty> result = service.getDynamicProperties(igNodeRef);

    assertTrue(result.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDynamicPropertyByID_whenWrongType_thenThrows() {
    QName wrongType = QName.createQName("http://wrong", "type");
    when(nodeService.getType(dpNodeRef)).thenReturn(wrongType);

    service.getDynamicPropertyByID(dpNodeRef);
  }

  @Test
  public void testGetDynamicPropertyByID_whenValid_thenReturnsProperty() {
    MLText label = new MLText(Locale.ENGLISH, "Label");
    when(nodeService.getType(dpNodeRef)).thenReturn(
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY
    );
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      )
    ).thenReturn(2L);
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL
      )
    ).thenReturn(label);
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_TYPE
      )
    ).thenReturn("SELECTION");
    when(
      nodeService.getProperty(
        dpNodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_VALID_VALUES
      )
    ).thenReturn("opt1\nopt2");

    DynamicProperty result = service.getDynamicPropertyByID(dpNodeRef);

    assertEquals(Long.valueOf(2L), result.getIndex());
    assertEquals(label, result.getLabel());
    assertEquals(DynamicPropertyType.SELECTION, result.getType());
    assertEquals("opt1\nopt2", result.getValidValues());
  }

  @Test(expected = NullPointerException.class)
  public void testUpdateDynamicPropertyLabel_whenNullDp_thenThrowsNPE() {
    service.updateDynamicPropertyLabel(null, new MLText(Locale.ENGLISH, "x"));
  }

  @Test
  public void testUpdateDynamicPropertyLabel_whenValid_thenSetsProperty() {
    MLText newLabel = new MLText(Locale.ENGLISH, "New Label");
    DynamicProperty dp = new DynamicPropertyImpl(
      1L,
      dpNodeRef,
      new MLText(),
      DynamicPropertyType.TEXT_FIELD,
      null
    );

    // getIgFromDynProp traversal
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(igNodeRef);
    when(
      nodeService.hasAspect(dpNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(nodeService.getPrimaryParent(dpNodeRef)).thenReturn(parentAssoc);
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    service.updateDynamicPropertyLabel(dp, newLabel);

    verify(nodeService).setProperty(
      dpNodeRef,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL,
      newLabel
    );
    verify(dynamicPropertyCache).remove(100L);
  }

  @Test
  public void testGetPropertyQname_whenIndex1_thenReturnsFirstDynProp() {
    DynamicProperty dp = new DynamicPropertyImpl(
      1L,
      dpNodeRef,
      new MLText(),
      DynamicPropertyType.TEXT_FIELD,
      null
    );

    QName result = service.getPropertyQname(dp);

    assertNotNull(result);
  }

  @Test(expected = DynamicPropertyException.class)
  public void testGetDynamicPropertyContainer_whenMultipleContainers_thenThrows() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(dynamicPropertyCache.get(100L)).thenReturn(null);

    ChildAssociationRef assoc1 = mock(ChildAssociationRef.class);
    ChildAssociationRef assoc2 = mock(ChildAssociationRef.class);
    when(
      nodeService.getChildAssocs(
        igNodeRef,
        ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Arrays.asList(assoc1, assoc2));

    service.getDynamicProperties(igNodeRef);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDynamicPropertyValidValues_whenInvalidType_thenThrows() {
    DynamicProperty dp = new DynamicPropertyImpl(
      1L,
      dpNodeRef,
      new MLText(),
      DynamicPropertyType.TEXT_FIELD,
      null
    );
    when(nodeService.getType(dpNodeRef)).thenReturn(
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY
    );

    service.updateDynamicPropertyValidValues(
      dp,
      "a\nb",
      false,
      Collections.emptySet(),
      Collections.emptyMap()
    );
  }
}
