package eu.europa.ec.digit.circabc.rest.configuration;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Spring lifecycle bean that enforces the {@code admin} user's password from
 * configuration during application startup.
 *
 * <p>On bootstrap, if the {@code circabc.admin.password} property is set to a
 * non-blank value, this bean runs as the system user and overwrites the
 * password of the Alfresco {@code admin} account with the configured value.
 * This allows the administrator password to be managed externally (for
 * example, via environment variables or a secrets store) rather than relying
 * on the default value shipped with the repository.
 *
 * <p>If no password is configured, password enforcement is skipped and the
 * existing {@code admin} password is left unchanged. Failures while setting
 * the password are logged and swallowed so that they do not prevent the
 * application from starting.
 */
public class AdminPasswordBootstrap extends AbstractLifecycleBean {

  /** Logger for bootstrap and password-enforcement events. */
  private static final Logger logger = LoggerFactory.getLogger(
    AdminPasswordBootstrap.class
  );

  /**
   * Alfresco authentication service used to overwrite the {@code admin}
   * account password. Injected by Spring using the Alfresco-defined bean name
   * {@code AuthenticationService}.
   */
  @Autowired
  @Qualifier("AuthenticationService")
  @SuppressWarnings("java:S6830") // Alfresco-defined bean name
  private MutableAuthenticationService authenticationService;

  /**
   * The desired {@code admin} password, resolved from the
   * {@code circabc.admin.password} property. Defaults to an empty string when
   * the property is not set, in which case password enforcement is skipped.
   */
  @Value("${circabc.admin.password:}")
  private String adminPassword;

  /**
   * Enforces the configured {@code admin} password when the application
   * context is bootstrapped.
   *
   * <p>If {@link #adminPassword} is {@code null} or blank, the method returns
   * immediately without changing any credentials. Otherwise it runs as the
   * system user and updates the {@code admin} account password via the
   * {@link MutableAuthenticationService}. Any error encountered while setting
   * the password is logged and suppressed so that startup can proceed, and the
   * security context is always cleared afterwards.
   *
   * @param event the Spring application event that triggered the bootstrap
   */
  @Override
  protected void onBootstrap(ApplicationEvent event) {
    if (adminPassword == null || adminPassword.isBlank()) {
      logger.debug(
        "No admin password configured, skipping password enforcement"
      );
      return;
    }
    AuthenticationUtil.setRunAsUserSystem();
    try {
      authenticationService.setAuthentication(
        "admin",
        adminPassword.toCharArray()
      );
      logger.info("Admin password enforced from configuration");
    } catch (Exception e) {
      logger.error("Failed to set admin password", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  /**
   * Called when the application context is shut down. No password-related
   * cleanup is required, so this implementation is intentionally empty.
   *
   * @param event the Spring application event that triggered the shutdown
   */
  @Override
  protected void onShutdown(ApplicationEvent event) {
    // nothing to do
  }
}
