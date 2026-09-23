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
 * Enumerates the kinds of calendar appointment that can be scheduled within an Interest Group.
 *
 * <p>An appointment classifies a time-bound entry in the Events service and is used to distinguish
 * between the different scheduling scenarios supported by the platform.
 */
@SuppressWarnings("java:S115")
public enum AppointmentType {
  /** A meeting appointment, typically involving a defined set of participants. */
  Meeting,
  /** A general event appointment, such as a public or group-wide occasion. */
  Event,
}
