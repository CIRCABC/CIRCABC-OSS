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
package eu.europa.ec.digit.circabc.rest.service.keyword;

import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

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
   * Getter for the multilingual values of the keyword.
   *
   * @return the multilingual values ({@link MLText}) holding one entry per available translation
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
  void addTranlatation(Locale locale, String keyword)
    throws UnsupportedOperationException;

  /**
   * Set the translations of the keyword by erasing the previous ones.
   *
   * @param translations the multilingual values to assign as the new translations
   */
  void setTranlatations(MLText translations);

  /**
   * Make the current NON MULTILINGUAL value to the keyword.
   *
   * @param locale the locale under which the current single value is registered when converting
   *     the keyword to a multilingual one
   * @throws UnsupportedOperationException error when the keyword <b>is set</b> multilingual yet
   */
  void makeMultilingual(Locale locale) throws UnsupportedOperationException;

  /**
   * Check if Keyword contains string value in given local
   *
   * @param locale the locale in which the value is looked up
   * @param value  the value to look for in the given locale
   * @return true if the keyword holds the given value for the given locale
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
