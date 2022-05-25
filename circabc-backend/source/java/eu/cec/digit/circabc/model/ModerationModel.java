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
 * Model representing
 *
 * @author Yanick Pignot
 */
public interface ModerationModel extends BaseCircabcModel {

    /**
     * Circabc Dossier namespace
     */
    String CIRCABC_MODERATION_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/moderation/1.0";

    /**
     * Circabc Model Prefix
     */
    String CIRCABC_MODERATION_MODEL_PREFIX = "mo";

    /**
     * Moderated node Aspect name
     */
    QName ASPECT_MODERATED = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "moderated");

    /**
     * Is moderated Property name for Moderated Aspect (Boolean)
     */
    QName PROP_IS_MODERATED = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "ismoderated");

    /**
     * Waiting node Aspect name
     */
    QName ASPECT_WAITING_APPROVAL = QName
            .createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "waittingApproval");

    /**
     * Approved node Aspect name
     */
    QName ASPECT_APPROVED = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "approved");

    /**
     * Approved By Property name for Approved Aspect (String)
     */
    QName PROP_APPROVED_BY = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "approvedBy");

    /**
     * Approved On Property name for Approved Aspect (Date)
     */
    QName PROP_APPROVED_ON = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "approvedOn");

    /**
     * Rejected node Aspect name
     */
    QName ASPECT_REJECTED = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "rejected");

    /**
     * Rejected by Property name for Rejected Aspect (String)
     */
    QName PROP_REJECT_BY = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "rejectedBy");

    /**
     * Rejected On Property name for Rejected Aspect (Date)
     */
    QName PROP_REJECT_ON = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "rejectedOn");

    /**
     * Rejected message Property name for Rejected Aspect (String)
     */
    QName PROP_REJECT_MESSAGE = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "rejectMessage");

    /**
     * Abuse signaled on node Aspect name
     */
    QName ASPECT_ABUSE_SIGNALED = QName
            .createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "abuseSignaled");

    /**
     * The messages signaled by users (AbuseMessage)
     */
    QName PROP_ABUSE_MESSAGES = QName.createQName(CIRCABC_MODERATION_MODEL_1_0_URI, "messages");
}
