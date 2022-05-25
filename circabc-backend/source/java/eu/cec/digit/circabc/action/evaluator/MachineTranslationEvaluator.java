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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.translation.TranslationService;
import org.alfresco.web.action.evaluator.AddTranslationEvaluator;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;

public class MachineTranslationEvaluator extends AddTranslationEvaluator {


    /**
     *
     */
    private static final long serialVersionUID = 5373329645825367946L;

    @Override
    public boolean evaluate(Node node) {
        if (CircabcConfig.OSS) {
            return false;
        }

        if (super.evaluate(node)) {
            FacesContext fc = FacesContext.getCurrentInstance();
            TranslationService translationService = (TranslationService) FacesHelper
                    .getManagedBean(fc, "TranslationService");
            return translationService.canBeTranslated(node.getName());
        } else {
            return false;
        }

    }

}
