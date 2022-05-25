/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * <p>EntityService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * EntityService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * EntityService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public interface EntityService_PortType extends java.rmi.Remote {

    /**
     * This operation performs a search for internal entities in the current base (maximum 50
     * entities).<br>
     * One can search for internal entities by using either an explicit criteria (by person, by
     * organization or by ID) or a more generic search expression.<br>
     *
     * <p>The fields that can be used inside a search expression are:
     *
     * <ul>
     *   <li>isOrganisation - whether the entities are organizations or persons
     *   <li>personId - the person internal ID
     *   <li>personUserId - the person's ecas user name
     *   <li>personLastName - the person's last name
     *   <li>personFirstName - the first name(s) of the person
     *   <li>personFullName - the person's full name
     *   <li>personEmail - the person's email
     *   <li>personAlias - the alias of the person
     *   <li>personBuildingCode - the code of the building where the person is located
     *   <li>personFloorCode - the code of the floor where the person is located
     *   <li>personRoomCode - the code of the room where the person is located
     *   <li>organisationId - the organization internal ID
     *   <li>organisationName - the code of the internal organization (e.g. DIGIT.B.1)
     *   <li>organisationDg - the DG of the internal organization
     * </ul>
     *
     * <p>Sample search expressions are:
     *
     * <ul style="list-style-type:none">
     *   <li>isOrganisation=true
     *   <li>personId = 90309813
     *   <li>isOrganisation=false and personLastName startswith 'Nico'
     *   <li>isOrganisation=true and organisationDg='DIGIT'
     * </ul>
     *
     * <p>The fields that can be used for sorting with a search expression are:
     *
     * <ul>
     *   <li>personFullName - usefull when searching for internal persons
     *   <li>organisationName - usefull when searching for internal organisations
     * </ul>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[] findCurrentInternalEntity(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequest criteria,
            eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest searchByExpressionRequest)
            throws java.rmi.RemoteException;

    /**
     * This operation performs a search for external entities in the current base (maximum 50
     * entities).
     *
     * <p>One can search for internal entities by using either an explicit criteria (by person, by
     * organization or by ID) or a more generic search expression.<br>
     *
     * <p>The fields that can be used inside a search expression are:
     *
     * <ul>
     *   <li>isOrganisation - whether the entities are organizations or not
     *   <li>personId - the person external ID
     *   <li>personLastName - the last name of the person
     *   <li>personFirstName - the first name(s) of the person
     *   <li>personFullName - the full name of the person
     *   <li>personEmail - the email of the person
     *   <li>personAlias - the alias of the person
     *   <li>personCity - the city of the person
     *   <li>personCountry - the country the person
     *   <li>personValidationLevel - search for external persons that have been validated and at which
     *       level; e.g. 1 for CREATED DG, 2 for VALIDATED DG or 3 for VALIDATED EC
     *   <li>personCreatorDg - the DG of the user that created the external person
     *   <li>personCreationDate - the creation date of external person
     *   <li>organisationId - the organization internal ID
     *   <li>organisationName - the name of the organization
     *   <li>organisationAcronym - the acronym of the organization
     *   <li>organisationEmail - the email of the external organization
     *   <li>organisationCity - the city of the organization
     *   <li>organisationCountry - the country of the organization
     *   <li>organisationCreatorDg - the DG of the user that created the external organization
     *   <li>organisationValidationLevel - search for external organizations that have been validated
     *       and at which level, e.g. 1 for CREATED DG, 2 for VALIDATED DG or 3 for VALIDATED EC
     *   <li>organisationCreationDate - the creation date of external organization
     *   <li>organisationModificationDate - the date when the external organization was last modified
     * </ul>
     *
     * <p>Sample search expressions are:
     *
     * <ul style="list-style-type:none">
     *   <li>isOrganisation=true
     *   <li>personId = 90309813
     *   <li>isOrganisation=false and personFullName startswith 'Nico'
     *   <li>isOrganisation=true and organisationCreationDate BETWEEN (2009-10-23, 2009-11-14)
     *   <li>organisationCreatorDg = 'DIGIT'
     * </ul>
     *
     * <p>The fields that can be used for sorting with a search expression are:
     *
     * <ul>
     *   <li>personFullName - usefull when searching for external persons
     *   <li>organisationName - usefull when searching for external organisations
     * </ul>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[] findCurrentExternalEntity(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequest criteria,
            eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest searchByExpressionRequest,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntityRetrievalOptions retrievalOptions)
            throws java.rmi.RemoteException;

    /** This operation finds all virtual entities in the current base. */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[] findAllVirtualEntities(
            eu.cec.digit.circabc.repo.hrs.ws.Header header) throws java.rmi.RemoteException;
}
