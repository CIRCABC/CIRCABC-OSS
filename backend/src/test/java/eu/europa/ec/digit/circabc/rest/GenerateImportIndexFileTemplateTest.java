package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GenerateImportIndexFileTemplateTest {

  private GenerateImportIndexFileTemplate webScript;
  private GroupsApi groupsApi;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    webScript = new GenerateImportIndexFileTemplate();
    groupsApi = mock(GroupsApi.class);
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    Field field = GenerateImportIndexFileTemplate.class.getDeclaredField(
      "groupsApi"
    );
    field.setAccessible(true);
    field.set(webScript, groupsApi);
  }

  @Test
  public void testExecute_whenSuccess_thenDelegatestoGroupsApi()
    throws Exception {
    webScript.execute(req, res);

    verify(groupsApi).generateImportIndexFileTemplate(res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenGroupsApiThrows_thenThrowsIOException()
    throws Exception {
    doThrow(new RuntimeException("fail"))
      .when(groupsApi)
      .generateImportIndexFileTemplate(res);

    webScript.execute(req, res);
  }

  @Test
  public void testExecute_whenException_thenMessageIsWrapped()
    throws Exception {
    doThrow(new RuntimeException("original error"))
      .when(groupsApi)
      .generateImportIndexFileTemplate(res);

    try {
      webScript.execute(req, res);
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("Could not generate index file.", e.getMessage());
      assertEquals("original error", e.getCause().getMessage());
    }
  }
}
