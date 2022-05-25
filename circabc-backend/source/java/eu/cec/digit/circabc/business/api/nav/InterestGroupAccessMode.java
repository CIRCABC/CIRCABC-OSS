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
 * Enumeration of all interest group access mode (or visibility mode)
 *
 * @author Yanick Pignot
 */
public enum InterestGroupAccessMode {
    /**
     * Gathers the Interest Groups which are accessible to everybody.
     *
     * <p><b>Visible for</b></p>
     * <ul>
     * <li>Guest</li>
     * <li>Not invited but authenticated user</li>
     * <li>Invited user</li>
     * </ul>
     * <p><b>Not visible for</b></p>
     * <ul>
     * <li>Nobody</li>
     * </ul>
     **/
    PUBLIC,

    /**
     * Gathers the Interest Groups accessible for any logged-in user.
     *
     * <p><b>Visible for</b></p>
     * <ul>
     * <li>Not invited but authenticated user</li>
     * <li>Invited user</li>
     * </ul>
     * <p><b>Not visible for</b></p>
     * <ul>
     * <li>Guest</li>
     * </ul>
     **/
    REGISTRED,

    /**
     * Gathers the Interest Groups accessible for any explicitly invited user.
     *
     * <p><b>Visible for</b></p>
     * <ul>
     * <li>Invited user</li>
     * </ul>
     * <p><b>Not visible for</b></p>
     * <ul>
     * <li>Guest</li>
     * <li>Not invited but authenticated user</li>
     * </ul>
     **/
    MEMBERS
}
