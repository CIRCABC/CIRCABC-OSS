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
package eu.europa.ec.digit.circabc.rest.service.namespace;

import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * CIRCABC namespace service contract.
 *
 * <p>Extends Alfresco's {@link NamespacePrefixResolver} to provide access to, and definition of,
 * namespace URIs and their associated prefixes used within the CIRCABC content model. By building
 * on {@code NamespacePrefixResolver}, implementations can resolve a namespace prefix to its URI (and
 * vice versa) so that qualified names ({@code QName}s) can be constructed and interpreted
 * consistently across the application.
 *
 * <p>This interface also centralises CIRCABC-specific namespace constants (such as the default CEC
 * DIGIT URI) that are shared by the components relying on the CIRCABC content model.
 *
 * @author Clinckart Stephane
 * @see NamespacePrefixResolver
 */
public interface CircabcNameSpaceService extends NamespacePrefixResolver {
  /**
   * Default namespace URI for the CEC DIGIT content model.
   *
   * <p>Used as the base URI for qualified names belonging to the CIRCABC / CEC DIGIT namespace.
   */
  String CEC_DIGIT_URI = "http://eu.cec.digit";
}
