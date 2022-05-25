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

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.repository.CircabcRootNode;
import org.alfresco.repo.cache.SimpleCache;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CategoryHeadersBeanData {

    private static final String CATEGORY_HEADERS_KEY = "categoryHeaders";
    private transient List<NavigableNode> headerNodes = null;
    private transient List<CategoryHeader> categoryHeaders = null;
    private SimpleCache<String, List<CategoryHeader>> categoryHeadersCache;

    /**
     * Get the list of category spaces inside link to the sub category. The sub category choose change
     * at each call to the function
     *
     * @return List<NavigableNode> List of category spaces inside the sub category
     */
    public synchronized List<CategoryHeader> getCategoryHeaders() {
        categoryHeaders = categoryHeadersCache.get(CATEGORY_HEADERS_KEY);
        if (categoryHeaders != null) {
            return categoryHeaders;
        }
        final Locale contentLocaleLang = I18NUtil.getContentLocaleLang();
        I18NUtil.setContentLocale(new Locale("en"));
        final List<NavigableNode> categoryHeadersHasNodes = getCategoryHeadersHasNode();
        if (categoryHeaders == null || categoryHeadersHasNodes.size() != categoryHeaders.size()) {
            categoryHeaders = new ArrayList<>(categoryHeadersHasNodes.size());
            for (final NavigableNode navigableNode : categoryHeadersHasNodes) {
                categoryHeaders.add(new CategoryHeader(navigableNode));
            }

        }
        categoryHeadersCache.put(CATEGORY_HEADERS_KEY, this.categoryHeaders);
        I18NUtil.setContentLocale(contentLocaleLang);
        return categoryHeaders;
    }

    /**
     * Get the list of category headers inside the category CircaBCHeader
     *
     * @return List<NavigableNode> List of category headers
     */
    private List<NavigableNode> getCategoryHeadersHasNode() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        final CircabcRootNode circabcHomeNode = navigator.getCircabcHomeNode();
        circabcHomeNode.resetCache();
        this.headerNodes = circabcHomeNode.getNavigableChilds();

        if (headerNodes == null) {
            this.headerNodes = Collections.emptyList();
        }
        return this.headerNodes;
    }

    public synchronized void reset() {
        headerNodes = null;
        categoryHeaders = null;
        categoryHeadersCache.remove(CATEGORY_HEADERS_KEY);
    }

    public SimpleCache<String, List<CategoryHeader>> getCategoryHeadersCache() {
        return categoryHeadersCache;
    }

    public void setCategoryHeadersCache(
            SimpleCache<String, List<CategoryHeader>> categoryHeadersCache) {
        this.categoryHeadersCache = categoryHeadersCache;
    }
}
