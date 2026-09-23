package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class CategoryGroupRequesLinkMethodTest {

  private CategoryGroupRequesLinkMethod method;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    method = new CategoryGroupRequesLinkMethod();
    circabcConfig = mock(CircabcConfig.class);

    Field field = CategoryGroupRequesLinkMethod.class.getDeclaredField(
      "circabcConfig"
    );
    field.setAccessible(true);
    field.set(method, circabcConfig);
  }

  @Test
  public void testGetResult_whenContextEndsWithSlash_thenNoCategoryDoubleSlash() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id-123"
    );

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/ui/category/cat-id-123/group-requests",
      result
    );
  }

  @Test
  public void testGetResult_whenContextDoesNotEndWithSlash_thenSlashAdded() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id-456"
    );

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/ui/category/cat-id-456/group-requests",
      result
    );
  }

  @Test
  public void testGetResult_whenNodeRefIsNull_thenEmptyId() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    String result = method.getResult(null);

    assertEquals(
      "https://circabc.europa.eu/ui/category//group-requests",
      result
    );
  }

  @Test
  public void testGetResult_whenContextIsEmpty_thenSlashCategoryUsed() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id-789"
    );

    String result = method.getResult(nodeRef);

    assertEquals(
      "https://circabc.europa.eu/category/cat-id-789/group-requests",
      result
    );
  }
}
