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
import eu.cec.digit.circabc.web.wai.bean.navigation.library.FileLinkDetailsBean;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;

/**
 * Bean that backs the navigation inside the file link details in the Information Service
 *
 * @author yanick pignot
 */
public class InfFileLinkDetailsBean extends FileLinkDetailsBean {

    public static final String BEAN_NAME = "InfFileLinkDetailsBean";
    public static final String JSP_NAME = "file-link-details.jsp";
    /**
     *
     */
    private static final long serialVersionUID = -2222116457591646394L;

    @Override
    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_FILE_LINK;
    }

    @Override
    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "inf/" + JSP_NAME;
    }

    public ActionsListWrapper getActionList() {
        return new ActionsListWrapper(getDocument(), "inf_filelink_details_actions_wai");
    }
}
