package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.exception.CustomizationException;
import io.swagger.model.alfresco.CircabcModel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LogoPreferencesServiceTest {

  private LogoPreferencesServiceImpl service;
  private NodePreferencesService nodePreferencesService;
  private NodeService nodeService;

  @SuppressWarnings("unchecked")
  private SimpleCache<NodeRef, List<LogoDefinition>> logoCache;

  @SuppressWarnings("unchecked")
  private SimpleCache<NodeRef, DefaultLogoConfiguration> configCache;

  private final NodeRef testRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "test-node"
  );
  private final NodeRef parentRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "parent-node"
  );

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    service = new LogoPreferencesServiceImpl();
    nodePreferencesService = mock(NodePreferencesService.class);
    nodeService = mock(NodeService.class);
    logoCache = mock(SimpleCache.class);
    configCache = mock(SimpleCache.class);

    setField("nodePreferencesService", nodePreferencesService);
    setField("nodeService", nodeService);
    setField("logoCache", logoCache);
    setField("configCache", configCache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LogoPreferencesServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testAddLogo_whenFileValid_thenReturnsLogoDefinition()
    throws Exception {
    File tempFile = File.createTempFile("logo", ".png");
    tempFile.deleteOnExit();
    NodeRef customizationRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "custom-ref"
    );

    when(
      nodePreferencesService.customizationFileExists(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("mylogo.png")
      )
    ).thenReturn(false);
    when(nodePreferencesService.isNodeConfigurable(testRef)).thenReturn(true);
    when(
      nodePreferencesService.addCustomizationFile(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("mylogo.png"),
        eq(tempFile)
      )
    ).thenReturn(customizationRef);

    LogoDefinition result = service.addLogo(testRef, "mylogo.png", tempFile);

    assertNotNull(result);
    assertEquals(customizationRef, result.getReference());
    assertEquals("mylogo.png", result.getName());
    assertEquals(testRef, result.getDefinedOn());
    verify(logoCache).clear();
  }

  @Test(expected = IOException.class)
  public void testAddLogo_whenFileDoesNotExist_thenThrowsIOException()
    throws Exception {
    File nonExistent = new File("/nonexistent/path/logo.png");
    service.addLogo(testRef, "logo.png", nonExistent);
  }

  @Test(expected = CustomizationException.class)
  public void testAddLogo_whenNameAlreadyExists_thenThrowsCustomizationException()
    throws Exception {
    File tempFile = File.createTempFile("logo", ".png");
    tempFile.deleteOnExit();

    when(
      nodePreferencesService.customizationFileExists(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("existing.png")
      )
    ).thenReturn(true);

    service.addLogo(testRef, "existing.png", tempFile);
  }

  @Test
  public void testAddLogo_withInputStream_thenReturnsLogoDefinition()
    throws Exception {
    InputStream is = new ByteArrayInputStream("data".getBytes());
    NodeRef customizationRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "custom-ref"
    );

    when(
      nodePreferencesService.customizationFileExists(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("stream.png")
      )
    ).thenReturn(false);
    when(nodePreferencesService.isNodeConfigurable(testRef)).thenReturn(true);
    when(
      nodePreferencesService.addCustomizationFile(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("stream.png"),
        eq(is)
      )
    ).thenReturn(customizationRef);

    LogoDefinition result = service.addLogo(testRef, "stream.png", is);

    assertNotNull(result);
    assertEquals(customizationRef, result.getReference());
    assertEquals("stream.png", result.getName());
  }

  @Test
  public void testGetAllLogos_whenCached_thenReturnsCachedValue()
    throws Exception {
    List<LogoDefinition> cached = Collections.emptyList();
    when(logoCache.contains(testRef)).thenReturn(true);
    when(logoCache.get(testRef)).thenReturn(cached);

    List<LogoDefinition> result = service.getAllLogos(testRef);

    assertSame(cached, result);
    verifyNoInteractions(nodePreferencesService);
  }

  @Test
  public void testGetAllLogos_whenNotCached_thenQueriesAndCaches()
    throws Exception {
    when(logoCache.contains(testRef)).thenReturn(false);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(testRef)).thenReturn(childAssoc);
    when(
      nodeService.hasAspect(parentRef, CircabcModel.ASPECT_CIRCABC_ROOT)
    ).thenReturn(true);

    when(
      nodePreferencesService.getConfigurationFiles(
        eq(testRef),
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(Collections.emptyList());

    List<LogoDefinition> result = service.getAllLogos(testRef);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(logoCache).put(eq(testRef), eq(result));
  }

  @Test
  public void testGetDefault_whenCached_thenReturnsCachedValue()
    throws Exception {
    DefaultLogoConfiguration cached = mock(DefaultLogoConfiguration.class);
    when(configCache.contains(testRef)).thenReturn(true);
    when(configCache.get(testRef)).thenReturn(cached);

    DefaultLogoConfiguration result = service.getDefault(testRef);

    assertSame(cached, result);
    verifyNoInteractions(nodePreferencesService);
  }

  @Test
  public void testRemoveLogo_whenLogoExists_thenRemoves() throws Exception {
    NodeRef configRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "config-ref"
    );
    when(
      nodePreferencesService.getCustomization(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("toremove.png")
      )
    ).thenReturn(configRef);

    service.removeLogo(testRef, "toremove.png");

    verify(nodePreferencesService).removeCustomization(
      eq(testRef),
      anyString(),
      anyString(),
      anyString(),
      eq("toremove.png")
    );
    verify(logoCache).clear();
    verify(configCache).clear();
  }

  @Test(expected = CustomizationException.class)
  public void testRemoveLogo_whenLogoDoesNotExist_thenThrows()
    throws Exception {
    when(
      nodePreferencesService.getCustomization(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("missing.png")
      )
    ).thenReturn(null);

    service.removeLogo(testRef, "missing.png");
  }

  @Test(expected = CustomizationException.class)
  public void testRemoveConfiguration_whenNoConfig_thenThrows()
    throws Exception {
    when(
      nodePreferencesService.getCustomization(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        anyString()
      )
    ).thenThrow(new CustomizationException("not found"));

    service.removeConfiguration(testRef);
  }

  @Test
  public void testForceClearCache_thenClearsAllCaches() {
    service.forceClearCache();

    verify(logoCache).clear();
    verify(configCache).clear();
  }

  @Test
  public void testAddLogo_whenNodeNotConfigurable_thenMakesConfigurable()
    throws Exception {
    File tempFile = File.createTempFile("logo", ".png");
    tempFile.deleteOnExit();
    NodeRef customizationRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "custom-ref"
    );

    when(
      nodePreferencesService.customizationFileExists(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("new.png")
      )
    ).thenReturn(false);
    when(nodePreferencesService.isNodeConfigurable(testRef)).thenReturn(false);
    when(nodePreferencesService.makeConfigurable(testRef)).thenReturn(testRef);
    when(
      nodePreferencesService.addCustomizationFile(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("new.png"),
        eq(tempFile)
      )
    ).thenReturn(customizationRef);

    service.addLogo(testRef, "new.png", tempFile);

    verify(nodePreferencesService).makeConfigurable(testRef);
  }
}
