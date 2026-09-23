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
package eu.europa.ec.digit.circabc.rest.service.bulk.validation;

/**
 * Classifies the severity of a validation error raised during a bulk operation.
 *
 * <p>Used by the bulk validation layer to distinguish between problems that must halt processing
 * and those that merely warrant attention while allowing processing to continue.
 */
@SuppressWarnings("java:S115")
public enum ErrorType {
  /** A blocking error: validation has failed and the associated operation cannot proceed. */
  Fatal,
  /** A non-blocking issue: the operation may still proceed, but the condition should be flagged. */
  Warning,
}
