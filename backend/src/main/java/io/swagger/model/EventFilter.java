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
 * Enumerates the temporal filtering options that can be applied when querying calendar events.
 *
 * <p>This filter is used to restrict a set of events based on their timing relative to a reference
 * point (typically the current date/time), allowing callers to select only upcoming events, only
 * past events, or events occurring at an exact point in time.
 */
@SuppressWarnings("java:S115")
public enum EventFilter {
  /** Selects events scheduled to occur in the future. */
  Future,
  /** Selects events that occurred in the past. */
  Previous,
  /** Selects events occurring at an exact, specified point in time. */
  Exact,
}
