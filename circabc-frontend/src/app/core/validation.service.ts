/* eslint-disable @typescript-eslint/no-explicit-any */
// original version created by Cory Rylan: https://coryrylan.com/blog/angular-2-form-builder-and-validation-management
import { Injectable } from '@angular/core';
import { AbstractControl, FormGroup, ValidatorFn } from '@angular/forms';
import { SelectableNode } from 'app/core/ui-model';
import { emailWellFormed, urlWellFormed } from 'app/core/util';

@Injectable({
  providedIn: 'root',
})
export class ValidationService {
  static getErrorMessageTranslationCode(code: string): string {
    return `validation.${code}`;
  }

  static passwordValidator(
    control: AbstractControl
  ): { [key: string]: boolean } | null {
    if (control.value === null) {
      return { invalidPassword: true };
    }

    // {6,100}           - Assert password is between 6 and 100 characters
    // (?=.*[0-9])       - Assert a string has at least one number
    // (?!.*\s)          - Spaces are not allowed
    if (
      control.value.match(/^(?=.*\d)(?=.*[a-zA-Z!@#$%^&*])(?!.*\s).{6,100}$/)
    ) {
      return null;
    } else {
      return { invalidPassword: true };
    }
  }

  static fileNameValidator(
    control: AbstractControl
  ): { [key: string]: any } | null {
    if (
      control.value.match(
        /(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$)/
      )
    ) {
      return {
        invalidFileName: {
          additionalInfo: ' " * \\ < > ? / : |',
        },
      };
    } else {
      return null;
    }
  }

  static nameValidator(
    control: AbstractControl
  ): { [key: string]: any } | null {
    if (control.value === null) {
      return null;
    }
    if (control.value === undefined || control.value.trim().length === 0) {
      return { invalidFileName: { additionalInfo: 'empty name' } };
    }
    if (
      control.value.match(
        /(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$)/
      )
    ) {
      return {
        invalidFileName: { additionalInfo: ' " * \\ < > ? / : |' },
      };
    } else {
      return null;
    }
  }

  static titleValidator(
    control: AbstractControl
  ): { [key: string]: any } | null {
    if (control.value === null) {
      return null;
    }
    const title: string = control.value['en'];
    if (title === undefined || title === null || title.trim().length === 0) {
      return { invalidFileName: { additionalInfo: 'empty title' } };
    }
    if (
      title.match(/(.*[\"\*\\\>\<\?\/\:\.\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$)/)
    ) {
      return {
        invalidTitle: { additionalInfo: ' " * \\ < > ? / : | . space' },
      };
    } else {
      return null;
    }
  }

  static maxLengthTitleValidator(
    control: AbstractControl,
    length: number
  ): { [key: string]: any } | null {
    if (control.value === null) {
      return null;
    }
    const title: string = control.value['en'];
    if (title === undefined || title === null || title.length > length) {
      return { invalidLengthTitle: { maxLength: length } };
    } else {
      return null;
    }
  }

  static dateLessThan(from: string, to: string) {
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

  // validates that the given date does not lie in the past
  static pastDateValidator(control: AbstractControl) {
    const currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);
    const result: boolean = new Date(control.value) < currentDate;
    return result ? { pastDate: true } : null;
  }

  // validates that the given date does not lie in the past
  static pastDateTimeValidator(control: AbstractControl) {
    const currentDate = new Date();
    const result: boolean = new Date(control.value) < currentDate;
    return result ? { pastDateTime: true } : null;
  }

  // validates that the given date does not lie in the future
  static futureDateValidator(control: AbstractControl) {
    const currentDate = new Date();
    currentDate.setHours(23, 59, 59, 999);
    const result: boolean = new Date(control.value) > currentDate;
    return result ? { futureDate: true } : null;
  }

  // validates that the given date and/or times do not lie in the past
  static dateInfoValidator(control: AbstractControl) {
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
    if (
      !ValidationService.timeStringOK(startTime) ||
      !ValidationService.timeStringOK(endTime)
    ) {
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

  static timeStringOK(time: string): boolean {
    if (time === undefined) {
      return false;
    }
    // check the HH:mm format
    return time.match(/^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/) !== null;
  }

  // validates that the given times are not backwards
  static timeInfoValidator(control: AbstractControl) {
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

  // validates that if repeats is selected, all involved fields must have a value
  static repeatsSelectedValidator(control: AbstractControl) {
    if (control.value.repeats) {
      // if the appointment repeats
      if (
        control.value.whenOrEverySelect === 'whenSelect' &&
        (control.value.timesOccurence === '' ||
          !ValidationService.checkWholeStringNumber(control.value.times))
      ) {
        // for a given amount of time
        return { wrongWhenRepeatValues: true };
      } else if (
        control.value.whenOrEverySelect === 'everySelect' &&
        (control.value.everyTimesOccurence === '' ||
          !ValidationService.checkWholeStringNumber(control.value.everyTime) ||
          !ValidationService.checkWholeStringNumber(control.value.times))
      ) {
        // every given amount of time
        return { wrongEveryRepeatValues: true };
      }
    }

    return null;
  }

  // validate that the phone number is a whole number (integer)
  static phoneValidator(control: AbstractControl) {
    if (
      control.value === undefined ||
      control.value === '' ||
      ValidationService.checkWholeStringNumber(control.value)
    ) {
      return null;
    } else {
      return { invalidPhone: true };
    }
  }

  // check if the given string is a whole number
  private static checkWholeStringNumber(value: string): boolean {
    return (
      value !== '' && !isNaN(parseFloat(value)) && parseFloat(value) % 1 === 0
    );
  }

  // check if the list of attendants has at least one attendant
  static attendantsValidator(control: AbstractControl) {
    const invitedUsersOrProfiles: string[] =
      control.value.invitedUsersOrProfiles;

    if (
      control.value.audienceStatusOpen ||
      (!control.value.audienceStatusOpen &&
        invitedUsersOrProfiles !== null &&
        invitedUsersOrProfiles.length !== 0) ||
      (control.value.invitedExternalEmails.length !== 0 &&
        ValidationService.validEmails(control.value.invitedExternalEmails))
    ) {
      return null;
    }

    return { attendantsMissing: true };
  }

  // checks if email addresses are well formed (validator compatible)
  static emailsValidator(control: AbstractControl) {
    if (ValidationService.validEmails(control.value)) {
      return null;
    } else {
      return { invalidEmails: true };
    }
  }

  // checks a list of emails (each email on one line)
  static validEmails(text: string): boolean {
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

  // checks if an email address is well formed (validator compatible)
  static emailValidator(control: AbstractControl) {
    if (emailWellFormed(control.value)) {
      return null;
    } else {
      return { invalidEmail: true };
    }
  }

  static urlValidator(control: AbstractControl) {
    if (
      control.value === undefined ||
      control.value === null ||
      control.value.trim() === ''
    ) {
      return null;
    }

    if (!urlWellFormed(control.value)) {
      return { invalidURL: true };
    } else {
      return null;
    }
  }

  public static nonEmptyTitle(control: AbstractControl) {
    let empty = true;
    if (control && control.value) {
      for (const key of Object.keys(control.value)) {
        if (control.value[key] !== '' && control.value[key] !== null) {
          empty = false;
        }
      }
    }

    if (!empty) {
      return null;
    } else if (empty && control.dirty) {
      return { required: true };
    } else {
      return null;
    }
  }

  public static portValidator(control: AbstractControl) {
    if (control.value === undefined || control.value === '') {
      return null;
    } else if (
      !isNaN(control.value) &&
      Number(control.value) > 0 &&
      Number(control.value) <= 65535
    ) {
      return null;
    } else {
      return { invalidPort: true };
    }
  }

  public static fileFolderExistsValidator(
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
    } else if (
      !contents.map((node: SelectableNode) => node.name).includes(control.value)
    ) {
      return null;
    } else {
      return { fileFolderExists: true };
    }
  }

  public static forbiddenNameRegExpValidator(nameRe: RegExp): ValidatorFn {
    return (control: AbstractControl): { [key: string]: {} } | null => {
      const name = control.value;
      const no = nameRe.test(name);
      return no ? { forbiddenNameRegExp: { name } } : null;
    };
  }

  public static forbiddenNameArrayValidator(names: string[]): ValidatorFn {
    return (control: AbstractControl): { [key: string]: {} } | null => {
      const name = control.value;
      const no = names.includes(name);
      return no ? { forbiddenNameArray: { name } } : null;
    };
  }

  static usernameValidator(
    control: AbstractControl
  ): { [key: string]: any } | null {
    if (control.value === null || control.value === '') {
      return null;
    }

    const minLength = 5;
    const maxLength = 32;

    const regexp = new RegExp('^.{' + minLength + ',' + maxLength + '}$');
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
}
