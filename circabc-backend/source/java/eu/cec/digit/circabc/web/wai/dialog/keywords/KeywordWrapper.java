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
package eu.cec.digit.circabc.web.wai.dialog.keywords;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.Locale;

/**
 * Light weight object that represent a single keyword in the User Interface
 *
 * @author Yanick pignot
 */
public class KeywordWrapper implements Serializable {

    private static final long serialVersionUID = -3491052998092218189L;

    private NodeRef id;
    private String value;
    private Locale locale;
    private Boolean selected;

    /**
     * Instanciate a non multilingual keyword
     */
    public KeywordWrapper(NodeRef id, String value) {
        this(id, value, null, false);
    }

    /**
     * Instanciate a multilingual keyword
     */
    public KeywordWrapper(NodeRef id, String value, Locale locale) {
        super();
        this.id = id;
        this.value = value;
        this.locale = locale;
        this.selected = false;
    }

    /**
     * Instanciate a multilingual keyword
     */
    public KeywordWrapper(NodeRef id, String value, Locale locale, Boolean selected) {
        super();
        this.id = id;
        this.value = value;
        this.locale = locale;
        this.selected = selected;
    }

    /**
     * @return the id
     */
    public NodeRef getId() {
        return id;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the Locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the Language
     */
    public String getLanguage() {
        return locale == null ? "" : locale.getLanguage();
    }

    @Override
    public String toString() {
        return getValue();
    }

    /**
     * @return the selected
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}