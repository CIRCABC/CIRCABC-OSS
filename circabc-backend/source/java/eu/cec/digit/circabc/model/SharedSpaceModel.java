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
package eu.cec.digit.circabc.model;

import org.alfresco.service.namespace.QName;

/**
 * It is the model for the share space specification
 *
 * @author Stephane Clinckart
 * @author Slobodan Filipovic
 */
public interface SharedSpaceModel extends BaseCircabcModel {

    /**
     * Circabc Shared Space namespace
     */
    String CIRCABC_SHARED_SPACE_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/sharespace/1.0";


    /**
     * Circabc Shared Space prefix
     */
    String CIRCABC_SHARED_SPACE_MODEL_PREFIX = "ss";

    QName ASSOC_SHARE_SPACE_CONTAINER = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "shareSpaceContainer");


    QName TYPE_CONTAINER = QName.createQName(
            CIRCABC_SHARED_SPACE_MODEL_1_0_URI, "Container");

    /**
     * Circabc
     */
    QName ASSOC_ITEREST_GROUP = QName.createQName(
            CIRCABC_SHARED_SPACE_MODEL_1_0_URI, "InterestGroupAss");

    QName TYPE_INVITED_INTEREST_GROUP = QName.createQName(
            CIRCABC_SHARED_SPACE_MODEL_1_0_URI, "invitedInterestGroup");
    QName PROP_INTEREST_GROUP_NODE_REF = QName.createQName(
            CIRCABC_SHARED_SPACE_MODEL_1_0_URI, "ignoderef");

    QName PROP_PERMISSION = QName.createQName(
            CIRCABC_SHARED_SPACE_MODEL_1_0_URI, "permission");


}
