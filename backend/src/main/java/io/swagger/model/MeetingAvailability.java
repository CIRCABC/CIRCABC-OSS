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
package io.swagger.model;

/**
 * Enumerates the visibility levels that can be assigned to a meeting.
 *
 * <p>A meeting's availability controls who is allowed to see it: {@link #Private} restricts
 * visibility to authorized members, whereas {@link #Public} exposes the meeting more broadly.
 */
@SuppressWarnings("java:S115")
public enum MeetingAvailability {
  /** The meeting is restricted and only visible to authorized members. */
  Private,
  /** The meeting is publicly visible. */
  Public,
}
