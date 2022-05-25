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
package eu.cec.digit.circabc.service.customisation.nav;

/**
 * The base representation of a column navigation customisation.
 *
 * @author Yanick Pignot
 */
public interface ColumnConfig {

    /**
     * @return the converter
     */
    String getConverter();

    /**
     * @return the label
     */
    String getLabel();

    /**
     * @param value
     */
    void setLabel(String value);

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the resolver
     */
    String getResolver();

    /**
     * @return true if column is dynamic property otherwise false
     */
    boolean isDynamicProperty();
}
