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

import eu.cec.digit.circabc.business.api.nav.NavNode;

import java.io.Serializable;

/**
 * Simple interface used to implement small classes capable of calculating dynamic property values
 * for Nodes at runtime. This allows bean responsible for building large lists of Nodes to
 * encapsulate the code needed to retrieve non-standard Node properties. The values are then
 * calculated on demand by the property resolver.
 * <p>
 * When a node is reset() the standard and other props are cleared. If property resolvers are used
 * then the non-standard props will be restored automatically as well.
 *
 * <p>
 * The code for this class has been shamelessly stolen from Alfresco, but we want to remove our
 * dependency to the alfresco-webclient.jar.
 *
 * @author Yanick Pignot
 * @see org.alfresco.web.bean.repository.NodePropertyResolver
 * </p>
 */
public interface RuntimePropertyResolver {

    /**
     * Get the property value for this resolver
     *
     * @param node Node this property is for
     * @return property value
     */
    Serializable get(NavNode node);

}
