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
 * Enumerates the supported recurrence frequencies for a repeating event.
 *
 * <p>Each constant describes how often an event occurs, ranging from simple periodic intervals
 * (daily, weekly, monthly, yearly) to specific weekday-based patterns. This enum is typically used
 * as part of an event's recurrence rule to determine when subsequent occurrences are scheduled.
 */
@SuppressWarnings("java:S115")
public enum TimesOccurence {
  /** The event recurs every day. */
  Daily,
  /** The event recurs once every week. */
  Weekly,
  /** The event recurs once every two weeks. */
  EveryTwoWeeks,
  /** The event recurs monthly on a fixed day of the month (e.g. the 15th). */
  MonthlyByDate,
  /** The event recurs monthly on a specific weekday of the month (e.g. the second Tuesday). */
  MonthlyByWeekday,
  /** The event recurs once every year. */
  Yearly,
  /** The event recurs on each weekday, Monday through Friday. */
  MondayToFriday,
  /** The event recurs on Monday, Wednesday and Friday. */
  MondayWednseyFriday,
  /** The event recurs on Tuesday and Thursday. */
  TuesdayThursday,
}
