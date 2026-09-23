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
 * Identifies which section of an appointment's information should be updated.
 *
 * <p>This enumeration is used to scope update operations on an appointment, allowing callers to
 * target a specific portion of the appointment data rather than replacing the whole record. It acts
 * as a discriminator in the REST/service layer when partial updates are requested.
 */
@SuppressWarnings("java:S115")
public enum AppointmentUpdateInfo {
  /** The appointment's general information section (e.g. title, description, dates). */
  GeneralInformation,
  /** The space (repository location) that the appointment is associated with. */
  RelevantSpace,
  /** The contact details attached to the appointment. */
  ContactInformation,
  /** The audience of the appointment, i.e. the users or groups it targets. */
  Audience,
  /** All sections of the appointment; updates the complete appointment record. */
  All,
}
