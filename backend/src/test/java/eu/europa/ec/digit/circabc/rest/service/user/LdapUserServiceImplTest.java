package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.exception.LdapAccessException;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import java.lang.reflect.Field;
import java.util.*;
import javax.naming.Context;
import org.junit.Before;
import org.junit.Test;

public class LdapUserServiceImplTest {

  private LdapUserServiceImpl service;
  private CircabcConfig circabcConfig;
  private LdapEcasDomainService ldapEcasDomainService;

  @Before
  public void setUp() throws Exception {
    service = new LdapUserServiceImpl();
    circabcConfig = mock(CircabcConfig.class);
    ldapEcasDomainService = mock(LdapEcasDomainService.class);
    setField("circabcConfig", circabcConfig);
    setField("ldapEcasDomainService", ldapEcasDomainService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LdapUserServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @SuppressWarnings("unchecked")
  private Hashtable<String, String> getEnv() throws Exception {
    Field field = LdapUserServiceImpl.class.getDeclaredField("env");
    field.setAccessible(true);
    return (Hashtable<String, String>) field.get(service);
  }

  @Test
  public void testInit_whenUseLdapTrue_thenEnvPopulated() throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(true);
    when(circabcConfig.getContextFactory()).thenReturn(
      "com.sun.jndi.ldap.LdapCtxFactory"
    );
    when(circabcConfig.getProviderURL()).thenReturn("ldap://localhost:389");
    when(circabcConfig.getSecurityAuthentication()).thenReturn("simple");
    when(circabcConfig.getSecurityPrincipal()).thenReturn("cn=admin");
    when(circabcConfig.getSecurityCredentials()).thenReturn("secret");

    service.init();

    Hashtable<String, String> env = getEnv();
    assertEquals(
      "com.sun.jndi.ldap.LdapCtxFactory",
      env.get(Context.INITIAL_CONTEXT_FACTORY)
    );
    assertEquals("ldap://localhost:389", env.get(Context.PROVIDER_URL));
    assertEquals("simple", env.get(Context.SECURITY_AUTHENTICATION));
    assertEquals("cn=admin", env.get(Context.SECURITY_PRINCIPAL));
    assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));
    assertEquals("true", env.get("com.sun.jndi.ldap.connect.pool"));
  }

  @Test
  public void testInit_whenUseLdapFalse_thenEnvEmpty() throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    service.init();

    Hashtable<String, String> env = getEnv();
    assertTrue(env.isEmpty());
  }

  @Test
  public void testGetLDAPUserDataByUid_whenLdapNotConfigured_thenReturnsNull()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();

    CircabcUserDataBean result = service.getLDAPUserDataByUid("testuser");

    assertNull(result);
  }

  @Test
  public void testGetLDAPUserDataNoFilterByUid_whenLdapNotConfigured_thenReturnsNull()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();

    CircabcUserDataBean result = service.getLDAPUserDataNoFilterByUid(
      "testuser"
    );

    assertNull(result);
  }

  @Test(expected = LdapAccessException.class)
  public void testGetLDAPUserIDByIdMonikerEmailCn_whenLdapNotConfigured_thenThrows()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    when(ldapEcasDomainService.getAllEcasDomains()).thenReturn(
      new HashSet<>(Arrays.asList("eu.europa.ec"))
    );
    service.init();

    service.getLDAPUserIDByIdMonikerEmailCn(
      "uid",
      "moniker",
      "email",
      "cn",
      false
    );
  }

  @Test(expected = LdapAccessException.class)
  public void testGetUsersByDomainFirstNameLastNameEmail_whenLdapNotConfigured_thenThrows()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();

    service.getUsersByDomainFirstNameLastNameEmail(
      "eu.europa.ec",
      "john",
      true
    );
  }

  @Test(expected = LdapAccessException.class)
  public void testGetUsersByMailDomain_whenLdapNotConfigured_thenThrows()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();

    service.getUsersByMailDomain("test@ec.europa.eu", "eu.europa.ec", true);
  }

  @Test
  public void testGetLDAPUserDataByUid_whenNullUserId_thenReturnsNull()
    throws Exception {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    service.init();

    CircabcUserDataBean result = service.getLDAPUserDataByUid(null);

    assertNull(result);
  }
}
