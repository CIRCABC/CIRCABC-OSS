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
package eu.cec.digit.circabc.business.api.content;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;

/**
 * Business service to upload a document.
 *
 * @author Yanick Pignot
 */
public interface ContentBusinessSrv {


    /**
     * Add a content node a given parent. The name will be unique and unique.
     *
     * @param parent              An existing parent
     * @param name                A filename name (not null)
     * @param file                An existing file on the fs
     * @param disableNotification if true, the notification mechanism will be skipped
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessSrv#computeValidName(String)
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessSrv#computeValidUniqueName(NodeRef,
     * String)
     */
    NodeRef addContent(final NodeRef parent, final String name, final File file,
                       final boolean disableNotification);

    /**
     * Add a content node a given parent. The name will be unique and unique.
     *
     * @param parent An existing parent
     * @param name   A filename name (not null)
     * @param file   An existing file on the fs
     */
    NodeRef addContent(final NodeRef parent, final String name, final File file);

}
