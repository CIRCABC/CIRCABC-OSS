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
package eu.cec.digit.circabc.business.api.props;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Locale;
import java.util.Map;

/**
 * Business service manage properties.
 *
 * <p><i>This service must keep the focus on hiding the background implementation</i></p>
 *
 * @author Yanick Pignot
 */
public interface PropertiesBusinessSrv {

    /**
     * Compute a valid file name replacing unsuported caraters by -
     *
     * @param name The filname (not null)
     * @return A valid file name
     */
    String computeValidName(final String name);

    /**
     * Compute a valid an unique child filename for a given parent
     *
     * @param parent An existing parent
     * @param name   A filename name (not null)
     */
    String computeValidUniqueName(final NodeRef parent, final String name);

    /**
     * Compute a user friendly language description of the locale
     *
     * @param locale The locale to translate
     * @return The translation of the language of the locale
     */
    String computeLanguageTranslation(final Locale locale);

    /**
     * Return the properties of a node keyed by their names.
     *
     * @param nodeRef An existing nodeRef
     * @return The list of the node properties
     */
    Map<String, PropertyItem> getProperties(final NodeRef nodeRef);

}
