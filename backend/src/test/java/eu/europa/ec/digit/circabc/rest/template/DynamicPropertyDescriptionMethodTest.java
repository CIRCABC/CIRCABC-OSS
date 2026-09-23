package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DynamicPropertyDescriptionMethodTest {

  private DynamicPropertyDescriptionMethod method;
  private DynamicPropertyService dynamicPropertyService;

  @Before
  public void setUp() throws Exception {
    method = new DynamicPropertyDescriptionMethod();
    dynamicPropertyService = mock(DynamicPropertyService.class);

    Field field = DynamicPropertyDescriptionMethod.class.getDeclaredField(
      "dynamicPropertyService"
    );
    field.setAccessible(true);
    field.set(method, dynamicPropertyService);
  }

  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsLabels()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    MLText label1 = new MLText(Locale.ENGLISH, "Label One");
    MLText label2 = new MLText(Locale.ENGLISH, "Label Two");

    DynamicProperty dp1 = mock(DynamicProperty.class);
    DynamicProperty dp2 = mock(DynamicProperty.class);
    when(dp1.getLabel()).thenReturn(label1);
    when(dp2.getLabel()).thenReturn(label2);

    when(dynamicPropertyService.getDynamicProperties(nodeRef)).thenReturn(
      Arrays.asList(dp1, dp2)
    );

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.exec(
      Collections.singletonList(beanModel)
    );

    assertEquals(2, result.size());
    assertEquals("Label One", result.get(0));
    assertEquals("Label Two", result.get(1));
  }

  @Test
  public void testExec_whenArgIsTemplateNode_thenReturnsLabels()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(nodeRef);

    MLText label = new MLText(Locale.ENGLISH, "Dynamic Label");
    DynamicProperty dp = mock(DynamicProperty.class);
    when(dp.getLabel()).thenReturn(label);

    when(dynamicPropertyService.getDynamicProperties(nodeRef)).thenReturn(
      Collections.singletonList(dp)
    );

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.exec(
      Collections.singletonList(beanModel)
    );

    assertEquals(1, result.size());
    assertEquals("Dynamic Label", result.get(0));
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyList()
    throws TemplateModelException {
    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.exec(Collections.emptyList());

    assertTrue(result.isEmpty());
  }

  @Test
  public void testExec_whenWrappedObjectIsUnknownType_thenReturnsEmptyList()
    throws TemplateModelException {
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn("some string");

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.exec(
      Collections.singletonList(beanModel)
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testExec_whenNoDynamicProperties_thenReturnsEmptyList()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    when(dynamicPropertyService.getDynamicProperties(nodeRef)).thenReturn(
      Collections.emptyList()
    );

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) method.exec(
      Collections.singletonList(beanModel)
    );

    assertTrue(result.isEmpty());
  }
}
