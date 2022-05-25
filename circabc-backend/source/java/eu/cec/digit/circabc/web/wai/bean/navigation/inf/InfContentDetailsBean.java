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
package eu.cec.digit.circabc.web.wai.bean.navigation.inf;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.library.ContentDetailsBean;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;

/**
 * Bean that backs the navigation inside the content details in the Library Service
 *
 * @author yanick pignot
 */
public class InfContentDetailsBean extends ContentDetailsBean {

    public static final String BEAN_NAME = "InfContentDetailsBean";
    /**
     *
     */
    private static final long serialVersionUID = -1967666575499663894L;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.INFORMATION_CONTENT;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "inf/" + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION, getCurrentNode().getName());
    }

    @Override
    public ActionsListWrapper getActionList() {
        if (!getDocument().isLocked()) {
            return new ActionsListWrapper(getDocument(), "inf_doc_details_actions_wai");
        } else {
            return null;
        }
    }
}
