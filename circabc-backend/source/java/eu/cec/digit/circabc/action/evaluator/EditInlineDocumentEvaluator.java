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

import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

/**
 * Test if the document is inline editable
 *
 * @author Yanick Pignot
 */
public class EditInlineDocumentEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 2888369999290163419L;

    public boolean evaluate(final Node node) {
        if (node.hasAspect(WCMAppModel.ASPECT_FORM_INSTANCE_DATA)
                || node.isLocked()
                || node.hasAspect(DocumentModel.ASPECT_URLABLE)
                || node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)
                || (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) && !node.isWorkingCopyOwner())) {
            return false;
        } else {
            return node.hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE) &&
                    node.getProperties().get(ApplicationModel.PROP_EDITINLINE.toString()) != null &&
                    (Boolean) node.getProperties().get(ApplicationModel.PROP_EDITINLINE.toString()) == true;
        }

    }

}
