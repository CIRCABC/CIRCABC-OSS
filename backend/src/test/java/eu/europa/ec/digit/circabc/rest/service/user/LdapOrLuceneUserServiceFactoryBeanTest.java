package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import org.junit.Before;
import org.junit.Test;

public class LdapOrLuceneUserServiceFactoryBeanTest {

  private CircabcConfig circabcConfig;
  private LdapUserServiceImpl ldapUserService;
  private LuceneUserServiceImpl luceneUserService;
  private LdapOrLuceneUserServiceFactoryBean factoryBean;

  @Before
  public void setUp() {
    circabcConfig = mock(CircabcConfig.class);
    ldapUserService = mock(LdapUserServiceImpl.class);
    luceneUserService = mock(LuceneUserServiceImpl.class);
    factoryBean = new LdapOrLuceneUserServiceFactoryBean(
      circabcConfig,
      ldapUserService,
      luceneUserService
    );
  }

  @Test
  public void testLdapOrLuceneUserService_whenUseLdapTrue_thenReturnsLdapService() {
    when(circabcConfig.isUseLDAP()).thenReturn(true);

    LdapUserService result = factoryBean.ldapOrLuceneUserService();

    assertSame(ldapUserService, result);
  }

  @Test
  public void testLdapOrLuceneUserService_whenUseLdapFalse_thenReturnsLuceneService() {
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    LdapUserService result = factoryBean.ldapOrLuceneUserService();

    assertSame(luceneUserService, result);
  }
}
