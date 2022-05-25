/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.keyword;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Locale;

/**
 * This class represents a Keyword. A collection of keywords can be defined on a node. A keyword can
 * be either multilingual or not.
 *
 * @author Matthieu Sprunck
 * @author Yanick Pignot
 */
public interface Keyword {

    /**
     * Getter for value file
     *
     * @return the value
     */
    String getValue();

    /**
     * @return the mlValues
     */
    MLText getMLValues();

    /**
     * @return the node reference where the keyword is stored
     */
    NodeRef getId();

    /**
     * @return true if the keyword is setted as being multilingual
     */
    boolean isKeywordTranslated();

    /**
     * Used to get the toString method within a JSF compoment.
     *
     * @return the toString method
     */
    String getString();

    /**
     * Add a translation to the keyword
     *
     * @param locale  the locale of the new translation
     * @param keyword the translated value
     * @throws UnsupportedOperationException error when the keyword <b>is not set</b> multilingual yet
     */
    void addTranlatation(Locale locale, String keyword) throws UnsupportedOperationException;

    /**
     * Set the translations of to the keyword by erasing the previous ones
     *
     * @param translations the locale of the new translation
     */
    void setTranlatations(MLText translations);

    /**
     * Make the current NON MULTILINGUAL value to the keyword.
     *
     * @throws UnsupportedOperationException error when the keyword <b>is set</b> multilingual yet
     */
    void makeMultilingual(Locale locale) throws UnsupportedOperationException;

    /**
     * Check if Keyword contains string value in given local
     */
    boolean exists(Locale locale, String value);

    /**
     * @return the selected
     */
    Boolean getSelected();

    /**
     * @param selected the selected to set
     */
    void setSelected(Boolean selected);
}
