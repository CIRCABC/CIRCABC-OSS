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
package eu.cec.digit.circabc.web.ui.component.evaluator;

import eu.cec.digit.circabc.web.ui.tag.ListContainsEvaluatorTag;
import org.alfresco.web.ui.common.component.evaluator.BaseEvaluator;

import javax.faces.el.ValueBinding;
import java.util.List;

/**
 * Evaluates to true if the value is contained into the specified list.
 *
 * @author Matthieu Sprunck
 */
public class ListContainsEvaluator extends BaseEvaluator {

    /**
     * The list to look into
     */
    private List list = null;

    @Override
    public boolean evaluate() {
        Object value = getValue();
        return getList().contains(value);
    }

    /**
     * Get the list to look into
     *
     * @return a list
     */
    public List getList() {
        ValueBinding vb = getValueBinding(ListContainsEvaluatorTag.ATTR_LIST);
        if (vb != null) {
            this.list = (List) vb.getValue(getFacesContext());
        }
        return this.list;
    }

    /**
     * Set the list to look into
     *
     * @param list a list
     */
    public void setList(List list) {
        this.list = list;
    }
}
