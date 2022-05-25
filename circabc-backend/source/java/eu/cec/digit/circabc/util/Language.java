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
package eu.cec.digit.circabc.util;

import org.alfresco.util.ParameterCheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Containing information about a language (code, label, ...)
 *
 * @author Matthieu Sprunck
 */
public class Language implements Serializable, Comparable<Language> {

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 1505177320531573328L;

    /**
     * The language's code
     */
    private String code;

    /**
     * The language's label
     */
    private String label;

    /**
     * True if the translation is available for this language
     */
    private boolean available;

    /**
     * The order of this language in the list
     */
    private int order;

    /**
     * Default constructor
     */
    public Language() {
    }

    /**
     * Constuctor
     *
     * @param code      The language's code
     * @param label     The language's label
     * @param available True if the translation is available for this language
     * @param order     The order of this language in the list
     */
    public Language(String code, String label, boolean available, int order) {
        // to lower case
        setCode(code);
        // first letter to upper case
        setLabel(label);
        this.available = available;
        this.order = order;
    }

    public static List<String> codesList(List<Language> langs) {
        return codesList(langs, false);
    }

    public static List<String> codesList(List<Language> langs,
                                         boolean onlyAvailable) {
        ParameterCheck.mandatory("langs", langs);

        List<String> result = new ArrayList<>();
        for (Language language : langs) {
            if (language.isAvailable() || !onlyAvailable) {
                result.add(language.getCode());
            }
        }
        return result;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code.toLowerCase();
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        char[] labelTab = label.toCharArray();
        labelTab[0] = Character.toUpperCase(labelTab[0]);
        this.label = new String(labelTab);
    }

    /**
     * @return Returns the order.
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order The order to set.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @return Returns the available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * @param available The available to set.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public int hashCode() {
        return this.getOrder();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Language)) {
            return false;
        }
        Language l = (Language) o;
        return (order == l.getOrder());
    }

    public int compareTo(Language o) {
        return (order < o.getOrder() ? -1 : (order == o.getOrder() ? 0 : 1));
    }
}
