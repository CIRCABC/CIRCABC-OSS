/* eslint-disable @typescript-eslint/no-explicit-any */
import { AbstractControl, FormGroup, ValidatorFn } from '@angular/forms';
import { SelectableNode } from 'app/core/ui-model';
import { emailWellFormed, urlWellFormed } from 'app/core/util';

/**
 * Collection of reusable Angular reactive-forms validator functions and
 * validation helpers used across CIRCABC forms.
 *
 * The module groups together {@link ValidatorFn}-compatible functions for
 * passwords, file/folder names, titles, dates and times, phone numbers,
 * emails, URLs, ports, usernames and repeat/attendance rules. Validators
 * return an error map (e.g. `{ invalidPassword: true }`) when the control is
 * invalid or `null` when it is valid, following Angular's validator contract.
 * Error keys can be turned into i18n translation codes via
 * {@link getErrorMessageTranslationCode}.
 */

/**
 * Builds the i18n translation key used to look up a validation error message.
 *
 * @param code - The raw validation error code (e.g. `invalidPassword`).
 * @returns The namespaced translation code prefixed with `validation.`.
 */
export function getErrorMessageTranslationCode(code: string): string {
  return `validation.${code}`;
}

/**
 * Validates that a control holds a password that meets CIRCABC's complexity
 * rules: 6–100 characters, at least one digit, at least one letter or special
 * character, and no whitespace.
 *
 * @param control - The form control whose value is the candidate password.
 * @returns `{ invalidPassword: true }` when the value is `null` or fails the
 * complexity requirements, otherwise `null`.
 */
export function passwordValidator(
  control: AbstractControl
): { [key: string]: boolean } | null {
  if (control.value === null) {
    return { invalidPassword: true };
  }

  // {6,100}           - Assert password is between 6 and 100 characters
  // (?=.*[0-9])       - Assert a string has at least one number
  // (?!.*\s)          - Spaces are not allowed
  // NOSONAR - password validation regex complexity is necessary for security requirements
  if (control.value.match(/^(?=.*\d)(?=.*[a-zA-Z!@#$%^&*])(?!.*\s).{6,100}$/)) {
    return null;
  }
  return { invalidPassword: true };
}

/**
 * Validates that a control value is a legal file name, rejecting names that
 * contain the reserved characters `" * \ < > ? / : |`, end with a dot, or end
 * with a trailing space.
 *
 * @param control - The form control whose value is the candidate file name.
 * @returns An `{ invalidFileName: { additionalInfo } }` error map listing the
 * forbidden characters when invalid, otherwise `null`.
 */
export function fileNameValidator(
  control: AbstractControl
): { [key: string]: any } | null {
  // prettier-ignore
  if (control.value.match(/(.*["*\\><?/:|]+.*)|(.*[.]?.*[.]+$)|(.*[ ]+$)/)) { // NOSONAR
    return {
      invalidFileName: {
        additionalInfo: String.raw` " * \ < > ? / : |`,
      },
    };
  }
  return null;
}

/**
 * Validates a required name field, ensuring it is non-empty and free of the
 * reserved characters `" * \ < > ? / : |`, trailing dots or trailing spaces.
 *
 * @param control - The form control whose value is the candidate name.
 * @returns An `{ invalidFileName: { additionalInfo } }` error map when the
 * value is empty or contains illegal characters, otherwise `null`. A `null`
 * control value is treated as valid.
 */
export function nameValidator(
  control: AbstractControl
): { [key: string]: any } | null {
  if (control.value === null) {
    return null;
  }
  if (control.value === undefined || control.value.trim().length === 0) {
    return { invalidFileName: { additionalInfo: 'empty name' } };
  }
  // prettier-ignore
  if (control.value.match(/(.*["*\\><?/:|]+.*)|(.*[.]?.*[.]+$)|(.*[ ]+$)/)) { // NOSONAR
    return {
      invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
    };
  }
  return null;
}

/**
 * Validates a multilingual title control by inspecting its English (`en`)
 * entry, requiring it to be non-empty and free of the reserved characters
 * `" * \ < > ? / : | .` and trailing spaces.
 *
 * @param control - The form control whose value is a language-keyed title map.
 * @returns `{ invalidFileName: { additionalInfo } }` when the English title is
 * empty, `{ invalidTitle: { additionalInfo } }` when it contains illegal
 * characters, otherwise `null`. A `null` control value is treated as valid.
 */
export function titleValidator(
  control: AbstractControl
): { [key: string]: any } | null {
  if (control.value === null) {
    return null;
  }
  const title: string = control.value['en'];
  if (title === undefined || title === null || title.trim().length === 0) {
    return { invalidFileName: { additionalInfo: 'empty title' } };
  }
  // prettier-ignore
  if (/(.*["*\\><?/:.|]+.*)|(.*[.]?.*[.]+$)|(.*[ ]+$)/.exec(title)) { // NOSONAR
    return {
      invalidTitle: { additionalInfo: String.raw` " * \ < > ? / : | . space` },
    };
  }
  return null;
}

/**
 * Validates that the English (`en`) entry of a multilingual title control does
 * not exceed a maximum length.
 *
 * @param control - The form control whose value is a language-keyed title map.
 * @param length - The maximum allowed length for the English title.
 * @returns `{ invalidLengthTitle: { maxLength } }` when the title is missing or
 * too long, otherwise `null`. A `null` control value is treated as valid.
 */
export function maxLengthTitleValidator(
  control: AbstractControl,
  length: number
): { [key: string]: any } | null {
  if (control.value === null) {
    return null;
  }
  const title: string = control.value['en'];
  if (title === undefined || title === null || title.length > length) {
    return { invalidLengthTitle: { maxLength: length } };
  }
  return null;
}

/**
 * Creates a group-level validator that ensures the date in the `from` control
 * is not later than the date in the `to` control.
 *
 * @param from - The name of the start-date control within the form group.
 * @param to - The name of the end-date control within the form group.
 * @returns A validator function that returns
 * `{ invalidDateRange: { from, to } }` when `from` is after `to`, otherwise an
 * empty object.
 */
export function dateLessThan(from: string, to: string) {
  return (group: AbstractControl): { [key: string]: any } => {
    const f = (group as FormGroup).controls[from];
    const t = (group as FormGroup).controls[to];
    if (f.value > t.value) {
      return {
        invalidDateRange: {
          from: f.value.toLocaleDateString(),
          to: t.value.toLocaleDateString(),
        },
      };
    }
    return {};
  };
}

/**
 * Validates that the control's date is today or later (not in the past),
 * comparing at day granularity (time-of-day is ignored).
 *
 * @param control - The form control whose value is a date.
 * @returns `{ pastDate: true }` when the date is before today, otherwise
 * `null`.
 */
// validates that the given date does not lie in the past
export function pastDateValidator(control: AbstractControl) {
  const currentDate = new Date();
  currentDate.setHours(0, 0, 0, 0);
  const result: boolean = new Date(control.value) < currentDate;
  return result ? { pastDate: true } : null;
}

/**
 * Validates that the control's date-time is now or later (not in the past),
 * comparing at full timestamp precision.
 *
 * @param control - The form control whose value is a date-time.
 * @returns `{ pastDateTime: true }` when the date-time is before now, otherwise
 * `null`.
 */
// validates that the given date does not lie in the past
export function pastDateTimeValidator(control: AbstractControl) {
  const currentDate = new Date();
  const result: boolean = new Date(control.value) < currentDate;
  return result ? { pastDateTime: true } : null;
}

/**
 * Validates that the control's date is today or earlier (not in the future),
 * comparing against the end of the current day.
 *
 * @param control - The form control whose value is a date.
 * @returns `{ futureDate: true }` when the date is after today, otherwise
 * `null`.
 */
// validates that the given date does not lie in the future
export function futureDateValidator(control: AbstractControl) {
  const currentDate = new Date();
  currentDate.setHours(23, 59, 59, 999);
  const result: boolean = new Date(control.value) > currentDate;
  return result ? { futureDate: true } : null;
}

/**
 * Validates a composite date/time control (`{ date, startTime, endTime }`),
 * ensuring the times are well-formed `HH:mm` values, both start and end
 * date-times are in the future, and the start is before the end.
 *
 * @param control - The form control whose value exposes `date`, `startTime`
 * and `endTime`.
 * @returns `{ wrongDateRanges: true }` when the date is missing, a time is
 * malformed, or the ranges are invalid, otherwise `null`.
 */
// validates that the given date and/or times do not lie in the past
export function dateInfoValidator(control: AbstractControl) {
  const fullDate: Date = control.value.date as Date;
  if (fullDate === null) {
    return { wrongDateRanges: true };
  }
  let startTime: string = control.value.startTime as string;
  let endTime: string = control.value.endTime as string;

  // pad the hour
  if (startTime.length < 5) {
    startTime = `0${startTime}`;
  }
  if (endTime.length < 5) {
    endTime = `0${endTime}`;
  }

  // check if time format matches
  if (!(timeStringOK(startTime) && timeStringOK(endTime))) {
    return { wrongDateRanges: true };
  }

  // create start and end datetimes
  let hour = Number(startTime.substring(0, 2));
  let minutes = Number(startTime.substring(3, 5));
  const startDateTime: Date = new Date(
    fullDate.getFullYear(),
    fullDate.getMonth() + 1,
    fullDate.getDate(),
    hour,
    minutes
  );

  hour = Number(endTime.substring(0, 2));
  minutes = Number(endTime.substring(3, 5));
  const endDateTime: Date = new Date(
    fullDate.getFullYear(),
    fullDate.getMonth() + 1,
    fullDate.getDate(),
    hour,
    minutes
  );
  const currentDateTime = new Date();

  // check if date ranges are ok
  if (
    currentDateTime <= startDateTime &&
    currentDateTime <= endDateTime &&
    startDateTime < endDateTime
  ) {
    return null;
  }

  return { wrongDateRanges: true };
}

/**
 * Checks whether a string matches the 24-hour `HH:mm` time format.
 *
 * @param time - The time string to validate.
 * @returns `true` when the string is a valid `HH:mm` time, otherwise `false`
 * (including when `time` is `undefined`).
 */
export function timeStringOK(time: string): boolean {
  if (time === undefined) {
    return false;
  }
  // check the HH:mm format
  return /^(\d|0\d|1\d|2[0-3]):[0-5]\d$/.exec(time) !== null;
}

/**
 * Validates a composite time control (`{ date, startTime, endTime }`) by
 * ensuring the start date-time is strictly before the end date-time.
 *
 * @param control - The form control whose value exposes `date`, `startTime`
 * and `endTime`.
 * @returns `{ wrongDateRanges: true }` when the start is not before the end,
 * otherwise `null`.
 */
// validates that the given times are not backwards
export function timeInfoValidator(control: AbstractControl) {
  const startDateTime = new Date(
    `${control.value.date as string} ${control.value.startTime as string}`
  );
  const endDateTime = new Date(
    `${control.value.date as string} ${control.value.endTime as string}`
  );

  // check if date ranges are ok
  if (startDateTime < endDateTime) {
    return null;
  }

  return { wrongDateRanges: true };
}

/**
 * Validates a recurrence configuration: when `repeats` is enabled, the fields
 * relevant to the chosen recurrence mode (`whenSelect` or `everySelect`) must
 * be filled in with valid whole-number occurrences.
 *
 * @param control - The form control whose value holds the repeat settings.
 * @returns `{ wrongWhenRepeatValues: true }` or
 * `{ wrongEveryRepeatValues: true }` when the corresponding recurrence fields
 * are incomplete or invalid, otherwise `null`.
 */
// validates that if repeats is selected, all involved fields must have a value
export function repeatsSelectedValidator(control: AbstractControl) {
  if (control.value.repeats) {
    // if the appointment repeats
    if (
      control.value.whenOrEverySelect === 'whenSelect' &&
      (control.value.timesOccurence === '' ||
        !checkWholeStringNumber(control.value.times))
    ) {
      // for a given amount of time
      return { wrongWhenRepeatValues: true };
    }
    if (
      control.value.whenOrEverySelect === 'everySelect' &&
      (control.value.everyTimesOccurence === '' ||
        !checkWholeStringNumber(control.value.everyTime) ||
        !checkWholeStringNumber(control.value.times))
    ) {
      // every given amount of time
      return { wrongEveryRepeatValues: true };
    }
  }

  return null;
}

/**
 * Validates that a phone number control is empty or a whole number (integer).
 *
 * @param control - The form control whose value is the candidate phone number.
 * @returns `{ invalidPhone: true }` when the value is a non-integer, otherwise
 * `null` (empty and `undefined` values are treated as valid).
 */
// validate that the phone number is a whole number (integer)
export function phoneValidator(control: AbstractControl) {
  if (
    control.value === undefined ||
    control.value === '' ||
    checkWholeStringNumber(control.value)
  ) {
    return null;
  }
  return { invalidPhone: true };
}

/**
 * Determines whether the given string represents a whole number (integer).
 *
 * @param value - The string to test.
 * @returns `true` when the string is a non-empty, parseable integer, otherwise
 * `false`.
 */
// check if the given string is a whole number
function checkWholeStringNumber(value: string): boolean {
  return (
    value !== '' &&
    !Number.isNaN(Number.parseFloat(value)) &&
    Number.parseFloat(value) % 1 === 0
  );
}

/**
 * Validates that an audience selection has at least one attendant: either the
 * audience is open, or invited users/profiles are present, or valid external
 * emails are supplied.
 *
 * @param control - The form control whose value exposes `audienceStatusOpen`,
 * `invitedUsersOrProfiles` and `invitedExternalEmails`.
 * @returns `{ attendantsMissing: true }` when no attendants are defined,
 * otherwise `null`.
 */
// check if the list of attendants has at least one attendant
export function attendantsValidator(control: AbstractControl) {
  const invitedUsersOrProfiles: string[] = control.value.invitedUsersOrProfiles;

  if (
    control.value.audienceStatusOpen ||
    (!control.value.audienceStatusOpen &&
      invitedUsersOrProfiles !== null &&
      invitedUsersOrProfiles.length !== 0) ||
    (control.value.invitedExternalEmails.length !== 0 &&
      validEmails(control.value.invitedExternalEmails))
  ) {
    return null;
  }

  return { attendantsMissing: true };
}

/**
 * Validates a newline-separated list of email addresses, delegating to
 * {@link validEmails}.
 *
 * @param control - The form control whose value is a multi-line string of
 * email addresses.
 * @returns `{ invalidEmails: true }` when any line is malformed, otherwise
 * `null`.
 */
// checks if email addresses are well formed (validator compatible)
export function emailsValidator(control: AbstractControl) {
  if (validEmails(control.value)) {
    return null;
  }
  return { invalidEmails: true };
}

/**
 * Checks whether every non-empty line in a newline-separated string is a
 * well-formed email address.
 *
 * @param text - The multi-line string of email addresses (one per line).
 * @returns `true` when all non-empty lines are valid emails, or when `text` is
 * `null`/`undefined`; otherwise `false`.
 */
// checks a list of emails (each email on one line)
export function validEmails(text: string): boolean {
  if (text === undefined || text === null) {
    return true;
  }

  const lines = text.split('\n');

  for (const line of lines) {
    if (line !== '' && !emailWellFormed(line)) {
      return false;
    }
  }

  return true;
}

/**
 * Validates that a control holds a single well-formed email address.
 *
 * @param control - The form control whose value is the candidate email.
 * @returns `{ invalidEmail: true }` when the value is not a well-formed email,
 * otherwise `null`.
 */
// checks if an email address is well formed (validator compatible)
export function emailValidator(control: AbstractControl) {
  if (emailWellFormed(control.value)) {
    return null;
  }
  return { invalidEmail: true };
}

/**
 * Validates that a control holds a well-formed URL. Empty, `null` and
 * whitespace-only values are treated as valid (optional field).
 *
 * @param control - The form control whose value is the candidate URL.
 * @returns `{ invalidURL: true }` when the value is a malformed URL, otherwise
 * `null`.
 */
export function urlValidator(control: AbstractControl) {
  if (
    control.value === undefined ||
    control.value === null ||
    control.value.trim() === ''
  ) {
    return null;
  }

  if (urlWellFormed(control.value)) {
    return null;
  }
  return { invalidURL: true };
}

/**
 * Validates that a multilingual title control has at least one non-empty
 * language entry. The `required` error is only reported once the control has
 * been touched (dirty).
 *
 * @param control - The form control whose value is a language-keyed map.
 * @returns `{ required: true }` when all entries are empty and the control is
 * dirty, otherwise `null`.
 */
export function nonEmptyTitle(control: AbstractControl) {
  let empty = true;
  if (control?.value) {
    for (const key of Object.keys(control.value)) {
      if (control.value[key] !== '' && control.value[key] !== null) {
        empty = false;
      }
    }
  }

  if (!empty) {
    return null;
  }
  if (empty && control.dirty) {
    return { required: true };
  }
  return null;
}

/**
 * Validates that a control holds a valid TCP/UDP port number (1–65535). Empty
 * and `undefined` values are treated as valid (optional field).
 *
 * @param control - The form control whose value is the candidate port.
 * @returns `{ invalidPort: true }` when the value is not a valid port number,
 * otherwise `null`.
 */
export function portValidator(control: AbstractControl) {
  if (control.value === undefined || control.value === '') {
    return null;
  }
  if (
    !Number.isNaN(control.value) &&
    Number(control.value) > 0 &&
    Number(control.value) <= 65535
  ) {
    return null;
  }
  return { invalidPort: true };
}

/**
 * Validates that the control's value does not collide with the name of any
 * existing file or folder in the given directory contents.
 *
 * @param control - The form control whose value is the candidate name.
 * @param contents - The current directory contents to check against.
 * @returns `{ fileFolderExists: true }` when a node with the same name already
 * exists, otherwise `null`. Empty values or empty contents are treated as
 * valid.
 */
export function fileFolderExistsValidator(
  control: AbstractControl,
  contents: SelectableNode[]
) {
  if (
    control.value === undefined ||
    control.value === '' ||
    contents === undefined ||
    contents.length === 0
  ) {
    return null;
  }
  if (
    contents.map((node: SelectableNode) => node.name).includes(control.value)
  ) {
    return { fileFolderExists: true };
  }
  return null;
}

/**
 * Creates a validator that rejects control values matching a forbidden regular
 * expression.
 *
 * @param nameRe - The regular expression describing forbidden values.
 * @returns A {@link ValidatorFn} returning `{ forbiddenNameRegExp: { name } }`
 * when the value matches `nameRe`, otherwise `null`.
 */
export function forbiddenNameRegExpValidator(nameRe: RegExp): ValidatorFn {
  return (control: AbstractControl): { [key: string]: {} } | null => {
    const name = control.value;
    const no = nameRe.test(name);
    return no ? { forbiddenNameRegExp: { name } } : null;
  };
}

/**
 * Creates a validator that rejects control values contained in a list of
 * forbidden names.
 *
 * @param names - The list of disallowed values.
 * @returns A {@link ValidatorFn} returning `{ forbiddenNameArray: { name } }`
 * when the value is in `names`, otherwise `null`.
 */
export function forbiddenNameArrayValidator(names: string[]): ValidatorFn {
  return (control: AbstractControl): { [key: string]: {} } | null => {
    const name = control.value;
    const no = names.includes(name);
    return no ? { forbiddenNameArray: { name } } : null;
  };
}

/**
 * Validates a username: it must be 5–32 characters long and contain only
 * alphanumeric characters. Empty and `null` values are treated as valid.
 *
 * @param control - The form control whose value is the candidate username.
 * @returns `{ invalidUsernameLength: { maxLength, minLength } }` when the
 * length is out of range, `{ invalidUsernameCharacter: true }` when it
 * contains non-alphanumeric characters, otherwise `null`.
 */
export function usernameValidator(
  control: AbstractControl
): { [key: string]: any } | null {
  if (control.value === null || control.value === '') {
    return null;
  }

  const minLength = 5;
  const maxLength = 32;

  const regexp = new RegExp(`^.{${minLength},${maxLength}}$`);
  if (!control.value.match(regexp)) {
    return {
      invalidUsernameLength: { maxLength: maxLength, minLength: minLength },
    };
  }

  if (!control.value.match(/^[a-zA-Z0-9]*$/)) {
    return { invalidUsernameCharacter: true };
  }

  return null;
}
