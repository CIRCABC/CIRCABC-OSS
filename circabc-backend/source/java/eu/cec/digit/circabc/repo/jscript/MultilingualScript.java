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
package eu.cec.digit.circabc.repo.jscript;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Locale;

/**
 * @author Slobodan Filipovic
 * <p>Expose multilingualContentService to be accessible via javascript
 */
public final class MultilingualScript extends BaseProcessorExtension {

    private static final Log logger = LogFactory.getLog(MultilingualScript.class);

    private MultilingualContentService multilingualContentService;
    private ServiceRegistry serviceRegistry;

    public ScriptNode addEmptyTranslation(ScriptNode translationOf, String name, String language) {
        ScriptNode result = null;
        try {
            final NodeRef resultNodeRef =
                    multilingualContentService.addEmptyTranslation(
                            translationOf.getNodeRef(), name, new Locale(language));
            result = new ScriptNode(resultNodeRef, serviceRegistry);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error when call addEmptyTranslation with arguments: "
                                + translationOf.toString()
                                + " , "
                                + language,
                        e);
            }
        }
        return result;
    }

    public void unmakeTranslation(ScriptNode content) {

        try {
            NodeRef contentNodeRef = content.getNodeRef();

            multilingualContentService.unmakeTranslation(contentNodeRef);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when call unmakeTranslation with arguments: " + content.toString(), e);
            }
        }
    }

    public Boolean isTranslation(ScriptNode content) {
        Boolean result = null;
        try {
            NodeRef contentNodeRef = content.getNodeRef();
            result = multilingualContentService.isTranslation(contentNodeRef);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when call isTranslation with arguments: " + content.toString(), e);
            }
        }
        return result;
    }

    public ScriptNode getPivotTranslation(ScriptNode content) {

        ScriptNode result = null;
        try {
            final NodeRef resultNodeRef =
                    multilingualContentService.getPivotTranslation(content.getNodeRef());
            result = new ScriptNode(resultNodeRef, serviceRegistry);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error when call getPivotTranslation with arguments: " + content.toString(), e);
            }
        }
        return result;
    }

    public void makeTranslation(ScriptNode content, String language) {

        try {
            NodeRef contentNodeRef = content.getNodeRef();
            Locale locale = new Locale(language);
            multilingualContentService.makeTranslation(contentNodeRef, locale);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error when call make translation with arguments: "
                                + content.toString()
                                + " , "
                                + language,
                        e);
            }
        }
    }

    public void addTranslation(ScriptNode newTranslation, ScriptNode content, String language) {
        try {
            NodeRef newTranslationNodeRef = newTranslation.getNodeRef();
            NodeRef contentNodeRef = content.getNodeRef();
            Locale locale = new Locale(language);
            multilingualContentService.addTranslation(newTranslationNodeRef, contentNodeRef, locale);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error when call add translation with arguments: "
                                + newTranslation.toString()
                                + " , "
                                + content.toString()
                                + " , "
                                + language,
                        e);
            }
        }
    }

    public MultilingualContentService getMultilingualContentService() {
        return multilingualContentService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
