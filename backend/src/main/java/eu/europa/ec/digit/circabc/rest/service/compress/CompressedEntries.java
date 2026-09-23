/**
 * ***************************************************************************** Copyright 2006
 * European Community
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
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.compress;

/**
 * Marker interface for the set of entries produced or consumed when compressing repository content
 * (for example when bundling one or more Alfresco nodes into a downloadable archive).
 *
 * <p>It defines a common type within the {@code compress} service package so that implementations
 * representing a collection of compressed entries can be handled polymorphically by the CIRCABC
 * compression services. It currently declares no members and acts purely as a semantic contract.
 */
public interface CompressedEntries {}
