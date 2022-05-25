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
/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package org.alfresco.web.bean.search;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Override search context to make persist forceAnd.
 *
 * @author Yanick Pignot
 */
public class SearchContextDelegate extends SearchContext {

    private static final String ELEMENT_FORCE_AND = "forceAnd";
    private static final long serialVersionUID = 474126961538850277L;
    final SearchContext searchContext;

    public SearchContextDelegate(final SearchContext searchContext) {
        ParameterCheck.mandatory("searchContext", searchContext);
        this.searchContext = searchContext;
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#addAttributeQuery(org.alfresco.service.namespace.QName,
     * java.lang.String)
     */
    public void addAttributeQuery(QName qname, String value) {
        searchContext.addAttributeQuery(qname, value);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#addFixedValueQuery(org.alfresco.service.namespace.QName,
     * java.lang.String)
     */
    public void addFixedValueQuery(QName qname, String value) {
        searchContext.addFixedValueQuery(qname, value);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#addRangeQuery(org.alfresco.service.namespace.QName,
     * java.lang.String, java.lang.String, boolean)
     */
    public void addRangeQuery(QName qname, String lower, String upper, boolean inclusive) {
        searchContext.addRangeQuery(qname, lower, upper, inclusive);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#addSimpleAttributeQuery(org.alfresco.service.namespace.QName)
     */
    public void addSimpleAttributeQuery(QName qname) {
        searchContext.addSimpleAttributeQuery(qname);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#buildQuery(int)
     */
    public String buildQuery(int minimum) {
        return searchContext.buildQuery(minimum);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return searchContext.equals(obj);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getAttributeQuery(org.alfresco.service.namespace.QName)
     */
    public String getAttributeQuery(QName qname) {
        return searchContext.getAttributeQuery(qname);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getCategories()
     */
    public String[] getCategories() {
        return searchContext.getCategories();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setCategories(java.lang.String[])
     */
    public void setCategories(String[] categories) {
        searchContext.setCategories(categories);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getContentType()
     */
    public String getContentType() {
        return searchContext.getContentType();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        searchContext.setContentType(contentType);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getFixedValueQuery(org.alfresco.service.namespace.QName)
     */
    public String getFixedValueQuery(QName qname) {
        return searchContext.getFixedValueQuery(qname);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getFolderType()
     */
    public String getFolderType() {
        return searchContext.getFolderType();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setFolderType(java.lang.String)
     */
    public void setFolderType(String folderType) {
        searchContext.setFolderType(folderType);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getForceAndTerms()
     */
    public boolean getForceAndTerms() {
        return searchContext.getForceAndTerms();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setForceAndTerms(boolean)
     */
    public void setForceAndTerms(boolean forceAndTerms) {
        searchContext.setForceAndTerms(forceAndTerms);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getLocation()
     */
    public String getLocation() {
        return searchContext.getLocation();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setLocation(java.lang.String)
     */
    public void setLocation(String location) {
        searchContext.setLocation(location);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getMimeType()
     */
    public String getMimeType() {
        return searchContext.getMimeType();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType) {
        searchContext.setMimeType(mimeType);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getMode()
     */
    public int getMode() {
        return searchContext.getMode();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setMode(int)
     */
    public void setMode(int mode) {
        searchContext.setMode(mode);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getRangeProperty(org.alfresco.service.namespace.QName)
     */
    public RangeProperties getRangeProperty(QName qname) {
        return searchContext.getRangeProperty(qname);
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#getText()
     */
    public String getText() {
        return searchContext.getText();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setText(java.lang.String)
     */
    public void setText(String text) {
        searchContext.setText(text);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return searchContext.hashCode();
    }

    /**
     * @see org.alfresco.web.bean.search.SearchContext#setSimpleSearchAdditionalAttributes(java.util.List)
     */
    public void setSimpleSearchAdditionalAttributes(List<QName> attrs) {
        searchContext.setSimpleSearchAdditionalAttributes(attrs);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return searchContext.toString();
    }


    @Override
    public org.alfresco.web.bean.search.SearchContext fromXML(String xml) {
        try {
            searchContext.fromXML(xml);

            final Element rootElement = extractDocument(xml).getRootElement();
            final Element forceAnd = rootElement.element(ELEMENT_FORCE_AND);
            if (forceAnd != null) {
                searchContext.setForceAndTerms(Boolean.valueOf(forceAnd.getText()));
            }

            return this;
        } catch (Throwable err) {
            throw new AlfrescoRuntimeException("Failed to import SearchContext from XML.", err);
        }
    }

    @Override
    public String toXML() {
        try {
            final String xmlStr = searchContext.toXML();
            final Document document = extractDocument(xmlStr);
            final Element rootElement = document.getRootElement();
            rootElement.addElement(ELEMENT_FORCE_AND).addText(Boolean.toString(getForceAndTerms()));

            StringWriter out = new StringWriter(1024);
            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
            writer.setWriter(out);
            writer.write(document);

            return out.toString();
        } catch (Throwable err) {
            throw new AlfrescoRuntimeException("Failed to import SearchContext from XML.", err);
        }
    }

    private Document extractDocument(String xml) throws DocumentException {
        final SAXReader reader = new SAXReader();
        reader.setIgnoreComments(true);
        reader.setIncludeExternalDTDDeclarations(false);
        reader.setIncludeInternalDTDDeclarations(false);
        reader.setValidation(false);
        final Document document = reader.read(new StringReader(xml));
        return document;
    }

    /**
     * Gets the value of the searchContext
     *
     * @return the searchContext
     */
    public SearchContext getSearchContext() {
        return searchContext;
    }
}
