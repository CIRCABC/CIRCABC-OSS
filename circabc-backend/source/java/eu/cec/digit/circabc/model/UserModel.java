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
 * It is the model of the new Aspect that defines the Circabc Users
 *
 * @author atadian
 * @author Guillaume
 */
public interface UserModel extends BaseCircabcModel {

    /**
     * Circabc user content Model Prefix
     */
    String CIRCABC_USER_MODEL_PREFIX = "cu";

    /**
     * Prefix used to define usernames. This prefix is for alfresco domian
     */
    String ALFRESCO_USER_PREFIX = "";

    /**
     * Circabc user model namespace
     */
    String CIRCABC_USER_MODEL_1_0_URI = CIRCABC_NAMESPACE
            + "/model/user/1.0";

    /**
     * QName for extra properties [title] that belong to Circabc Users
     */
    QName PROP_TITLE = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "title");

    /**
     * QName for extra properties [phone] that belong to Circabc Users
     */
    QName PROP_PHONE = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "phone");

    /**
     * QName for extra properties [fax] that belong to Circabc Users
     */
    QName PROP_FAX = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "fax");

    /**
     * QName for extra properties [url] that belong to Circabc Users
     */
    QName PROP_URL = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "url");

    /**
     * QName for extra properties [zip] that belong to Circabc Users
     */
    QName PROP_POSTAL_ADDRESS = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "postaladress");

    /**
     * QName for extra properties [description] that belong to Circabc Users
     */
    QName PROP_DESCRIPTION = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "description");

    /**
     * QName for extra properties [domain] that belong to Circabc Users
     */
    QName PROP_DOMAIN = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "domain");

    /**
     * QName for extra properties [orgdepnumber] that belong to Circabc Users
     */
    QName PROP_ORGDEPNUMBER = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "orgdepnumber");

    /**
     * QName for extra properties [visibility] that belong to Circabc Users
     */
    QName PROP_VISISBILITY = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "visibility");

    /**
     * QName for extra properties [globalNotification] that belong to Circabc Users
     */
    QName PROP_GLOBAL_NOTIFICATION = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "globalNotification");

    /**
     * QName for extra properties [lastLoginTime] that belong to Circabc Users
     */
    QName PROP_LAST_LOGIN_TIME = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "lastLoginTime");

    /**
     * QName for extra properties [lastModificationDetailsTime] that belong to Circabc Users
     */
    QName PROP_LAST_MODIFICATION_DETAILS_TIME = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "lastModificationDetailsTime");

    /**
     * QName for extra properties [creationDate] that belong to Circabc Users
     */
    QName PROP_CREATION_DATE = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "creationDate");


    /**
     * QName for Circabc Aspect
     */
    QName TYPE_CIRCA_ASPECT = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "circauseraspect");

    /**
     * QName for extra properties ecas user that belong to Circabc Users
     */
    QName PROP_ECAS_USER_NAME = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "ecasUserName");

    /**
     * QName for extra properties new ui preferences
     */
    QName PROP_PREFERENCE = QName.createQName(
            CIRCABC_USER_MODEL_1_0_URI, "preference");

}
