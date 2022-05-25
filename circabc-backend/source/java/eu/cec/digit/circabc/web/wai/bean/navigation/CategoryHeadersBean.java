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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * Bean that backs the navigation inside a Category Header
 *
 * @author yanick pignot
 * @author Stephane Clinckart
 */
public final class CategoryHeadersBean extends BaseWaiNavigator {

    public static final String JSP_NAME = "category-list.jsp";
    public static final String BEAN_NAME = "CategoryHeadersBean";
    public static final String MSG_PAGE_TITLE = "cat_list_title";
    public static final String MSG_PAGE_DESCRIPTION = "cat_list_title_desc";
    public static final String MSG_BROWSER_TITLE = "title_cat_list";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    private static final Log logger = LogFactory.getLog(CategoryHeadersBean.class);
    private CircabcService circabcService;
    private CategoryHeadersBeanData categoryHeadersBeanData;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.CATEGORY_HEADER;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    @Override
    public String getBrowserTitle() {
        return translate(MSG_BROWSER_TITLE);
    }

    @Override
    public String getWebdavUrl() {
        // not relevant for category header
        return null;
    }

    public void init(final Map<String, String> parameters) {
        //headerNodes = null;
        //categoryHeaders = null;
    }

    private CategoryHeadersBeanData getCategoryHeadersBeanData() {
        if (categoryHeadersBeanData == null) {
            //categoryHeadersBeanData = (CategoryHeadersBeanData) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("CategoryHeadersBeanData");
            categoryHeadersBeanData = (CategoryHeadersBeanData) Beans.getBean("CategoryHeadersBeanData");
        }
        return categoryHeadersBeanData;
    }


    /**
     * Get the list of category spaces inside link to the subcategorie. The subcategory choose change
     * at each call to the function
     *
     * @return List<NavigableNode> List of category spaces inside the subcategorie
     */
    public List<CategoryHeader> getCategoryHeaders() {
        if (circabcService.readFromDatabase()) {
            return getCategoryHeadersFromDatabase();
        } else {
            return getCategoryHeadersBeanData().getCategoryHeaders();
        }

    }

    private List<CategoryHeader> getCategoryHeadersFromDatabase() {
        return circabcService.getHeadersAndCategories();
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}
