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
package eu.cec.digit.circabc.repo.customisation.mail;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The mail wrapper implementation
 *
 * @author yanick pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class MailWrapperImpl implements MailWrapper {

    private static final String TEMPLATE_FREEMARKER = "freemarker";

    private static final Locale NO_LOCALE = new Locale("__NO__LOCALE");

    private final MailTemplate mailTemplate;
    private final String name;
    private final Map<Locale, NodeRef> templateTranslations;
    private final TemplateService templateService;
    private final NodeService nodeService;
    private final Locale pivotLocale;

    /*package*/ MailWrapperImpl(
            final Map<Locale, NodeRef> templateTranslations,
            final MailTemplate template,
            final TemplateService templateService,
            final NodeService nodeService,
            final Locale pivotLocale) {
        // build non multilingual template
        this.templateTranslations = templateTranslations;
        this.mailTemplate = template;
        this.templateService = templateService;
        this.pivotLocale = pivotLocale;
        this.nodeService = nodeService;
        this.name =
                (String) nodeService.getProperty(getSafeTemplate(pivotLocale), ContentModel.PROP_NAME);
    }

    /*package*/ MailWrapperImpl(
            final NodeRef templateRef,
            final MailTemplate template,
            final TemplateService templateService,
            final NodeService nodeService) {
        // build a non multilingual template
        this(
                Collections.singletonMap(NO_LOCALE, templateRef),
                template,
                templateService,
                nodeService,
                NO_LOCALE);
    }

    public String getName() {
        return this.name;
    }

    public final String getSubject(final Map<String, Object> model) {
        return getSubject(model, null);
    }

    public final String getSubject(final Map<String, Object> model, final Locale locale) {
        final Locale currentLocale = I18NUtil.getLocale();
        try {
            if (locale != null) {
                I18NUtil.setLocale(locale);
            }

            final Serializable subjectProp =
                    nodeService.getProperty(getSafeTemplate(locale), ContentModel.PROP_TITLE);

            final String subject;
            if (subjectProp != null
                    && subjectProp instanceof String
                    && subjectProp.toString().length() > 0) {
                subject = (String) subjectProp;
            } else {
                subject = this.mailTemplate.getDefaultSubject();
            }

            return templateService.processTemplateString(TEMPLATE_FREEMARKER, subject, safeModel(model));
        } finally {
            I18NUtil.setLocale(currentLocale);
        }
    }

    public final String getBody(final Map<String, Object> model) {
        return getBody(model, null);
    }

    public String getBody(final Map<String, Object> model, final Locale locale) {
        final Locale currentLocale = I18NUtil.getLocale();
        try {
            if (locale != null) {
                I18NUtil.setLocale(locale);
            }

            final String translationId = getSafeTemplate(locale).toString();
            return templateService.processTemplate(TEMPLATE_FREEMARKER, translationId, safeModel(model));
        } finally {
            I18NUtil.setLocale(currentLocale);
        }
    }

    public MailTemplate getMailTemplate() {
        return this.mailTemplate;
    }

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

    public NodeRef getTemplateNodeRef() {
        return getSafeTemplate(null);
    }

    private NodeRef getSafeTemplate(final Locale locale) {
        if (locale == null || templateTranslations.containsKey(locale) == false) {
            return this.templateTranslations.get(this.pivotLocale);
        } else {
            return this.templateTranslations.get(locale);
        }
    }

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
        result = PRIME * result + ((mailTemplate == null) ? 0 : mailTemplate.hashCode());
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((pivotLocale == null) ? 0 : pivotLocale.hashCode());
        result =
                PRIME * result + ((templateTranslations == null) ? 0 : templateTranslations.hashCode());
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
