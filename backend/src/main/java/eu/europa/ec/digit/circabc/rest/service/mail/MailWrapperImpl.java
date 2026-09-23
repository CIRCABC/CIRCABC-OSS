/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.mail;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Default {@link MailWrapper} implementation backed by the Alfresco repository.
 *
 * <p>An instance wraps one mail template that may exist in several localized variants. Each
 * localized variant is a repository node ({@link NodeRef}) keyed by its {@link Locale} in {@link
 * #templateTranslations}. The subject is taken from the node's {@code cm:title} property (falling
 * back to {@link MailTemplate#getDefaultSubject()} when absent) and the body from the node's
 * content; both are rendered through the Alfresco {@link TemplateService} using the FreeMarker
 * engine. When a locale is requested that has no dedicated translation, the {@link #pivotLocale
 * pivot} translation is used as a fallback.
 *
 * <p>During rendering the current thread locale is temporarily switched (via {@code I18NUtil}) so
 * that locale-sensitive template directives resolve correctly, and it is always restored
 * afterwards.
 *
 * @author yanick pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class MailWrapperImpl implements MailWrapper {

  /** Identifier of the template engine (FreeMarker) used by the {@link TemplateService}. */
  private static final String TEMPLATE_FREEMARKER = "freemarker";

  /** Sentinel locale used to key the single translation of a non-multilingual template. */
  private static final Locale NO_LOCALE = Locale.of("__NO__LOCALE");

  /** Template type describing the kind of notification this mail represents. */
  private final MailTemplate mailTemplate;

  /** Logical name of the template, read from the pivot node's {@code cm:name} property. */
  private final String name;

  /** Localized template variants keyed by {@link Locale}; the value is the backing node. */
  private final Map<Locale, NodeRef> templateTranslations;

  /** Alfresco service used to render the subject and body through FreeMarker. */
  private final TemplateService templateService;

  /** Alfresco service used to read template node properties (name, title). */
  private final NodeService nodeService;

  /** Locale whose translation is used as the fallback when a requested locale is missing. */
  private final Locale pivotLocale;

  /**
   * Creates a wrapper for a (possibly) multilingual template.
   *
   * @param templateTranslations the localized template nodes keyed by {@link Locale}
   * @param template the template type describing this notification
   * @param templateService the service used to render subject and body via FreeMarker
   * @param nodeService the service used to read template node properties
   * @param pivotLocale the locale whose translation is used as the fallback
   */
  /*package*/ MailWrapperImpl(
    final Map<Locale, NodeRef> templateTranslations,
    final MailTemplate template,
    final TemplateService templateService,
    final NodeService nodeService,
    final Locale pivotLocale
  ) {
    // build non multilingual template
    this.templateTranslations = templateTranslations;
    this.mailTemplate = template;
    this.templateService = templateService;
    this.pivotLocale = pivotLocale;
    this.nodeService = nodeService;
    this.name = (String) nodeService.getProperty(
      getSafeTemplate(pivotLocale),
      ContentModel.PROP_NAME
    );
  }

  /**
   * Creates a wrapper for a single, non-multilingual template.
   *
   * <p>The given node is registered under the internal {@link #NO_LOCALE} sentinel, which also
   * acts as the pivot locale.
   *
   * @param templateRef the repository node backing the template
   * @param template the template type describing this notification
   * @param templateService the service used to render subject and body via FreeMarker
   * @param nodeService the service used to read template node properties
   */
  /*package*/ MailWrapperImpl(
    final NodeRef templateRef,
    final MailTemplate template,
    final TemplateService templateService,
    final NodeService nodeService
  ) {
    // build a non multilingual template
    this(
      Collections.singletonMap(NO_LOCALE, templateRef),
      template,
      templateService,
      nodeService,
      NO_LOCALE
    );
  }

  /**
   * {@inheritDoc}
   *
   * @return the template name read from the pivot node's {@code cm:name} property
   */
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Renders the subject using the default (pivot) template.
   *
   * @param model the data model merged into the template; {@code null} is treated as empty
   * @return the rendered subject of the mail
   */
  public final String getSubject(final Map<String, Object> model) {
    return getSubject(model, null);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads the subject from the localized template node's {@code cm:title} property, falling back
   * to {@link MailTemplate#getDefaultSubject()} when that property is empty or missing, then renders
   * it through FreeMarker. The thread locale is temporarily set to {@code locale} while rendering
   * and restored afterwards.
   *
   * @param model the data model merged into the template; {@code null} is treated as empty
   * @param locale the locale used to select the translation and format values; {@code null} uses
   *     the pivot/default template
   * @return the rendered subject of the mail
   */
  public final String getSubject(
    final Map<String, Object> model,
    final Locale locale
  ) {
    final Locale currentLocale = I18NUtil.getLocale();
    try {
      if (locale != null) {
        I18NUtil.setLocale(locale);
      }

      final Serializable subjectProp = nodeService.getProperty(
        getSafeTemplate(locale),
        ContentModel.PROP_TITLE
      );

      final String subject;
      if (subjectProp instanceof String str && !str.isEmpty()) {
        subject = str;
      } else {
        subject = this.mailTemplate.getDefaultSubject();
      }

      return templateService.processTemplateString(
        TEMPLATE_FREEMARKER,
        subject,
        safeModel(model)
      );
    } finally {
      I18NUtil.setLocale(currentLocale);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Renders the body using the default (pivot) template.
   *
   * @param model the data model merged into the template; {@code null} is treated as empty
   * @return the rendered body of the mail
   */
  public final String getBody(final Map<String, Object> model) {
    return getBody(model, null);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Renders the content of the localized template node through FreeMarker, identifying the
   * template by the node reference. The thread locale is temporarily set to {@code locale} while
   * rendering and restored afterwards.
   *
   * @param model the data model merged into the template; {@code null} is treated as empty
   * @param locale the locale used to select the translation and format values; {@code null} uses
   *     the pivot/default template
   * @return the rendered body of the mail
   */
  public String getBody(final Map<String, Object> model, final Locale locale) {
    final Locale currentLocale = I18NUtil.getLocale();
    try {
      if (locale != null) {
        I18NUtil.setLocale(locale);
      }

      final String translationId = getSafeTemplate(locale).toString();
      return templateService.processTemplate(
        TEMPLATE_FREEMARKER,
        translationId,
        safeModel(model)
      );
    } finally {
      I18NUtil.setLocale(currentLocale);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the template type of this mail
   */
  public MailTemplate getMailTemplate() {
    return this.mailTemplate;
  }

  /**
   * Returns a short human-readable description of this wrapper, including its name and template
   * type.
   *
   * @return a string representation of this wrapper
   */
  @Override
  public String toString() {
    return "Template " + getName() + " (" + getMailTemplate() + ")";
  }

  private Map<String, Object> safeModel(final Map<String, Object> model) {
    if (model == null) {
      return new HashMap<>();
    } else {
      return model;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@link NodeRef} of the pivot/default template node
   */
  public NodeRef getTemplateNodeRef() {
    return getSafeTemplate(null);
  }

  /**
   * Resolves the template node for the given locale, falling back to the pivot translation.
   *
   * @param locale the requested locale; {@code null} or an unknown locale selects the pivot
   *     translation
   * @return the {@link NodeRef} of the best-matching template variant
   */
  private NodeRef getSafeTemplate(final Locale locale) {
    if (locale == null || !templateTranslations.containsKey(locale)) {
      return this.templateTranslations.get(this.pivotLocale);
    } else {
      return this.templateTranslations.get(locale);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if this wrapper's name matches the template's default template name
   */
  public boolean isOriginalTemplate() {
    return getName().equals(mailTemplate.getDefaultTemplateName());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result =
      PRIME * result + ((mailTemplate == null) ? 0 : mailTemplate.hashCode());
    result = PRIME * result + ((name == null) ? 0 : name.hashCode());
    result =
      PRIME * result + ((pivotLocale == null) ? 0 : pivotLocale.hashCode());
    result =
      PRIME * result +
      ((templateTranslations == null) ? 0 : templateTranslations.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MailWrapperImpl other = (MailWrapperImpl) obj;
    if (mailTemplate == null) {
      if (other.mailTemplate != null) {
        return false;
      }
    } else if (!mailTemplate.equals(other.mailTemplate)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (pivotLocale == null) {
      if (other.pivotLocale != null) {
        return false;
      }
    } else if (!pivotLocale.equals(other.pivotLocale)) {
      return false;
    }
    if (templateTranslations == null) {
      if (other.templateTranslations != null) {
        return false;
      }
    } else if (!templateTranslations.equals(other.templateTranslations)) {
      return false;
    }
    return true;
  }
}
