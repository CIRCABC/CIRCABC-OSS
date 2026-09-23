/**
 * @fileoverview
 * Shared, framework-agnostic utility functions used across the CIRCABC
 * frontend. This module groups small, reusable helpers for common concerns
 * such as object sanitisation, array manipulation, i18n-aware sorting, date
 * and time formatting, content/MIME type detection for preview features,
 * string validation (email/URL/image extensions) and user lookup.
 *
 * All exports are pure functions (with the exception of {@link getUserFullName},
 * which performs an asynchronous backend call) and carry no component state.
 */
import { ActionType } from 'app/action-result';
import {
  EventItemDefinition,
  Node as ModelNode,
  UserService,
} from 'app/core/generated/circabc';

/**
 * Removes, in place, every property whose value is strictly `null` from the
 * given object.
 *
 * @param obj The object to sanitise. Mutated directly by deleting null keys.
 * @returns The same object reference, with all `null`-valued properties removed.
 */

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const removeNulls = (obj: any) => {
  Object.keys(obj).forEach((key) => {
    if (obj[key] === null) {
      delete obj[key];
    }
  });
  return obj;
};

/**
 * Return all elements from a that does not exists in b
 * compared by key aitem[key] !== bitem[key]
 * @param a Array that need to be filtered
 * @param b Array that
 * @param key property name of object that is contained in arrays
 * @returns new array all elements array a that does not exists in array b
 */

export function arrayDiff<T, K extends keyof T>(a: T[], b: T[], key: K): T[] {
  return a.filter((aitem: T) => {
    return !b.some((bitem: T) => aitem[key] === bitem[key]);
  });
}

// to see if we can use collator
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Collator/compare
/**
 * Resolves a localized string from a language-keyed map, falling back
 * gracefully when the preferred languages are not available.
 *
 * Resolution order: the current language, then the default language, then the
 * first available entry in the map, and finally an empty string.
 *
 * @param obj Map of language codes to their translated string.
 * @param currentLang Preferred language code to look up first.
 * @param defaultLang Fallback language code used when `currentLang` is missing.
 * @returns The best-matching localized string, or an empty string if none exist.
 */
function getI18nString(
  obj: { [key: string]: string },
  currentLang: string,
  defaultLang: string
): string {
  if (obj[currentLang]) return obj[currentLang];
  if (obj[defaultLang]) return obj[defaultLang];

  for (const key in obj) {
    if (Object.hasOwn(obj, key)) {
      return obj[key];
    }
  }
  return '';
}

/**
 * Locale-aware comparator for two language-keyed string maps, suitable for use
 * with `Array.prototype.sort`.
 *
 * The comparison uses the localized value resolved via {@link getI18nString};
 * when a resolved value is blank, the provided fallback name (`aname`/`bname`)
 * is used instead.
 *
 * @param a First language-keyed map to compare. Returns `0` if undefined.
 * @param b Second language-keyed map to compare. Returns `0` if undefined.
 * @param currentLang Preferred language code used for resolution and collation.
 * @param defaultLang Fallback language code used when `currentLang` is missing.
 * @param aname Fallback display name used when `a` resolves to a blank string.
 * @param bname Fallback display name used when `b` resolves to a blank string.
 * @returns A negative number if `a` sorts before `b`, positive if after, `0` if equal.
 */
export function sortI18nProperty(
  a: { [key: string]: string } | undefined,
  b: { [key: string]: string } | undefined,
  currentLang = 'en',
  defaultLang = 'en',
  aname = '',
  bname = ''
): number {
  if (a === undefined || b === undefined) {
    return 0;
  }

  let astring = getI18nString(a, currentLang, defaultLang);
  let bstring = getI18nString(b, currentLang, defaultLang);

  if (astring.trim() === '') astring = aname;
  if (bstring.trim() === '') bstring = bname;

  return astring.localeCompare(bstring, currentLang);
}
/**
 * Formats a `Date` as an ISO date-only string (`YYYY-MM-DD`) in UTC.
 *
 * @param date The date to format.
 * @returns The `YYYY-MM-DD` representation, or an empty string if `date` is
 *   `undefined` or `null`.
 */
export function getFullDate(date: Date): string {
  if (date === undefined || date === null) {
    return '';
  }
  return date.toISOString().slice(0, 10);
}

/**
 * Computes the next sort descriptor for a toggling, single-column sort control.
 *
 * Given the current sort value and the column that was clicked, it cycles the
 * direction: a new column starts ascending, an ascending column becomes
 * descending, and a descending column returns to ascending.
 *
 * @param oldSort The current sort descriptor (e.g. `name_ASC`, `name_DESC`).
 * @param newSort The base name of the column that was activated.
 * @returns The new sort descriptor (`<newSort>_ASC` or `<newSort>_DESC`).
 */
export function changeSort(oldSort: string, newSort: string) {
  let result = '';
  if (!oldSort.includes(newSort)) {
    result = `${newSort}_ASC`;
  } else if (oldSort === `${newSort}_ASC`) {
    result = `${newSort}_DESC`;
  } else if (oldSort === `${newSort}_DESC`) {
    result = `${newSort}_ASC`;
  }
  return result;
}

// pad values with '0' to conform to the date and time formats
/**
 * Left-pads a number with a single leading zero when it is a single digit, so
 * that it conforms to the two-digit date and time formats.
 *
 * @param value The numeric value to pad.
 * @returns The value as a string, prefixed with `0` when it has a single digit.
 */
export function padWithLeadingZero(value: number): string {
  const valueString: string = value.toString();

  return valueString.length === 1 ? `0${valueString}` : valueString;
}

/**
 * Formats the calendar date portion of a `Date` as `YYYY-MM-DD` using the
 * local time zone.
 *
 * @param date The date to format.
 * @returns The local-date string in `YYYY-MM-DD` format.
 */
export function getFormattedDate(date: Date) {
  return `${date.getFullYear()}-${padWithLeadingZero(
    date.getMonth() + 1
  )}-${padWithLeadingZero(date.getDate())}`;
}

/**
 * Formats the time portion of a `Date` as `HH:mm` using the local time zone.
 *
 * @param date The date whose time should be formatted.
 * @returns The local-time string in `HH:mm` format.
 */
export function getFormattedTime(date: Date) {
  return `${padWithLeadingZero(date.getHours())}:${padWithLeadingZero(
    date.getMinutes()
  )}`;
}

/**
 * Builds the i18n translation key for the success message of a given action.
 *
 * @param actionType The action that succeeded.
 * @returns The translation key in the form `<actionType>.succeed`.
 */
export function getSuccessTranslation(actionType: ActionType): string {
  return `${actionType}.succeed`;
}

/**
 * Builds the i18n translation key for the failure message of a given action.
 *
 * @param actionType The action that failed.
 * @returns The translation key in the form `<actionType>.failed`.
 */
export function getErrorTranslation(actionType: ActionType): string {
  return `${actionType}.failed`;
}

/**
 * Determines whether a node's content can be rendered by the full-featured
 * preview (documents, spreadsheets, presentations, PDFs, images, etc.).
 *
 * @param content The node whose MIME type is inspected.
 * @returns `true` if the node's MIME type is in the supported preview list.
 */
export function isContentPreviewableFull(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    [
      'text/plain',
      'text/xml',
      'text/csv',
      'application/xml',
      'application/pdf',
      'application/rtf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'application/vnd.oasis.opendocument.presentation',
      'application/vnd.oasis.opendocument.spreadsheet',
      'application/vnd.oasis.opendocument.text',
      'application/msword',
      'application/vnd.ms-excel',
      'application/vnd.visio',
      'image/gif',
      'image/jpeg',
      'image/png',
    ].includes(content.properties.mimetype.toLowerCase())
  );
}
/**
 * Determines whether a node's content can be shown in the lightweight inline
 * preview, i.e. PDFs, common raster images, or any audio/video content.
 *
 * @param content The node whose MIME type is inspected.
 * @returns `true` if the node can be previewed inline.
 */
export function isContentPreviewable(content: ModelNode): boolean {
  let result =
    content?.properties?.mimetype !== undefined &&
    ['application/pdf', 'image/gif', 'image/jpeg', 'image/png'].includes(
      content.properties.mimetype.toLowerCase()
    );
  result = result || isContentAudio(content) || isContentVideo(content);
  return result;
}

/**
 * Determines whether a node's content is a supported raster image
 * (GIF, PNG or JPEG).
 *
 * @param content The node whose MIME type is inspected.
 * @returns `true` if the node is an image of a supported type.
 */
export function isContentImage(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    ['image/gif', 'image/png', 'image/jpeg'].includes(
      content.properties.mimetype.toLowerCase()
    )
  );
}

/**
 * Determines whether a node's content is a video, based on its MIME type
 * containing the `video` token.
 *
 * @param content The node whose name and MIME type are inspected.
 * @returns `true` if the node appears to be a video.
 */
export function isContentVideo(content: ModelNode): boolean {
  return (
    content?.name !== undefined &&
    content.properties?.mimetype?.toLowerCase().includes('video') === true
  );
}

/**
 * Determines whether a node's content is audio, based on its MIME type
 * containing the `audio` token.
 *
 * @param content The node whose name and MIME type are inspected.
 * @returns `true` if the node appears to be audio.
 */
export function isContentAudio(content: ModelNode): boolean {
  return (
    content?.name !== undefined &&
    content.properties?.mimetype?.toLowerCase().includes('audio') === true
  );
}

/**
 * Determines whether a node's content is a PDF document.
 *
 * @param content The node whose MIME type is inspected.
 * @returns `true` if the node's MIME type is `application/pdf`.
 */
export function isContentPdf(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    ['application/pdf'].includes(content.properties.mimetype.toLowerCase())
  );
}

/**
 * Parses an encoded event occurrence-rate descriptor into a list of i18n key
 * parts and their associated values.
 *
 * Recognised formats yield: `['na']` when empty/undefined, `['once']` for
 * `OnlyOnce|...`, `['times', when, times]` for `Times|...`, and
 * `['everytimes', when, times, repeat]` for `EveryTimes|...`.
 *
 * @param occurrenceRate The raw occurrence-rate string, or `undefined`.
 * @returns An array whose first element is the i18n key and whose remaining
 *   elements are the extracted values, if any.
 */
export function translateOccurrenceRate(
  occurrenceRate: string | undefined
): string[] {
  const result = [];
  if (occurrenceRate === undefined || occurrenceRate === '') {
    result.push('na');
  } else if (occurrenceRate.startsWith('OnlyOnce|')) {
    result.push('once');
  } else if (occurrenceRate.startsWith('Times|')) {
    const match = /Times\|(.*)\|null\|(\d+)\|\d{1,4}/.exec(occurrenceRate); // NOSONAR - regex pattern is intentional
    if (match !== null) {
      const when = match[1];
      const times = match[2];
      result.push('times', when, times);
    }
  } else if (occurrenceRate.startsWith('EveryTimes|')) {
    const match = /EveryTimes\|null\|(.*)\|(\d+)\|(\d+)/.exec(occurrenceRate);
    if (match !== null) {
      const when = match[1];
      const times = match[2];
      const repeat = match[3];
      result.push('everytimes', when, times, repeat);
    }
  }
  return result;
}

/**
 * Generates a random, fully opaque hexadecimal color string.
 *
 * Intended for cosmetic UI purposes only; it is not cryptographically secure.
 *
 * @returns A color in the form `#RRGGBB` with uppercase hex digits.
 */
export function getRandomColor(): string {
  const hexNibbles = '0123456789ABCDEF';
  let color = '#';
  for (let idx = 0; idx < 6; idx += 1) {
    color += hexNibbles[Math.floor(Math.random() * 16)]; // NOSONAR - cosmetic UI color, not security-sensitive
  }
  return color;
}

/**
 * Comparator that orders two events by their start time (`HH:mm`), suitable
 * for use with `Array.prototype.sort`.
 *
 * Events without a start time are ordered before those that have one.
 *
 * @param event1 The first event to compare.
 * @param event2 The second event to compare.
 * @returns A negative number if `event1` starts earlier, positive if later,
 *   and `0` when they are equal or both lack a start time.
 */
export function eventsStartTimeComparator(
  event1: EventItemDefinition,
  event2: EventItemDefinition
): number {
  if (event1.startTime === undefined && event2.startTime === undefined) {
    return 0;
  }

  if (event1.startTime === undefined) {
    return -1;
  }
  const event1StartTime: number =
    Number(event1.startTime.substring(0, 2)) * 60 +
    Number(event1.startTime.substring(3, 5));

  if (event2.startTime === undefined) {
    return 1;
  }
  const event2StartTime: number =
    Number(event2.startTime.substring(0, 2)) * 60 +
    Number(event2.startTime.substring(3, 5));

  if (event1StartTime > event2StartTime) {
    return 1;
  }
  if (event1StartTime < event2StartTime) {
    return -1;
  }
  return 0;
}

/**
 * Truncates a string to a maximum length, appending an ellipsis (`...`) when
 * the text exceeds that length.
 *
 * @param text The text to truncate.
 * @param length The maximum allowed length before truncation is applied.
 * @returns The original text if within the limit, otherwise the text shortened
 *   to `length - 3` characters followed by `...`.
 */
export function truncate(text: string, length: number): string {
  if (text.length > length) {
    return `${text.substring(0, (length ?? 10) - 3)}...`;
  }
  return text;
}

// check if an email address is well formed
/**
 * Validates that a string is a well-formed email address.
 *
 * @param email The candidate email address.
 * @returns `true` if the value matches the expected email pattern.
 */
export function emailWellFormed(email: string): boolean {
  // prettier-ignore
  const emailExpression = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}])|(([a-zA-Z\-\d]+\.)+[a-zA-Z]{2,}))$/; // NOSONAR
  return emailExpression.test(email);
}

// check if an url is well formed
/**
 * Validates that a string is a well-formed URL (http, https, ftp, file, or a
 * `www.`/`ftp.` prefixed host).
 *
 * @param email The candidate URL string (the parameter name is historical).
 * @returns `true` if the value matches the expected URL pattern.
 */
export function urlWellFormed(email: string): boolean {
  // prettier-ignore
  const expression = /(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)(?:\([-A-Z\d+&@#/%=~_|$?!:,.]*\)|[-A-Z\d+&@#/%=~_|$?!:,.])*(?:\([-A-Z\d+&@#/%=~_|$?!:,.]*\)|[A-Z\d+&@#/%=~_|$])/i; // NOSONAR
  return expression.test(email);
}

// check if a string has extension of image
/**
 * Checks whether a file name ends with a recognised image extension
 * (jpg, jpeg, bmp, png or gif).
 *
 * @param name The file name to inspect.
 * @returns `true` if the name has a supported image extension, `false` when it
 *   does not or when `name` is `undefined`.
 */
export function imageExtensionValid(name: string): boolean {
  if (name === undefined) {
    return false;
  }
  const lowerName = name.toLowerCase();
  return (
    lowerName.endsWith('jpg') ||
    lowerName.endsWith('bmp') ||
    lowerName.endsWith('png') ||
    lowerName.endsWith('gif') ||
    lowerName.endsWith('jpeg')
  );
}

/**
 * Resolves the display full name for a user by id, using the backend
 * {@link UserService}.
 *
 * The special id `System` is returned verbatim. If the lookup fails, the error
 * is logged and the raw user id is returned as a fallback so the UI can still
 * render something.
 *
 * @param userId The identifier of the user to resolve.
 * @param userService The generated service used to fetch user details.
 * @returns A promise resolving to the user's `"firstname lastname"`, the
 *   literal `System`, or the raw `userId` on failure.
 */
export async function getUserFullName(
  userId: string,
  userService: UserService
) {
  let fullName: string;
  if (userId === 'System') {
    fullName = userId;
  } else {
    try {
      const user = await userService.getUserAsync({ userId });
      fullName = `${user.firstname} ${user.lastname}`;
    } catch (error) {
      console.error(
        `Error getting info about user '${userId}'. User card will not be displayed.`
      );
      console.error(error);
      fullName = userId;
    }
  }
  return fullName;
}

/**
 * Returns a new array with duplicate items removed, using a caller-supplied
 * equality comparator to determine duplication. The first occurrence of each
 * distinct item is preserved and order is maintained.
 *
 * @typeParam T The element type of the array.
 * @param items The source array to de-duplicate.
 * @param comparator Predicate returning `true` when two items are considered equal.
 * @returns A new array containing only the first occurrence of each item.
 */
export function removeDuplicates<T>(
  items: T[],
  comparator: (item1: T, item2: T) => boolean
): T[] {
  const newItems: T[] = [];
  for (const item of items) {
    if (!contains(newItems, item, comparator)) {
      newItems.push(item);
    }
  }
  return newItems;
}

/**
 * Determines whether an array already contains an item equal to the given one,
 * according to the supplied comparator.
 *
 * @typeParam T The element type of the array.
 * @param items The array to search.
 * @param item The item to look for.
 * @param comparator Predicate returning `true` when two items are considered equal.
 * @returns `true` if an equal item is found in `items`.
 */
function contains<T>(
  items: T[],
  item: T,
  comparator: (item1: T, item2: T) => boolean
): boolean {
  for (const thisItem of items) {
    if (comparator(thisItem, item)) {
      return true;
    }
  }
  return false;
}

/**
 * Converts a date string of the form produced by `Date.prototype.toString`
 * (e.g. `Wed Apr 05 2023 ...`) into a `YYYY-MM-DD` string.
 *
 * The input is split on spaces; the year, month name and day tokens are
 * re-ordered, with the month name mapped to its two-digit number.
 *
 * @param str The space-separated date string to convert.
 * @returns The date formatted as `YYYY-MM-DD`.
 */
export function convertDate(str: string): string {
  const months: { [x: string]: string } = {
    Jan: '01',
    Feb: '02',
    Mar: '03',
    Apr: '04',
    May: '05',
    Jun: '06',
    Jul: '07',
    Aug: '08',
    Sep: '09',
    Oct: '10',
    Nov: '11',
    Dec: '12',
  };

  const date = str.split(' ');

  return [date[3], months[date[1]], date[2]].join('-');
}
/**
 * Adjusts a `Date` to compensate for daylight saving time so that the wall
 * clock time is normalised against the year's standard-time offset.
 *
 * It computes the difference between the date's current offset and the maximum
 * (standard-time) offset observed in January and July, then shifts the minutes
 * accordingly.
 *
 * @param dt The date to adjust. Mutated in place.
 * @returns The same, adjusted `Date` reference.
 */
export function compensateDST(dt: Date) {
  const janOffset = new Date(dt.getFullYear(), 0, 1).getTimezoneOffset();
  const julOffset = new Date(dt.getFullYear(), 6, 1).getTimezoneOffset();
  const dstMinutes = dt.getTimezoneOffset() - Math.max(janOffset, julOffset);
  dt.setMinutes(dt.getMinutes() + dstMinutes);

  return dt;
}

/**
 * Convert date string in format "DD/MM/YYYY" to "YYYY-MM-DD"
 *
 * @param {string} dateString - The date string in the format "DD/MM/YYYY"
 * @return {string} The new date string in the format "YYYY-MM-DD"
 */
export function convertDateFormat(dateString: string | null) {
  if (dateString === null) {
    return null;
  }
  // Split the date string into day, month, and year parts
  const parts = dateString.split('/');

  // Re-arrange the parts to form a new date string in the format "YYYY-MM-DD"
  const newDateString = `${parts[2]}-${parts[1]}-${parts[0]}`;

  return newDateString;
}
