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
package io.swagger.model.alfresco;

import static io.swagger.model.alfresco.BaseCircabcModel.CIRCABC_NAMESPACE;

import org.alfresco.service.namespace.QName;

/**
 * Content model constants for the CIRCABC moderation feature.
 *
 * <p>This utility class centralizes the Alfresco {@link QName} definitions of the
 * aspects and properties used to moderate content (documents, topics, posts, etc.).
 * It defines the moderation model namespace/prefix together with the aspects that
 * describe a node's moderation lifecycle &mdash; moderated, waiting for approval,
 * approved, rejected and abuse-signaled &mdash; and the properties attached to those
 * aspects (approver, moderation dates, rejection message and reported abuse messages).
 *
 * <p>The class is not meant to be instantiated; all members are {@code static}
 * constants intended to be referenced from services and web scripts that read or
 * apply moderation metadata on repository nodes.
 *
 * @author Yanick Pignot
 */
public final class ModerationModel {

  /**
   * Private constructor to prevent instantiation of this constants holder class.
   */
  private ModerationModel() {}

  /**
   * Base namespace URI of the CIRCABC moderation content model (version 1.0).
   */
  public static final String CIRCABC_MODERATION_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/moderation/1.0";

  /**
   * Circabc Model Prefix
   */
  public static final String CIRCABC_MODERATION_MODEL_PREFIX = "mo";

  /**
   * Moderated node Aspect name
   */
  public static final QName ASPECT_MODERATED = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "moderated"
  );

  /**
   * Is moderated Property name for Moderated Aspect (Boolean)
   */
  public static final QName PROP_IS_MODERATED = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "ismoderated"
  );

  /**
   * Waiting node Aspect name
   */
  public static final QName ASPECT_WAITING_APPROVAL = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "waittingApproval"
  );

  /**
   * Approved node Aspect name
   */
  public static final QName ASPECT_APPROVED = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "approved"
  );

  /**
   * Approved By Property name for Approved Aspect (String)
   */
  public static final QName PROP_APPROVED_BY = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "approvedBy"
  );

  /**
   * Approved On Property name for Approved Aspect (Date)
   */
  public static final QName PROP_APPROVED_ON = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "approvedOn"
  );

  /**
   * Rejected node Aspect name
   */
  public static final QName ASPECT_REJECTED = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "rejected"
  );

  /**
   * Rejected by Property name for Rejected Aspect (String)
   */
  public static final QName PROP_REJECT_BY = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "rejectedBy"
  );

  /**
   * Rejected On Property name for Rejected Aspect (Date)
   */
  public static final QName PROP_REJECT_ON = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "rejectedOn"
  );

  /**
   * Rejected message Property name for Rejected Aspect (String)
   */
  public static final QName PROP_REJECT_MESSAGE = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "rejectMessage"
  );

  /**
   * Abuse signaled on node Aspect name
   */
  public static final QName ASPECT_ABUSE_SIGNALED = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "abuseSignaled"
  );

  /**
   * The messages signaled by users (AbuseMessage)
   */
  public static final QName PROP_ABUSE_MESSAGES = QName.createQName(
    CIRCABC_MODERATION_MODEL_1_0_URI,
    "messages"
  );
}
