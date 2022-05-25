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
import java.io.InputStream;
import java.util.List;

/**
 * Business service manage the attachements. An attachement can be added to any kind of node
 * (usually a topic, but not required).
 *
 * @author Yanick Pignot
 */
public interface AttachementBusinessSrv {

    /**
     * Add a hidden attachement in a temporarary space. Used to get the attachement nodeRef reference
     * before the creation of the parent
     *
     * @param name A filename name (not null)
     * @param file An existing file on the fs
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidName(String)
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidUniqueName(NodeRef,
     * String)
     */
    NodeRef addTempAttachement(final String name, final File file);

    /**
     * Add a hidden attachement to the given node.
     *
     * @param referer An existing parent
     * @param name    A filename name (not null)
     * @param file    An existing file on the fs
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidName(String)
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidUniqueName(NodeRef,
     * String)
     */
    NodeRef addAttachement(final NodeRef referer, final String name, final File file);

    /**
     * Add a hidden attachement to the given node.
     *
     * @param referer     An existing parent
     * @param name        A filename name (not null)
     * @param inputStream An existing inputStream on the fs
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidName(String)
     * @see eu.cec.digit.circabc.business.impl.props.PropertiesBusinessImpl#computeValidUniqueName(NodeRef,
     * String)
     */
    NodeRef addAttachement(final NodeRef referer, final String name, final InputStream inputStream);

    /**
     * Add a linked attachement to the given node.
     *
     * @param referer An existing parent
     * @param refered The link to reference
     */
    NodeRef addAttachement(final NodeRef referer, final NodeRef refered);


    /**
     * Get all attachements of any ref. An empty list if no attacheùent fond.
     *
     * @param referer The node to query
     */
    List<Attachement> getAttachements(final NodeRef referer);

    /**
     * Remove a given attachement from its referer.
     *
     * @param referer An existing parent
     * @param refered The link to reference
     */
    void removeAttachement(final NodeRef referer, final NodeRef refered);

}
