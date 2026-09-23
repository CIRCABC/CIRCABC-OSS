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
 * Enumerates the time units used to express the recurrence interval of a repeating event.
 *
 * <p>A recurring event repeats "every N occurrences" of one of these units (for example, every 2
 * weeks). This enum identifies which unit the recurrence count refers to. The lowercase constant
 * names are intentional so that the values serialize directly to the string literals expected by
 * the REST API and its consumers.
 */
@SuppressWarnings("java:S115")
public enum EveryTimesOccurence {
  /** The recurrence interval is measured in days. */
  days,
  /** The recurrence interval is measured in weeks. */
  weeks,
  /** The recurrence interval is measured in months. */
  months,
}
