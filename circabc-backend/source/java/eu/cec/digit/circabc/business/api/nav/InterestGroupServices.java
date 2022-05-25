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
package eu.cec.digit.circabc.business.api.nav;

/**
 * Enumeration of all interest group services
 *
 * @author Yanick Pignot
 */
public enum InterestGroupServices {
    /**
     * This kind of WEB space helps users present information about their interest group in HTML
     * format.
     **/
    INFORMATION,

    /**
     * In the Library service, users manage and share their documents.
     **/
    LIBRARY,

    /**
     * Directory service gives users the opportunity of view and interact with other circvabc users.
     **/
    DIRECTORY,

    /**
     * The Newsgroups service allows the members of the Interest Group to create forums and hold
     * discussions with one another.
     **/
    NEWSGROUP,

    /**
     * Event service gives users the opportunity of scheduling, publicising and managing events.
     **/
    EVENTS,

    /**
     * With <b>optional</b> survey service, users can build and follow up surveys through the
     * integration of CIRCABC with IPM (Interactive Policy-Making).
     **/
    SURVEY
}
