package io.swagger.config;

import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Central configuration holder for the CIRCABC REST module.
 *
 * <p>This Spring-managed bean wraps the Alfresco global properties (typically
 * loaded from {@code alfresco-global.properties}) and exposes strongly typed,
 * intention-revealing accessors for the configuration values used across the
 * application: mail/IMAP settings, machine-translation (MT) service
 * credentials, LDAP/JNDI context settings, CAS authentication URLs, new UI
 * URLs, attachment size limits and more.
 *
 * <p>Property values are resolved lazily through {@link #getProperty(String)},
 * which additionally supports {@code ${ENV_VAR:default}} placeholder
 * expansion. The class also tracks the current build "release" flavour
 * (e.g. OSS, ENT, ECHA, OLAF) to enable feature toggling at runtime.
 */
public class CircabcConfig {

  /** Model key under which the application name is exposed to templates. */
  private static final String APP_NAME = "appName";
  /** Model key under which the application URL is exposed to templates. */
  private static final String APP_URL = "appUrl";
  /** Property key for the new UI base URL. */
  public static final String NEW_UI_URL = "new.ui.url";
  /** Property key for the Hibernate SQL dialect. */
  private static final String HIBERNATE_DIALECT = "hibernate.dialect";
  /** Log message prefix used when a required property is missing. */
  private static final String PROPERTY = "Property:";
  /** Log message suffix used when a required property is missing. */
  private static final String IS_NOT_DEFINED_IN_FILE =
    " is not defined in file:";

  /** Name of the properties file referenced in missing-property log messages. */
  private static final String DEFAULT_PROPERTY_FILE =
    "alfresco-global.properties";
  /** Property key for the JNDI initial context factory. */
  private static final String CONTEXT_INITIAL_CONTEXT_FACTORY =
    "context.initial_context_factory";
  /** Property key for the JNDI/LDAP provider URL. */
  private static final String CONTEXT_PROVIDER_URL = "context.provider_url";
  /** Property key for the JNDI security authentication mode. */
  private static final String CONTEXT_SECURITY_AUTHENTICATION =
    "context.security_authentication";
  /** Property key for the JNDI security principal (bind DN). */
  private static final String CONTEXT_SECURITY_PRINCIPAL =
    "context.security_principal";
  /** Property key for the JNDI security credentials (bind password). */
  private static final String CONTEXT_SECURITY_CREDENTIALS =
    "context.security_credentials";
  /** Property key for the ECAS-specific JNDI/LDAP provider URL. */
  private static final String CONTEXT_PROVIDER_URL_ECAS =
    "context.provider_url_ecas";
  /** Property key for the public web root URL of the application. */
  private static final String WEB_ROOT_URL = "web.root.url";

  /** Property key controlling whether events use direct store access. */
  private static final String EVENTS_DIRECT_STORE_ACCESS =
    "events.direct.store.access";

  /** Commons-logging logger for this class. */
  static final Log logger = LogFactory.getLog(CircabcConfig.class);
  /** Property key for the new UI context path. */
  private static final String NEW_UI_CONTEXT = "new.ui.context";
  /** Property key for the incoming e-mail address. */
  private static final String EMAIL_ADDRESS = "email_address";
  /** Property key for the flag enabling the e-mail listener. */
  private static final String IS_EMAIL_LISTENER_ACTIVE =
    "is_email_listener_active";

  /** Current build release flavour (e.g. {@code oss}, {@code echa}, {@code olaf}). */
  private String buildRelease = "";
  /** Backing Alfresco global properties injected by Spring. */
  private Properties globalProperties;

  /**
   * Injects the Alfresco global properties that back all configuration lookups.
   *
   * @param globalProperties the resolved global properties (typically from
   *     {@code alfresco-global.properties})
   */
  public void setGlobalProperties(Properties globalProperties) {
    this.globalProperties = globalProperties;
  }

  /**
   * @return the configured Hibernate SQL dialect
   */
  public String getHibernateDialect() {
    return this.getProperty(HIBERNATE_DIALECT);
  }

  /**
   * @return the configured CIRCABC application name
   */
  public String getApplicationName() {
    return this.getProperty("build.circabc.app.name");
  }

  /**
   * @return the "from" address used for CIRCABC IT help-desk mails
   */
  public String getItHelpDeskMail() {
    return this.getProperty("mail.from.circabc.it.helpdesk");
  }

  /**
   * @return the "from" address used for CIRCABC help-desk mails
   */
  public String getHelpDeskMail() {
    return this.getProperty("mail.from.circabc.helpdesk");
  }

  /**
   * @return the host used to render document previews
   */
  public String getDocumentPreviewRenderHost() {
    return this.getProperty("document.preview.render.host");
  }

  /**
   * @return the base URL of the machine-translation (MT) service
   */
  public String getMtServiceUrl() {
    return this.getProperty("mt.service.url");
  }

  /**
   * @return the username used for authenticating REST calls to the MT service
   */
  public String getMtRESTUsername() {
    return this.getProperty("mt.rest.username");
  }

  /**
   * @return the password used for authenticating REST calls to the MT service
   */
  public String getMtRESTPassword() {
    return this.getProperty("mt.rest.password");
  }

  /**
   * @return the MT service user identifier
   */
  public String getMtUsername() {
    return this.getProperty("mt.user");
  }

  /**
   * @return the MT service user password
   */
  public String getMtPassword() {
    return this.getProperty("mt.password");
  }

  /**
   * @return the maximum allowed size, in bytes, for forum post attachments
   */
  public String getPostsAllowedAttachmentSizeinBytes() {
    return this.getProperty("posts.allowed.attachment.size.bytes");
  }

  /**
   * @return the maximum allowed size, in bytes, for uploaded logos
   */
  public String getLogoAllowedSizeinBytes() {
    return this.getProperty("logo.allowed.size.bytes");
  }

  /**
   * Looks up a required property from the global properties and resolves any
   * {@code ${ENV_VAR:default}} placeholder it may contain.
   *
   * @param key the property key to resolve
   * @return the resolved property value
   * @throws NullPointerException if the property is not defined
   */
  private String getProperty(String key) {
    String value = globalProperties.getProperty(key);
    if (value == null) {
      if (logger.isErrorEnabled()) {
        logger.error(
          PROPERTY +
            key +
            IS_NOT_DEFINED_IN_FILE +
            CircabcConfig.DEFAULT_PROPERTY_FILE
        );
      }
      throw new NullPointerException();
    } else {
      return resolvePlaceholder(value);
    }
  }

  /**
   * Resolves ${ENV_VAR:default} placeholders that Alfresco's property
   * system does not handle natively (unlike Spring Boot).
   *
   * @param value the raw property value, possibly containing a placeholder
   * @return the environment variable value when present, otherwise the default
   *     encoded in the placeholder, or the original value if it is not a
   *     placeholder
   */
  private String resolvePlaceholder(String value) {
    if (value != null && value.startsWith("${") && value.endsWith("}")) {
      String inner = value.substring(2, value.length() - 1);
      int colonIdx = inner.indexOf(':');
      if (colonIdx >= 0) {
        String envKey = inner.substring(0, colonIdx);
        String defaultVal = inner.substring(colonIdx + 1);
        String envVal = System.getenv(envKey);
        return envVal != null ? envVal : defaultVal;
      }
    }
    return value;
  }

  /**
   * @return the current build release flavour identifier
   */
  public String getBuildRelease() {
    return buildRelease;
  }

  /**
   * Sets the current build release flavour identifier.
   *
   * @param buildRelease the release flavour (e.g. {@code oss}, {@code echa},
   *     {@code olaf})
   */
  public void setBuildRelease(String buildRelease) {
    this.buildRelease = buildRelease;
  }

  /**
   * @return {@code true} if this is the open-source (OSS) build flavour
   */
  public boolean isOSS() {
    return buildRelease.equalsIgnoreCase("oss");
  }

  /**
   * @return {@code true} if this is an enterprise (non-OSS) build flavour
   */
  public boolean isENT() {
    return !isOSS();
  }

  /**
   * @return {@code true} if this is the ECHA build flavour
   */
  public boolean isECHA() {
    return buildRelease.equalsIgnoreCase("echa");
  }

  /**
   * @return {@code true} if this is the OLAF build flavour
   */
  public boolean isOLAF() {
    return buildRelease.equalsIgnoreCase("olaf");
  }

  /**
   * @return {@code true} if LDAP should be used, i.e. an enterprise flavour
   *     that is not OLAF
   */
  public boolean isUseLDAP() {
    return isENT() && !isOLAF();
  }

  /**
   * @return the JNDI initial context factory class name
   */
  public String getContextFactory() {
    return getProperty(CONTEXT_INITIAL_CONTEXT_FACTORY);
  }

  /**
   * @return the JNDI/LDAP provider URL
   */
  public String getProviderURL() {
    return getProperty(CONTEXT_PROVIDER_URL);
  }

  /**
   * @return the JNDI security authentication mode
   */
  public String getSecurityAuthentication() {
    return getProperty(CONTEXT_SECURITY_AUTHENTICATION);
  }

  /**
   * @return the JNDI security principal (bind DN)
   */
  public String getSecurityPrincipal() {
    return getProperty(CONTEXT_SECURITY_PRINCIPAL);
  }

  /**
   * @return the JNDI security credentials (bind password)
   */
  public String getSecurityCredentials() {
    return getProperty(CONTEXT_SECURITY_CREDENTIALS);
  }

  /**
   * @return the ECAS-specific JNDI/LDAP provider URL
   */
  public String getProviderUrlEcas() {
    return getProperty(CONTEXT_PROVIDER_URL_ECAS);
  }

  /**
   * @return the new UI base URL
   */
  public String getNewUiUrl() {
    return getProperty(NEW_UI_URL);
  }

  /**
   * @return the new UI context path
   */
  public String getNewUiContext() {
    return getProperty(NEW_UI_CONTEXT);
  }

  /**
   * @return the public web root URL of the application
   */
  public String getWebRootUrl() {
    return getProperty(WEB_ROOT_URL);
  }

  /**
   * @return {@code true} if events should use direct store access
   */
  public Boolean getEventsDirectStoreAccess() {
    return Boolean.parseBoolean(getProperty(EVENTS_DIRECT_STORE_ACCESS));
  }

  /**
   * @return the incoming e-mail address configured for the listener
   */
  public String getEmailAddress() {
    return getProperty(EMAIL_ADDRESS);
  }

  /**
   * @return {@code true} if the e-mail listener is active
   */
  public Boolean isListenerActive() {
    return Boolean.parseBoolean(getProperty(IS_EMAIL_LISTENER_ACTIVE));
  }

  /**
   * Adds the application name and URL to the given template model if they are
   * not already present.
   *
   * @param model the mutable template model to enrich
   */
  public void addApplicationNameToModel(Map<String, Object> model) {
    model.computeIfAbsent(APP_NAME, key -> getApplicationName());
    model.computeIfAbsent(APP_URL, key -> getWebRootUrl());
  }

  /**
   * Indicates whether outgoing mail is enabled. Defaults to {@code true} when
   * the {@code mail.enabled} property is absent.
   *
   * @return {@code true} if mail sending is enabled
   */
  public boolean isMailEnabled() {
    String value = globalProperties.getProperty("mail.enabled", "true");
    return Boolean.parseBoolean(value);
  }

  /**
   * @return the callback URL registered with the MT service
   */
  public String getMTCallbackUrl() {
    return getProperty("mt.callback.url");
  }

  /**
   * @return the application name registered with the MT service
   */
  public String getMTApplicationName() {
    return getProperty("mt.application.name");
  }

  /**
   * @return the protocol (e.g. IMAP/POP3) used by the incoming mail listener
   */
  public String getEmailProtocol() {
    return getProperty("email_protocol");
  }

  /**
   * @return the host name of the incoming mail server
   */
  public String getEmailServer() {
    return getProperty("email_server");
  }

  /**
   * @return the mailbox/folder monitored by the mail listener
   */
  public String getEmailBox() {
    return getProperty("email_box");
  }

  /**
   * @return the username used to authenticate against the mail server
   */
  public String getEmailUserName() {
    return getProperty("email_username");
  }

  /**
   * @return the password used to authenticate against the mail server
   */
  public String getEmailPassword() {
    return getProperty("email_password");
  }

  /**
   * @return the port of the incoming mail server
   */
  public Integer getEmailServerPort() {
    return Integer.valueOf(getProperty("email_server_port"));
  }

  /**
   * @return {@code true} if the e-mail listener is active
   */
  public Boolean isEmailListenerActive() {
    return Boolean.valueOf(getProperty(IS_EMAIL_LISTENER_ACTIVE));
  }

  /**
   * @return {@code true} if the mail connection should use TLS
   */
  public Boolean isEmailUseTls() {
    return Boolean.valueOf(getProperty("is_email_use_tls"));
  }

  /**
   * @return the base URL of the CAS authentication server
   */
  public String getCasBaseUrl() {
    return getProperty("cas.baseUrl");
  }

  /**
   * @return the service URL registered with CAS
   */
  public String getCasServiceUrl() {
    return getProperty("cas.serviceUrl");
  }

  /**
   * @return the frontend redirect URL used after CAS authentication
   */
  public String getCasFrontendRedirectUrl() {
    return getProperty("cas.frontendRedirectUrl");
  }

  /**
   * @return the cookie path scope used for CAS authentication cookies
   */
  public String getCasCookiePath() {
    return getProperty("cas.cookiePath");
  }
}
