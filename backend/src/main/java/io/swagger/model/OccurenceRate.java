package io.swagger.model;

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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Domain model describing the recurrence (occurrence) rate of a recurring event.
 *
 * <p>An occurrence rate combines a main occurrence mode ({@link MainOccurence}, e.g. only once, a
 * fixed number of times, or every N time units) with the supporting parameters required by that
 * mode: an optional {@link TimesOccurence} or {@link EveryTimesOccurence} unit, a {@code times}
 * count and an {@code every} interval.
 *
 * <p>Instances can be serialized to and reconstructed from a compact pipe-separated string via
 * {@link #toString()} and the {@link #OccurenceRate(String)} constructor. The serialized form is
 * {@code mainOccurence|timesOccurence|everyTimesOccurence|times|every}, where absent values are
 * represented by the literal {@code "null"}.
 */
public class OccurenceRate {

  /** Sentinel value indicating that a numeric parameter ({@code times} or {@code every}) is unset. */
  public static final int INVALID_VALUE = -1;

  /** Token used to represent an absent value in the serialized string form. */
  private static final String NULL = "null";

  /** Field separator used in the serialized string form. */
  private static final String SEPARATOR = "|";

  /** The main occurrence mode (only once, a number of times, or every N units). */
  private MainOccurence mainOccurence;

  /** The time unit used when the occurrence is expressed as a number of times. */
  private TimesOccurence timesOccurence;

  /** The time unit used when the occurrence repeats every given interval. */
  private EveryTimesOccurence everyTimesOccurence;

  /** The number of times the event occurs; {@link #INVALID_VALUE} when not applicable. */
  private int times;

  /** The interval between occurrences; {@link #INVALID_VALUE} when not applicable. */
  private int every;

  /** Creates an empty occurrence rate with no parameters set. */
  public OccurenceRate() {}

  /**
   * Reconstructs an occurrence rate from its serialized pipe-separated string form.
   *
   * <p>The expected format is {@code mainOccurence|timesOccurence|everyTimesOccurence|times|every},
   * where enum fields may be the literal {@code "null"} to indicate an absent value.
   *
   * @param occurenceRate the serialized occurrence rate string, as produced by {@link #toString()}
   */
  public OccurenceRate(String occurenceRate) {
    String[] elements = occurenceRate.split("\\" + SEPARATOR);
    if (!elements[0].equalsIgnoreCase(NULL)) {
      mainOccurence = MainOccurence.valueOf(elements[0]);
    }
    if (!elements[1].equalsIgnoreCase(NULL)) {
      timesOccurence = TimesOccurence.valueOf(elements[1]);
    }
    if (!elements[2].equalsIgnoreCase(NULL)) {
      everyTimesOccurence = EveryTimesOccurence.valueOf(elements[2]);
    }
    times = Integer.valueOf(elements[3]);
    every = Integer.valueOf(elements[4]);
  }

  /**
   * Creates an occurrence rate for a main occurrence mode that requires no additional parameters
   * (typically {@link MainOccurence#OnlyOnce}).
   *
   * @param mainOccurence the main occurrence mode
   */
  public OccurenceRate(MainOccurence mainOccurence) {
    init(mainOccurence, null, null, INVALID_VALUE, INVALID_VALUE);
  }

  /**
   * Creates an occurrence rate expressed as a number of times over a given unit (typically
   * {@link MainOccurence#Times}).
   *
   * @param mainOccurence the main occurrence mode
   * @param timesOccurence the time unit for the {@code times} count
   * @param times the number of times the event occurs
   */
  public OccurenceRate(
    MainOccurence mainOccurence,
    TimesOccurence timesOccurence,
    int times
  ) {
    init(mainOccurence, timesOccurence, null, INVALID_VALUE, times);
  }

  /**
   * Creates an occurrence rate that repeats every given interval (typically
   * {@link MainOccurence#EveryTimes}).
   *
   * @param mainOccurence the main occurrence mode
   * @param everyTimesOccurence the time unit for the {@code every} interval
   * @param every the interval between occurrences
   * @param times the number of times the event occurs
   */
  public OccurenceRate(
    MainOccurence mainOccurence,
    EveryTimesOccurence everyTimesOccurence,
    int every,
    int times
  ) {
    init(mainOccurence, null, everyTimesOccurence, every, times);
  }

  /**
   * Validates and initializes the occurrence rate fields for a given main occurrence mode.
   *
   * <p>Consistency rules enforced:
   *
   * <ul>
   *   <li>{@link MainOccurence#OnlyOnce} must not specify a times or every unit.
   *   <li>{@link MainOccurence#Times} requires a {@code timesOccurence} unit and a valid
   *       {@code times} count.
   *   <li>{@link MainOccurence#EveryTimes} requires an {@code everyTimesOccurence} unit and valid
   *       {@code times} and {@code every} values.
   * </ul>
   *
   * @param mainOccurence the main occurrence mode
   * @param timesOccurence the time unit for the {@code times} count, or {@code null}
   * @param everyTimesOccurence the time unit for the {@code every} interval, or {@code null}
   * @param every the interval between occurrences, or {@link #INVALID_VALUE}
   * @param times the number of times the event occurs, or {@link #INVALID_VALUE}
   * @throws AlfrescoRuntimeException if the supplied parameters are inconsistent with the main
   *     occurrence mode
   */
  private void init(
    MainOccurence mainOccurence,
    TimesOccurence timesOccurence,
    EveryTimesOccurence everyTimesOccurence,
    int every,
    int times
  ) {
    if (
      (mainOccurence == MainOccurence.OnlyOnce) &&
      ((timesOccurence) != null || (everyTimesOccurence != null))
    ) {
      throw new AlfrescoRuntimeException("");
    }
    if (
      (mainOccurence == MainOccurence.Times) &&
      ((timesOccurence == null) || (times == INVALID_VALUE))
    ) {
      throw new AlfrescoRuntimeException("");
    }
    if (
      (mainOccurence == MainOccurence.EveryTimes) &&
      ((everyTimesOccurence == null) ||
        (times == INVALID_VALUE) ||
        (every == INVALID_VALUE))
    ) {
      throw new AlfrescoRuntimeException("");
    }
    this.mainOccurence = mainOccurence;
    this.timesOccurence = timesOccurence;
    this.everyTimesOccurence = everyTimesOccurence;
    this.times = times;
    this.every = every;
  }

  /**
   * @return the mainOccurence
   */
  public MainOccurence getMainOccurence() {
    return mainOccurence;
  }

  /**
   * @param mainOccurence the mainOccurence to set
   */
  public void setMainOccurence(MainOccurence mainOccurence) {
    this.mainOccurence = mainOccurence;
  }

  /**
   * @return the timesOccurence
   */
  public TimesOccurence getTimesOccurence() {
    return timesOccurence;
  }

  /**
   * @param timesOccurence the timesOccurence to set
   */
  public void setTimesOccurence(TimesOccurence timesOccurence) {
    this.timesOccurence = timesOccurence;
  }

  /**
   * @return the everyTimesOccurence
   */
  public EveryTimesOccurence getEveryTimesOccurence() {
    return everyTimesOccurence;
  }

  /**
   * @param everyTimesOccurence the everyTimesOccurence to set
   */
  public void setEveryTimesOccurence(EveryTimesOccurence everyTimesOccurence) {
    this.everyTimesOccurence = everyTimesOccurence;
  }

  /**
   * @return the times
   */
  public int getTimes() {
    return times;
  }

  /**
   * @param times the times to set
   */
  public void setTimes(int times) {
    this.times = times;
  }

  /**
   * @return the every
   */
  public int getEvery() {
    return every;
  }

  /**
   * @param every the every to set
   */
  public void setEvery(int every) {
    this.every = every;
  }

  /**
   * Renders an object as a string, using the {@link #NULL} token when the object is {@code null}.
   *
   * @param object the object to render, may be {@code null}
   * @return the object's string representation, or {@code "null"} if it is {@code null}
   */
  private String objectToString(Object object) {
    String result = NULL;
    if (object != null) {
      result = object.toString();
    }
    return result;
  }

  /**
   * Serializes this occurrence rate to its compact pipe-separated string form.
   *
   * <p>The produced format is {@code mainOccurence|timesOccurence|everyTimesOccurence|times|every},
   * where absent enum values are rendered as {@code "null"}. The result can be parsed back with
   * {@link #OccurenceRate(String)}.
   *
   * @return the serialized string representation of this occurrence rate
   */
  public String toString() {
    return (
      objectToString(this.mainOccurence) +
      SEPARATOR +
      objectToString(this.timesOccurence) +
      SEPARATOR +
      objectToString(this.everyTimesOccurence) +
      SEPARATOR +
      this.times +
      SEPARATOR +
      this.every
    );
  }
}
