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
package eu.cec.digit.circabc.web.wai.generator;

import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.keywords.SetKeywordsDialog;
import org.alfresco.web.app.Application;

import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Generates a specify keyword on a document generator.
 *
 * @author Yanick Pignot
 */
public class KeywordPickerGenerator extends BaseDialogLauncherGenerator {

    private static final String MSG_PICK_VALUE = "keywords_property_pick";
    private static final String MSG_PICK_TOOLTIP = "keywords_property_pick_tooltip";

    private static final String ACTION = SetKeywordsDialog.WAI_DIALOG_CALL;

    @Override
    protected String getTextContent(FacesContext context, String id) {
        final KeywordsService keywordService = Services.getCircabcServiceRegistry(context)
                .getKeywordsService();
        final List<Keyword> keywords = keywordService.getKeywordsForNode(getNode().getNodeRef());
        final StringBuilder buff = new StringBuilder("");

        boolean first = true;

        for (Keyword keyword : keywords) {
            if (first) {
                first = false;
            } else {
                buff.append(", ");
            }

            if (keyword.isKeywordTranslated()) {
                buff.append(keyword.getMLValues().getDefaultValue());
            } else {
                buff.append(keyword.getValue());
            }
        }

        return buff.toString();
    }


    @Override
    protected String getAction(FacesContext context, String id) {
        return ACTION;
    }


    @Override
    protected String getActionTooltip(FacesContext context, String id) {
        return Application.getMessage(context, MSG_PICK_TOOLTIP);
    }


    @Override
    protected String getActionValue(FacesContext context, String id) {
        return Application.getMessage(context, MSG_PICK_VALUE);
    }
}
