package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class GroupNodeLinkMethodTest {

  private GroupNodeLinkMethod groupNodeLinkMethod;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    groupNodeLinkMethod = new GroupNodeLinkMethod();
    circabcConfig = mock(CircabcConfig.class);

    Field field = GroupNodeLinkMethod.class.getDeclaredField("circabcConfig");
    field.setAccessible(true);
    field.set(groupNodeLinkMethod, circabcConfig);
  }

  @Test
  public void testGetResult_whenContextEndsWithSlash_thenNoExtraSlash() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "abc-123"
    );

    String result = groupNodeLinkMethod.getResult(nodeRef);

    assertEquals("https://circabc.europa.eu/ui/group/abc-123", result);
  }

  @Test
  public void testGetResult_whenContextDoesNotEndWithSlash_thenSlashAdded() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "def-456"
    );

    String result = groupNodeLinkMethod.getResult(nodeRef);

    assertEquals("https://circabc.europa.eu/ui/group/def-456", result);
  }

  @Test
  public void testGetResult_whenEmptyContext_thenSlashBeforeGroup() {
    when(circabcConfig.getNewUiUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getNewUiContext()).thenReturn("");

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ghi-789"
    );

    String result = groupNodeLinkMethod.getResult(nodeRef);

    assertEquals("https://circabc.europa.eu/group/ghi-789", result);
  }
}
