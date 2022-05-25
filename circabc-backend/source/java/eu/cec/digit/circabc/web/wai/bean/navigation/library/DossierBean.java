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
package eu.cec.digit.circabc.web.wai.bean.navigation.library;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import org.alfresco.model.ContentModel;
import org.owasp.esapi.ESAPI;

import java.util.Map;

//
public class DossierBean extends LibraryBean {

    public static final String JSP_NAME = "dossier.jsp";
    public static final String BEAN_NAME = "DossierBean";
    public static final String DESCRPTION = "library_dossier_title_desc";
    public static final String TITLE = "library_dossier_title";
    /**
     *
     */
    private static final long serialVersionUID = 3115195309313119381L;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_DOSSIER;
    }

    public String getBrowserTitle() {
        String titleOrName = null;

        final String propertyTitle = ContentModel.PROP_TITLE.toString();
        final Object title = getCurrentNode().getProperties().get(propertyTitle);
        if (title != null) {
            ESAPI.encoder().encodeForHTML(titleOrName = (title.toString().length() > 0 ? title.toString()
                    : getCurrentNode().getName()));
        }

        return titleOrName;
    }

    public String getPageDescription() {
        return translate(DESCRPTION, getCurrentNode().getName());
    }

    public String getPageTitle() {
        return getCurrentNode().getName();
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "library/" + JSP_NAME;
    }

    public void init(final Map<String, String> parameters) {
        super.init(parameters);
    }
}

