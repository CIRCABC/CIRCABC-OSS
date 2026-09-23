package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.cache.SimpleCache;
import org.junit.Before;
import org.junit.Test;

public class LdapEcasDomainServiceTest {

  private LdapEcasDomainServiceImpl service;
  private CircabcConfig circabcConfig;
  private SimpleCache<String, Map<String, HashMap<String, String>>> cache;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    service = new LdapEcasDomainServiceImpl();
    circabcConfig = mock(CircabcConfig.class);
    cache = mock(SimpleCache.class);

    Field field = LdapEcasDomainServiceImpl.class.getDeclaredField(
      "circabcConfig"
    );
    field.setAccessible(true);
    field.set(service, circabcConfig);

    service.setLdapECASDomainsCache(cache);
  }

  @Test
  public void testInitWhenLdapDisabled() {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();
    verify(circabcConfig, never()).getContextFactory();
  }

  @Test
  public void testGetAllEcasDomains() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    data.put("ec.europa.eu", new HashMap<>());
    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Set<String> result = service.getAllEcasDomains();

    assertEquals(1, result.size());
    assertTrue(result.contains("ec.europa.eu"));
  }

  @Test
  public void testGetDefaultEcasDomains() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("description", "European Commission");
    data.put("ec.europa.eu", attrs);
    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Map<String, String> result = service.getDefaultEcasDomains();

    assertEquals("European Commission", result.get("ec.europa.eu"));
  }

  @Test
  public void testGetEcasDomainsWithLanguage() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("description;lang-fr", "Commission Européenne");
    data.put("ec.europa.eu", attrs);
    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Map<String, String> result = service.getEcasDomains("fr");

    assertEquals("Commission Européenne", result.get("ec.europa.eu"));
  }
}
