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

import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.repository.CategoryHeaderNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryHeader implements Comparable<CategoryHeader>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7071377793316258035L;
    private transient CategoryHeaderNode categoryHeader;
    private CategoryItem categoryHeaderItem;
    private List<CategoryItem> links = null;
    private List<CategoryItem> categories = null;


    public CategoryHeader() {
        super();
    }


    public CategoryHeader(final NavigableNode categoryHeader) {
        this.categoryHeader = (CategoryHeaderNode) categoryHeader;
        categoryHeaderItem = new CategoryItem(categoryHeader);
    }

    public CategoryHeaderNode getCategoryHeader() {
        return categoryHeader;
    }

    public void setCategoryHeader(CategoryHeaderNode categoryHeader) {
        this.categoryHeader = categoryHeader;
    }

    public List<CategoryItem> getLinks() {
        return links;
    }

    public void setLinks(List<CategoryItem> links) {
        this.links = links;
    }

    public List<CategoryItem> getCategories() {
        if (categories == null) {
            if (this.categoryHeader == null) {
                this.categoryHeader = new CategoryHeaderNode(categoryHeaderItem.getNodeRef());
            }
            final List<NavigableNode> navigableChilds = this.categoryHeader.getNavigableChilds();
            categories = new ArrayList<>(navigableChilds.size());
            for (NavigableNode navigableNode : navigableChilds) {
                categories.add(new CategoryItem(navigableNode));
            }
        }
        return categories;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public List<CategoryItem> getExternalLinks() {
        if (links == null) {
            if (this.categoryHeader == null) {
                this.categoryHeader = new CategoryHeaderNode(categoryHeaderItem.getNodeRef());
            }
            final List<NavigableNode> externalLinks = this.categoryHeader.getExternalLinks();
            links = new ArrayList<>(externalLinks.size());
            for (NavigableNode navigableNode : externalLinks) {
                links.add(new CategoryItem(navigableNode));
            }
        }
        return links;
    }

    public int getCategoriesSize() {
        return getCategories().size();
    }

    public int compareTo(CategoryHeader o) {
        return this.categoryHeaderItem.compareTo(o.getCategoryHeaderItem());
    }

    public CategoryItem getCategoryHeaderItem() {
        return categoryHeaderItem;
    }

    public void setCategoryHeaderItem(CategoryItem categoryHeaderItem) {
        this.categoryHeaderItem = categoryHeaderItem;
    }
}