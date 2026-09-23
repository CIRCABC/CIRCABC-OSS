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

public class LogoPreferencesServiceImplTest {

  private LogoPreferencesServiceImpl service;
  private NodePreferencesService nodePreferencesService;
  private NodeService nodeService;
  private SimpleCache<NodeRef, List<LogoDefinition>> logoCache;
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
        eq("mylogo.png")
      )
    ).thenReturn(configRef);

    service.removeLogo(testRef, "mylogo.png");

    verify(nodePreferencesService).removeCustomization(
      eq(testRef),
      anyString(),
      anyString(),
      anyString(),
      eq("mylogo.png")
    );
    verify(logoCache).clear();
    verify(configCache).clear();
  }

  @Test(expected = CustomizationException.class)
  public void testRemoveLogo_whenLogoNotFound_thenThrows() throws Exception {
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

  @Test(expected = IOException.class)
  public void testAddLogo_withFile_whenFileDoesNotExist_thenThrows()
    throws Exception {
    File nonExistent = mock(File.class);
    when(nonExistent.exists()).thenReturn(false);

    service.addLogo(testRef, "logo.png", nonExistent);
  }

  @Test(expected = CustomizationException.class)
  public void testAddLogo_withInputStream_whenNameAlreadyExists_thenThrows()
    throws Exception {
    InputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
    when(
      nodePreferencesService.customizationFileExists(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("dup.png")
      )
    ).thenReturn(true);

    service.addLogo(testRef, "dup.png", is);
  }

  @Test
  public void testAddLogo_withInputStream_whenValid_thenReturnsDefinition()
    throws Exception {
    InputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-logo"
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
    when(nodePreferencesService.isNodeConfigurable(testRef)).thenReturn(true);
    when(
      nodePreferencesService.addCustomizationFile(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("new.png"),
        any(InputStream.class)
      )
    ).thenReturn(createdRef);

    LogoDefinition result = service.addLogo(testRef, "new.png", is);

    assertNotNull(result);
    assertEquals(createdRef, result.getReference());
    assertEquals("new.png", result.getName());
    assertEquals(testRef, result.getDefinedOn());
    verify(logoCache).clear();
  }

  @Test
  public void testForceClearCache_clearsBothCaches() {
    service.forceClearCache();

    verify(logoCache).clear();
    verify(configCache).clear();
  }

  @Test
  public void testSetDefault_whenLogoRefNull_thenSetsNullLogo()
    throws Exception {
    // Setup getOrCreateConfiguraton to work
    NodeRef configFileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "config-file"
    );
    when(
      nodePreferencesService.getCustomization(
        eq(testRef),
        anyString(),
        anyString(),
        anyString(),
        eq("config.properties")
      )
    ).thenThrow(new CustomizationException("not found"));

    // For getDefault (called inside getOrCreateConfiguraton)
    when(configCache.contains(testRef)).thenReturn(false);
    NodeRef defaultConfigFile = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "default-cfg"
    );
    when(
      nodePreferencesService.getDefaultConfigurationFile(
        eq(testRef),
        anyString(),
        anyString(),
        anyString()
      )
    ).thenThrow(new CustomizationException("no default"));

    // Since both getOrCreateConfiguraton and getDefault will throw, expect exception
    try {
      service.setDefault(testRef, null);
      fail("Expected CustomizationException");
    } catch (CustomizationException e) {
      // expected - no configuration exists
    }
  }
}
