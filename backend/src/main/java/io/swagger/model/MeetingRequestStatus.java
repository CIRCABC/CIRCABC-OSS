package io.swagger.model;

/*
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

/**
 * Enumerates the possible responses a user can have to a meeting (event) invitation or request.
 *
 * <p>This status reflects whether an invitee has agreed to attend, declined, has not yet responded,
 * or whether responding is not relevant for the given context.
 */
@SuppressWarnings("java:S115")
public enum MeetingRequestStatus {
  /** The invitee has accepted the meeting request and intends to attend. */
  Accepted,
  /** The invitee has declined the meeting request. */
  Rejected,
  /** The invitee has not yet responded to the meeting request. */
  Pending,
  /** No response is applicable for this meeting request (e.g. the user is not an invitee). */
  NotApplicable,
}
