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
package eu.cec.digit.circabc.web.wai.dialog.search;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.search.SearchContext;

import java.util.List;

/**
 * Decorates the search context to add a raw query in case we want to extend the search terms beyond
 * the allowed by Alfresco
 *
 * @author schwerr
 */
public class CircabcSearchContext extends SearchContext {

    private static final long serialVersionUID = -637548934867379236L;

    private SearchContext searchContext = null;

    private String rawQuery = "";

    public CircabcSearchContext(SearchContext searchContext) {
        super();
        this.searchContext = searchContext;
    }

    /**
     * Gets the value of the rawQuery
     *
     * @return the rawQuery
     */
    public String getRawQuery() {
        return rawQuery;
    }

    /**
     * Adds the value of the rawQuery
     *
     * @param rawQuery the rawQuery to set.
     */
    public void addRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    /**
     * Build the search query string based on the current search context members.
     *
     * @param minimum small possible textual string used for a match this does not effect fixed values
     *                searches (e.g. boolean, int values) or date ranges
     * @return prepared search query string
     */
    @Override
    public String buildQuery(int minimum) {
        String query = searchContext.buildQuery(minimum);
        if (rawQuery.length() == 0) {
            return query;
        }
        return "(" + query + ") AND (" + rawQuery + ")";
    }

    /**
     * Gets the value of the searchContext
     *
     * @return the searchContext
     */
    public SearchContext getSearchContext() {
        return searchContext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getCategories()
     */
    @Override
    public String[] getCategories() {
        return searchContext.getCategories();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setCategories(java.lang.String[])
     */
    @Override
    public void setCategories(String[] categories) {
        searchContext.setCategories(categories);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getLocation()
     */
    @Override
    public String getLocation() {
        return searchContext.getLocation();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setLocation(java.lang.String)
     */
    @Override
    public void setLocation(String location) {
        searchContext.setLocation(location);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getMode()
     */
    @Override
    public int getMode() {
        return searchContext.getMode();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setMode(int)
     */
    @Override
    public void setMode(int mode) {
        searchContext.setMode(mode);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getText()
     */
    @Override
    public String getText() {
        return searchContext.getText();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        searchContext.setText(text);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getContentType()
     */
    @Override
    public String getContentType() {
        return searchContext.getContentType();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String contentType) {
        searchContext.setContentType(contentType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getFolderType()
     */
    @Override
    public String getFolderType() {
        return searchContext.getFolderType();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setFolderType(java.lang.String)
     */
    @Override
    public void setFolderType(String folderType) {
        searchContext.setFolderType(folderType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getMimeType()
     */
    @Override
    public String getMimeType() {
        return searchContext.getMimeType();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setMimeType(java.lang.String)
     */
    @Override
    public void setMimeType(String mimeType) {
        searchContext.setMimeType(mimeType);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#addSimpleAttributeQuery(org.alfresco.service.namespace.QName)
     */
    @Override
    public void addSimpleAttributeQuery(QName qname) {
        searchContext.addSimpleAttributeQuery(qname);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setSimpleSearchAdditionalAttributes(java.util.List)
     */
    @Override
    public void setSimpleSearchAdditionalAttributes(List<QName> attrs) {
        searchContext.setSimpleSearchAdditionalAttributes(attrs);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#addAttributeQuery(org.alfresco.service.namespace.QName, java.lang.String)
     */
    @Override
    public void addAttributeQuery(QName qname, String value) {
        searchContext.addAttributeQuery(qname, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getAttributeQuery(org.alfresco.service.namespace.QName)
     */
    @Override
    public String getAttributeQuery(QName qname) {
        return searchContext.getAttributeQuery(qname);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#addRangeQuery(org.alfresco.service.namespace.QName, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void addRangeQuery(QName qname, String lower, String upper,
                              boolean inclusive) {
        searchContext.addRangeQuery(qname, lower, upper, inclusive);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#addFixedValueQuery(org.alfresco.service.namespace.QName, java.lang.String)
     */
    @Override
    public void addFixedValueQuery(QName qname, String value) {
        searchContext.addFixedValueQuery(qname, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getFixedValueQuery(org.alfresco.service.namespace.QName)
     */
    @Override
    public String getFixedValueQuery(QName qname) {
        return searchContext.getFixedValueQuery(qname);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#getForceAndTerms()
     */
    @Override
    public boolean getForceAndTerms() {
        return searchContext.getForceAndTerms();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#setForceAndTerms(boolean)
     */
    @Override
    public void setForceAndTerms(boolean forceAndTerms) {
        searchContext.setForceAndTerms(forceAndTerms);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#toXML()
     */
    @Override
    public String toXML() {
        return searchContext.toXML();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.search.SearchContext#fromXML(java.lang.String)
     */
    @Override
    public SearchContext fromXML(String xml) {
        return searchContext.fromXML(xml);
    }
}
