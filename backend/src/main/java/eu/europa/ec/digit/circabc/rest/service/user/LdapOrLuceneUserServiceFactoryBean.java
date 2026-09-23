package eu.europa.ec.digit.circabc.rest.service.user;

import io.swagger.config.CircabcConfig;

/**
 * Spring factory bean that selects the appropriate {@link LdapUserService} implementation to use at
 * runtime based on the application configuration.
 *
 * <p>Depending on the {@code useLDAP} flag exposed by {@link CircabcConfig}, this factory returns
 * either the LDAP-backed implementation ({@link LdapUserServiceImpl}) or the Lucene/repository-backed
 * implementation ({@link LuceneUserServiceImpl}). This lets deployments switch their user directory
 * strategy through configuration without changing the beans that depend on {@link LdapUserService}.
 */
public class LdapOrLuceneUserServiceFactoryBean {

  /** Application configuration used to decide whether LDAP-based user lookup is enabled. */
  private final CircabcConfig circabcConfig;

  /** LDAP-backed user service implementation, returned when LDAP is enabled. */
  private final LdapUserServiceImpl ldapUserService;

  /** Lucene/repository-backed user service implementation, returned when LDAP is disabled. */
  private final LuceneUserServiceImpl luceneUserService;

  /**
   * Creates the factory bean with the configuration and both candidate service implementations.
   *
   * @param circabcConfig the application configuration that determines whether LDAP is used
   * @param ldapUserService the LDAP-backed user service implementation
   * @param luceneUserService the Lucene/repository-backed user service implementation
   */
  public LdapOrLuceneUserServiceFactoryBean(
    CircabcConfig circabcConfig,
    LdapUserServiceImpl ldapUserService,
    LuceneUserServiceImpl luceneUserService
  ) {
    this.circabcConfig = circabcConfig;
    this.ldapUserService = ldapUserService;
    this.luceneUserService = luceneUserService;
  }

  /**
   * Resolves the {@link LdapUserService} implementation to use according to the current
   * configuration.
   *
   * @return the {@link LdapUserServiceImpl} if LDAP is enabled in {@link CircabcConfig}, otherwise
   *     the {@link LuceneUserServiceImpl}
   */
  public LdapUserService ldapOrLuceneUserService() {
    return circabcConfig.isUseLDAP() ? ldapUserService : luceneUserService;
  }
}
