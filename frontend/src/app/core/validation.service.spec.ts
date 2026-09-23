import { FormControl, FormGroup } from '@angular/forms';
import { SelectableNode } from 'app/core/ui-model';

import {
  attendantsValidator,
  dateLessThan,
  emailsValidator,
  emailValidator,
  fileFolderExistsValidator,
  fileNameValidator,
  forbiddenNameArrayValidator,
  forbiddenNameRegExpValidator,
  futureDateValidator,
  getErrorMessageTranslationCode,
  maxLengthTitleValidator,
  nameValidator,
  nonEmptyTitle,
  passwordValidator,
  pastDateValidator,
  phoneValidator,
  portValidator,
  timeStringOK,
  titleValidator,
  urlValidator,
  usernameValidator,
  validEmails,
} from 'app/core/validation.service';

describe('ValidationService', () => {
  describe('getErrorMessageTranslationCode', () => {
    it('should return prefixed translation code', () => {
      expect(getErrorMessageTranslationCode('required')).toBe(
        'validation.required'
      );
    });
  });

  describe('passwordValidator', () => {
    it('should return null for valid password', () => {
      const control = new FormControl('Abc123');
      expect(passwordValidator(control)).toBeNull();
    });

    it.each([
      ['null value', null],
      ['password without digit', 'Abcdef'],
      ['password shorter than 6 chars', 'Ab1'],
      ['password with spaces', 'Ab 123'],
    ])('should return error for %s', (_, value) => {
      const control = new FormControl(value);
      expect(passwordValidator(control)).toEqual({ invalidPassword: true });
    });
  });

  describe('fileNameValidator', () => {
    it('should return null for valid filename', () => {
      const control = new FormControl('document.pdf');
      expect(fileNameValidator(control)).toBeNull();
    });

    it.each([
      ['filename with forbidden characters', 'file*name'],
      ['filename ending with dot', 'file.'],
      ['filename ending with space', 'file '],
    ])('should return error for %s', (_, value) => {
      const control = new FormControl(value);
      expect(fileNameValidator(control)).toEqual({
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      });
    });
  });

  describe('nameValidator', () => {
    it('should return null for valid name', () => {
      const control = new FormControl('validName');
      expect(nameValidator(control)).toBeNull();
    });

    it('should return null for null value', () => {
      const control = new FormControl(null);
      expect(nameValidator(control)).toBeNull();
    });

    it('should return error for empty string', () => {
      const control = new FormControl('   ');
      expect(nameValidator(control)).toEqual({
        invalidFileName: { additionalInfo: 'empty name' },
      });
    });

    it('should return null for undefined (coerced to null by FormControl)', () => {
      const control = new FormControl(undefined);
      expect(nameValidator(control)).toBeNull();
    });

    it('should return error for forbidden characters', () => {
      const control = new FormControl('name<bad');
      expect(nameValidator(control)).toEqual({
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      });
    });
  });

  describe('titleValidator', () => {
    it('should return null for null value', () => {
      const control = new FormControl(null);
      expect(titleValidator(control)).toBeNull();
    });

    it('should return null for valid title', () => {
      const control = new FormControl({ en: 'Valid Title' });
      expect(titleValidator(control)).toBeNull();
    });

    it('should return error for empty english title', () => {
      const control = new FormControl({ en: '   ' });
      expect(titleValidator(control)).toEqual({
        invalidFileName: { additionalInfo: 'empty title' },
      });
    });

    it('should return error for title with forbidden characters', () => {
      const control = new FormControl({ en: 'title*bad' });
      expect(titleValidator(control)).toEqual({
        invalidTitle: {
          additionalInfo: String.raw` " * \ < > ? / : | . space`,
        },
      });
    });
  });

  describe('maxLengthTitleValidator', () => {
    it('should return null for null value', () => {
      const control = new FormControl(null);
      expect(maxLengthTitleValidator(control, 10)).toBeNull();
    });

    it('should return null for title within length', () => {
      const control = new FormControl({ en: 'short' });
      expect(maxLengthTitleValidator(control, 10)).toBeNull();
    });

    it('should return error for title exceeding length', () => {
      const control = new FormControl({ en: 'this is too long' });
      expect(maxLengthTitleValidator(control, 5)).toEqual({
        invalidLengthTitle: { maxLength: 5 },
      });
    });
  });

  describe('dateLessThan', () => {
    it('should return empty object when from < to', () => {
      const group = new FormGroup({
        from: new FormControl(new Date(2020, 0, 1)),
        to: new FormControl(new Date(2020, 0, 2)),
      });
      const validator = dateLessThan('from', 'to');
      expect(validator(group)).toEqual({});
    });

    it('should return error when from > to', () => {
      const group = new FormGroup({
        from: new FormControl(new Date(2020, 5, 15)),
        to: new FormControl(new Date(2020, 0, 1)),
      });
      const validator = dateLessThan('from', 'to');
      const result = validator(group);
      expect(result['invalidDateRange']).toBeDefined();
    });
  });

  describe('pastDateValidator', () => {
    it('should return null for future date', () => {
      const future = new Date();
      future.setFullYear(future.getFullYear() + 1);
      const control = new FormControl(future.toISOString());
      expect(pastDateValidator(control)).toBeNull();
    });

    it('should return error for past date', () => {
      const control = new FormControl('2000-01-01');
      expect(pastDateValidator(control)).toEqual({ pastDate: true });
    });
  });

  describe('futureDateValidator', () => {
    it('should return null for past date', () => {
      const control = new FormControl('2000-01-01');
      expect(futureDateValidator(control)).toBeNull();
    });

    it('should return error for future date', () => {
      const future = new Date();
      future.setFullYear(future.getFullYear() + 10);
      const control = new FormControl(future.toISOString());
      expect(futureDateValidator(control)).toEqual({ futureDate: true });
    });
  });

  describe('timeStringOK', () => {
    it('should return true for valid time', () => {
      expect(timeStringOK('09:30')).toBe(true);
      expect(timeStringOK('23:59')).toBe(true);
      expect(timeStringOK('0:00')).toBe(true);
    });

    it('should return false for invalid time', () => {
      expect(timeStringOK('25:00')).toBe(false);
      expect(timeStringOK('12:60')).toBe(false);
      expect(timeStringOK(undefined as unknown as string)).toBe(false);
    });
  });

  describe('phoneValidator', () => {
    it('should return null for empty value', () => {
      const control = new FormControl('');
      expect(phoneValidator(control)).toBeNull();
    });

    it('should return null for valid phone number', () => {
      const control = new FormControl('123456789');
      expect(phoneValidator(control)).toBeNull();
    });

    it('should return error for non-numeric value', () => {
      const control = new FormControl('abc');
      expect(phoneValidator(control)).toEqual({ invalidPhone: true });
    });

    it('should return error for decimal value', () => {
      const control = new FormControl('12.5');
      expect(phoneValidator(control)).toEqual({ invalidPhone: true });
    });
  });

  describe('emailValidator', () => {
    it('should return null for valid email', () => {
      const control = new FormControl('user@example.com');
      expect(emailValidator(control)).toBeNull();
    });

    it('should return error for invalid email', () => {
      const control = new FormControl('not-an-email');
      expect(emailValidator(control)).toEqual({ invalidEmail: true });
    });
  });

  describe('emailsValidator', () => {
    it('should return null for valid emails', () => {
      const control = new FormControl('a@b.com\nc@d.com');
      expect(emailsValidator(control)).toBeNull();
    });

    it('should return error for invalid emails', () => {
      const control = new FormControl('a@b.com\ninvalid');
      expect(emailsValidator(control)).toEqual({ invalidEmails: true });
    });
  });

  describe('validEmails', () => {
    it('should return true for null or undefined', () => {
      expect(validEmails(null as unknown as string)).toBe(true);
      expect(validEmails(undefined as unknown as string)).toBe(true);
    });

    it('should return true for valid email list', () => {
      expect(validEmails('a@b.com\nc@d.com')).toBe(true);
    });

    it('should return false if any email is invalid', () => {
      expect(validEmails('a@b.com\nbad')).toBe(false);
    });

    it('should skip empty lines', () => {
      expect(validEmails('a@b.com\n\nc@d.com')).toBe(true);
    });
  });

  describe('urlValidator', () => {
    it('should return null for empty value', () => {
      const control = new FormControl('');
      expect(urlValidator(control)).toBeNull();
    });

    it('should return null for null value', () => {
      const control = new FormControl(null);
      expect(urlValidator(control)).toBeNull();
    });

    it('should return null for valid URL', () => {
      const control = new FormControl('https://example.com');
      expect(urlValidator(control)).toBeNull();
    });

    it('should return error for invalid URL', () => {
      const control = new FormControl('not a url');
      expect(urlValidator(control)).toEqual({ invalidURL: true });
    });
  });

  describe('nonEmptyTitle', () => {
    it('should return null when at least one language has value', () => {
      const control = new FormControl({ en: 'Hello', fr: '' });
      expect(nonEmptyTitle(control)).toBeNull();
    });

    it('should return error when all values are empty and control is dirty', () => {
      const control = new FormControl({ en: '', fr: '' });
      control.markAsDirty();
      expect(nonEmptyTitle(control)).toEqual({ required: true });
    });

    it('should return null when all values are empty but control is pristine', () => {
      const control = new FormControl({ en: '', fr: '' });
      expect(nonEmptyTitle(control)).toBeNull();
    });
  });

  describe('portValidator', () => {
    it('should return null for empty value', () => {
      const control = new FormControl('');
      expect(portValidator(control)).toBeNull();
    });

    it('should return null for valid port', () => {
      const control = new FormControl('8080');
      expect(portValidator(control)).toBeNull();
    });

    it.each([
      ['port > 65535', '70000'],
      ['port 0', '0'],
      ['non-numeric value', 'abc'],
    ])('should return error for %s', (_, value) => {
      const control = new FormControl(value);
      expect(portValidator(control)).toEqual({ invalidPort: true });
    });
  });

  describe('fileFolderExistsValidator', () => {
    const contents: SelectableNode[] = [
      { name: 'existing-file' } as SelectableNode,
      { name: 'another-file' } as SelectableNode,
    ];

    it('should return null for empty value', () => {
      const control = new FormControl('');
      expect(fileFolderExistsValidator(control, contents)).toBeNull();
    });

    it('should return null when name does not exist', () => {
      const control = new FormControl('new-file');
      expect(fileFolderExistsValidator(control, contents)).toBeNull();
    });

    it('should return error when name already exists', () => {
      const control = new FormControl('existing-file');
      expect(fileFolderExistsValidator(control, contents)).toEqual({
        fileFolderExists: true,
      });
    });

    it('should return null for empty contents', () => {
      const control = new FormControl('anything');
      expect(fileFolderExistsValidator(control, [])).toBeNull();
    });
  });

  describe('forbiddenNameRegExpValidator', () => {
    it('should return null when name does not match regex', () => {
      const validator = forbiddenNameRegExpValidator(/^admin$/);
      const control = new FormControl('user');
      expect(validator(control)).toBeNull();
    });

    it('should return error when name matches regex', () => {
      const validator = forbiddenNameRegExpValidator(/^admin$/);
      const control = new FormControl('admin');
      expect(validator(control)).toEqual({
        forbiddenNameRegExp: { name: 'admin' },
      });
    });
  });

  describe('forbiddenNameArrayValidator', () => {
    it('should return null when name is not in array', () => {
      const validator = forbiddenNameArrayValidator(['admin', 'root']);
      const control = new FormControl('user');
      expect(validator(control)).toBeNull();
    });

    it('should return error when name is in array', () => {
      const validator = forbiddenNameArrayValidator(['admin', 'root']);
      const control = new FormControl('admin');
      expect(validator(control)).toEqual({
        forbiddenNameArray: { name: 'admin' },
      });
    });
  });

  describe('usernameValidator', () => {
    it('should return null for null value', () => {
      const control = new FormControl(null);
      expect(usernameValidator(control)).toBeNull();
    });

    it('should return null for empty value', () => {
      const control = new FormControl('');
      expect(usernameValidator(control)).toBeNull();
    });

    it('should return null for valid username', () => {
      const control = new FormControl('user123');
      expect(usernameValidator(control)).toBeNull();
    });

    it('should return error for username shorter than 5 chars', () => {
      const control = new FormControl('ab1');
      expect(usernameValidator(control)).toEqual({
        invalidUsernameLength: { maxLength: 32, minLength: 5 },
      });
    });

    it('should return error for username longer than 32 chars', () => {
      const control = new FormControl('a'.repeat(33));
      expect(usernameValidator(control)).toEqual({
        invalidUsernameLength: { maxLength: 32, minLength: 5 },
      });
    });

    it('should return error for username with special characters', () => {
      const control = new FormControl('user@name');
      expect(usernameValidator(control)).toEqual({
        invalidUsernameCharacter: true,
      });
    });
  });

  describe('attendantsValidator', () => {
    it('should return null when audience is open', () => {
      const control = new FormControl({
        audienceStatusOpen: true,
        invitedUsersOrProfiles: [],
        invitedExternalEmails: '',
      });
      expect(attendantsValidator(control)).toBeNull();
    });

    it('should return null when closed with invited users', () => {
      const control = new FormControl({
        audienceStatusOpen: false,
        invitedUsersOrProfiles: ['user1'],
        invitedExternalEmails: '',
      });
      expect(attendantsValidator(control)).toBeNull();
    });

    it('should return null when closed with valid external emails', () => {
      const control = new FormControl({
        audienceStatusOpen: false,
        invitedUsersOrProfiles: [],
        invitedExternalEmails: 'a@b.com',
      });
      expect(attendantsValidator(control)).toBeNull();
    });

    it('should return error when closed with no attendants', () => {
      const control = new FormControl({
        audienceStatusOpen: false,
        invitedUsersOrProfiles: [],
        invitedExternalEmails: '',
      });
      expect(attendantsValidator(control)).toEqual({
        attendantsMissing: true,
      });
    });
  });
});
