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

public class LdapEcasDomainServiceImplTest {

  private LdapEcasDomainServiceImpl service;
  private CircabcConfig circabcConfig;
  private SimpleCache<String, Map<String, HashMap<String, String>>> cache;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    service = new LdapEcasDomainServiceImpl();
    circabcConfig = mock(CircabcConfig.class);
    cache = mock(SimpleCache.class);

    setField("circabcConfig", circabcConfig);
    service.setLdapECASDomainsCache(cache);
  }

  @Test
  public void testInit_whenUseLdapFalse_thenEnvNotPopulated() throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();
    verify(circabcConfig, never()).getContextFactory();
  }

  @Test
  public void testInit_whenUseLdapTrue_thenEnvPopulated() throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(true);
    when(circabcConfig.getContextFactory()).thenReturn(
      "com.sun.jndi.ldap.LdapCtxFactory"
    );
    when(circabcConfig.getProviderUrlEcas()).thenReturn("ldap://localhost:389");
    when(circabcConfig.getSecurityAuthentication()).thenReturn("simple");
    when(circabcConfig.getSecurityPrincipal()).thenReturn("cn=admin");
    when(circabcConfig.getSecurityCredentials()).thenReturn("secret");
    when(cache.contains("data")).thenReturn(true);

    Map<String, HashMap<String, String>> data = new HashMap<>();
    when(cache.get("data")).thenReturn(data);

    service.init();

    verify(circabcConfig).getContextFactory();
    verify(circabcConfig).getProviderUrlEcas();
    verify(circabcConfig).getSecurityAuthentication();
    verify(circabcConfig).getSecurityPrincipal();
    verify(circabcConfig).getSecurityCredentials();
  }

  @Test
  public void testGetAllEcasDomains_returnsCachedKeys() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    data.put("eu.europa.ec", new HashMap<>());
    data.put("external", new HashMap<>());

    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Set<String> result = service.getAllEcasDomains();

    assertEquals(2, result.size());
    assertTrue(result.contains("eu.europa.ec"));
    assertTrue(result.contains("external"));
  }

  @Test
  public void testGetDefaultEcasDomains_returnsDescriptions() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("description", "European Commission");
    data.put("ec.europa.eu", attrs);

    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Map<String, String> result = service.getDefaultEcasDomains();

    assertEquals(1, result.size());
    assertEquals("European Commission", result.get("ec.europa.eu"));
  }

  @Test
  public void testGetEcasDomains_returnsLocalizedDescriptions() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("description;lang-fr", "Commission Européenne");
    attrs.put("description", "European Commission");
    data.put("ec.europa.eu", attrs);

    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Map<String, String> result = service.getEcasDomains("fr");

    assertEquals(1, result.size());
    assertEquals("Commission Européenne", result.get("ec.europa.eu"));
  }

  @Test
  public void testGetEcasDomains_whenLanguageNotFound_returnsNull() {
    Map<String, HashMap<String, String>> data = new HashMap<>();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put("description", "European Commission");
    data.put("ec.europa.eu", attrs);

    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Map<String, String> result = service.getEcasDomains("de");

    assertEquals(1, result.size());
    assertNull(result.get("ec.europa.eu"));
  }

  @Test
  public void testGetAllEcasDomains_whenCacheEmpty_returnsEmptySet() {
    Map<String, HashMap<String, String>> data = new HashMap<>();

    when(cache.contains("data")).thenReturn(true);
    when(cache.get("data")).thenReturn(data);

    Set<String> result = service.getAllEcasDomains();

    assertTrue(result.isEmpty());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LdapEcasDomainServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
