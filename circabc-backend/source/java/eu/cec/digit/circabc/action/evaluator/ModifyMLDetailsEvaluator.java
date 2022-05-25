/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.Locale;
import java.util.Map;


/**
 * Evaluates whether the Modify mlContainer details is visible.
 *
 * @author Yanick Pignot
 */


public class ModifyMLDetailsEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 2839741048206551370L;

    public boolean evaluate(final Node node) {

        final MultilingualContentService mlService = Services
                .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                .getMultilingualContentService();

        boolean allow = true;

        final Map<Locale, NodeRef> translations = mlService.getTranslations(node.getNodeRef());
        Node translation;
        for (final Map.Entry<Locale, NodeRef> entry : translations.entrySet()) {
            translation = new Node(entry.getValue());

            if (translation.hasPermission(PermissionService.WRITE_PROPERTIES) == false) {
                allow = false;
                break;
            }
        }

        // must be not already versionable.
        return allow;

    }
}
