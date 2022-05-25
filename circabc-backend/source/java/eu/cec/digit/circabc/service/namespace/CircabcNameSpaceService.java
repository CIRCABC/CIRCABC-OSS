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
package eu.cec.digit.circabc.service.namespace;

import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * Namespace Service.
 *
 * <p>The Namespace Service provides access to and definition of namespace URIs and Prefixes.
 *
 * @author Clinckart Stephane
 */
public interface CircabcNameSpaceService extends NamespacePrefixResolver {

    /**
     * Default CEC DIGIT URI
     */
    String CEC_DIGIT_URI = "http://eu.cec.digit";
}
