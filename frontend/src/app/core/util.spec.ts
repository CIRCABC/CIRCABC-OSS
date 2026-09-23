import { ActionType } from 'app/action-result';
import {
  EventItemDefinition,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import {
  arrayDiff,
  changeSort,
  compensateDST,
  convertDate,
  convertDateFormat,
  emailWellFormed,
  eventsStartTimeComparator,
  getErrorTranslation,
  getFormattedDate,
  getFormattedTime,
  getFullDate,
  getRandomColor,
  getSuccessTranslation,
  getUserFullName,
  imageExtensionValid,
  isContentAudio,
  isContentImage,
  isContentPdf,
  isContentPreviewable,
  isContentPreviewableFull,
  isContentVideo,
  padWithLeadingZero,
  removeDuplicates,
  removeNulls,
  sortI18nProperty,
  translateOccurrenceRate,
  truncate,
  urlWellFormed,
} from 'app/core/util';
import { vi } from 'vitest';

describe('util', () => {
  describe('removeNulls', () => {
    it('should remove properties with null values', () => {
      const obj = { a: 1, b: null, c: 'hello', d: null };
      const result = removeNulls(obj);
      expect(result).toEqual({ a: 1, c: 'hello' });
    });

    it('should keep properties with undefined or falsy non-null values', () => {
      const obj = { a: 0, b: '', c: false, d: undefined };
      const result = removeNulls(obj);
      expect(result).toEqual({ a: 0, b: '', c: false, d: undefined });
    });
  });

  describe('arrayDiff', () => {
    it('should return elements in a that are not in b', () => {
      const a = [{ id: 1 }, { id: 2 }, { id: 3 }];
      const b = [{ id: 2 }];
      expect(arrayDiff(a, b, 'id')).toEqual([{ id: 1 }, { id: 3 }]);
    });

    it('should return empty array when all elements match', () => {
      const a = [{ id: 1 }];
      const b = [{ id: 1 }];
      expect(arrayDiff(a, b, 'id')).toEqual([]);
    });

    it('should return all elements when b is empty', () => {
      const a = [{ id: 1 }, { id: 2 }];
      expect(arrayDiff(a, [], 'id')).toEqual(a);
    });
  });

  describe('sortI18nProperty', () => {
    it('should sort by current language', () => {
      const a = { en: 'Banana' };
      const b = { en: 'Apple' };
      expect(sortI18nProperty(a, b, 'en', 'en')).toBeGreaterThan(0);
    });

    it('should return 0 if either is undefined', () => {
      expect(sortI18nProperty(undefined, { en: 'A' })).toBe(0);
      expect(sortI18nProperty({ en: 'A' }, undefined)).toBe(0);
    });

    it('should fall back to default language', () => {
      const a = { fr: 'Zèbre' };
      const b = { fr: 'Abricot' };
      expect(sortI18nProperty(a, b, 'fr', 'en')).toBeGreaterThan(0);
    });

    it('should use aname/bname when strings are empty', () => {
      const a = { en: '' };
      const b = { en: '' };
      expect(
        sortI18nProperty(a, b, 'en', 'en', 'Banana', 'Apple')
      ).toBeGreaterThan(0);
    });
  });

  describe('getFullDate', () => {
    it('should return ISO date string', () => {
      const date = new Date('2024-03-15T10:30:00Z');
      expect(getFullDate(date)).toBe('2024-03-15');
    });

    it('should return empty string for null or undefined', () => {
      expect(getFullDate(null as unknown as Date)).toBe('');
      expect(getFullDate(undefined as unknown as Date)).toBe('');
    });
  });

  describe('changeSort', () => {
    it('should set ASC when new sort is different', () => {
      expect(changeSort('name_ASC', 'date')).toBe('date_ASC');
    });

    it('should toggle to DESC when already ASC', () => {
      expect(changeSort('name_ASC', 'name')).toBe('name_DESC');
    });

    it('should toggle to ASC when already DESC', () => {
      expect(changeSort('name_DESC', 'name')).toBe('name_ASC');
    });
  });

  describe('padWithLeadingZero', () => {
    it('should pad single digit with zero', () => {
      expect(padWithLeadingZero(5)).toBe('05');
    });

    it('should not pad double digit', () => {
      expect(padWithLeadingZero(12)).toBe('12');
    });
  });

  describe('getFormattedDate', () => {
    it('should format date as YYYY-MM-DD', () => {
      const date = new Date(2024, 2, 5); // March 5, 2024
      expect(getFormattedDate(date)).toBe('2024-03-05');
    });
  });

  describe('getFormattedTime', () => {
    it('should format time as HH:MM', () => {
      const date = new Date(2024, 0, 1, 9, 5);
      expect(getFormattedTime(date)).toBe('09:05');
    });
  });

  describe('getSuccessTranslation', () => {
    it('should append .succeed to action type', () => {
      expect(getSuccessTranslation(ActionType.UPLOAD_FILE)).toBe(
        'action.upload-files.succeed'
      );
    });
  });

  describe('getErrorTranslation', () => {
    it('should append .failed to action type', () => {
      expect(getErrorTranslation(ActionType.UPLOAD_FILE)).toBe(
        'action.upload-files.failed'
      );
    });
  });

  describe('isContentPreviewableFull', () => {
    it('should return true for PDF mimetype', () => {
      const node: ModelNode = { properties: { mimetype: 'application/pdf' } };
      expect(isContentPreviewableFull(node)).toBe(true);
    });

    it('should return true for image/png', () => {
      const node: ModelNode = { properties: { mimetype: 'image/png' } };
      expect(isContentPreviewableFull(node)).toBe(true);
    });

    it('should return false for unsupported mimetype', () => {
      const node: ModelNode = { properties: { mimetype: 'application/zip' } };
      expect(isContentPreviewableFull(node)).toBe(false);
    });

    it('should return false when properties is undefined', () => {
      const node: ModelNode = {};
      expect(isContentPreviewableFull(node)).toBe(false);
    });
  });

  describe('isContentPreviewable', () => {
    it('should return true for PDF', () => {
      const node: ModelNode = { properties: { mimetype: 'application/pdf' } };
      expect(isContentPreviewable(node)).toBe(true);
    });

    it('should return true for video content', () => {
      const node: ModelNode = {
        name: 'video.mp4',
        properties: { mimetype: 'video/mp4' },
      };
      expect(isContentPreviewable(node)).toBe(true);
    });

    it('should return true for audio content', () => {
      const node: ModelNode = {
        name: 'audio.mp3',
        properties: { mimetype: 'audio/mpeg' },
      };
      expect(isContentPreviewable(node)).toBe(true);
    });

    it('should return false for unsupported type', () => {
      const node: ModelNode = { properties: { mimetype: 'text/plain' } };
      expect(isContentPreviewable(node)).toBe(false);
    });
  });

  describe('isContentImage', () => {
    it('should return true for image/jpeg', () => {
      const node: ModelNode = { properties: { mimetype: 'image/jpeg' } };
      expect(isContentImage(node)).toBe(true);
    });

    it('should return false for non-image', () => {
      const node: ModelNode = { properties: { mimetype: 'text/plain' } };
      expect(isContentImage(node)).toBe(false);
    });
  });

  describe('isContentVideo', () => {
    it('should return true for video mimetype', () => {
      const node: ModelNode = {
        name: 'file.mp4',
        properties: { mimetype: 'video/mp4' },
      };
      expect(isContentVideo(node)).toBe(true);
    });

    it('should return false when name is undefined', () => {
      const node: ModelNode = { properties: { mimetype: 'video/mp4' } };
      expect(isContentVideo(node)).toBe(false);
    });
  });

  describe('isContentAudio', () => {
    it('should return true for audio mimetype', () => {
      const node: ModelNode = {
        name: 'file.mp3',
        properties: { mimetype: 'audio/mpeg' },
      };
      expect(isContentAudio(node)).toBe(true);
    });

    it('should return false for non-audio', () => {
      const node: ModelNode = {
        name: 'file.txt',
        properties: { mimetype: 'text/plain' },
      };
      expect(isContentAudio(node)).toBe(false);
    });
  });

  describe('isContentPdf', () => {
    it('should return true for application/pdf', () => {
      const node: ModelNode = { properties: { mimetype: 'application/pdf' } };
      expect(isContentPdf(node)).toBe(true);
    });

    it('should return false for non-pdf', () => {
      const node: ModelNode = { properties: { mimetype: 'image/png' } };
      expect(isContentPdf(node)).toBe(false);
    });
  });

  describe('translateOccurrenceRate', () => {
    it('should return ["na"] for undefined', () => {
      expect(translateOccurrenceRate(undefined)).toEqual(['na']);
    });

    it('should return ["na"] for empty string', () => {
      expect(translateOccurrenceRate('')).toEqual(['na']);
    });

    it('should return ["once"] for OnlyOnce pattern', () => {
      expect(translateOccurrenceRate('OnlyOnce|something')).toEqual(['once']);
    });

    it('should parse Times pattern', () => {
      expect(translateOccurrenceRate('Times|Monday|null|5|2024')).toEqual([
        'times',
        'Monday',
        '5',
      ]);
    });

    it('should parse EveryTimes pattern', () => {
      expect(translateOccurrenceRate('EveryTimes|null|Weekly|3|10')).toEqual([
        'everytimes',
        'Weekly',
        '3',
        '10',
      ]);
    });
  });

  describe('getRandomColor', () => {
    it('should return a valid hex color string', () => {
      const color = getRandomColor();
      expect(color).toMatch(/^#[0-9A-F]{6}$/);
    });
  });

  describe('eventsStartTimeComparator', () => {
    it('should return 0 when both startTimes are undefined', () => {
      const e1: EventItemDefinition = {};
      const e2: EventItemDefinition = {};
      expect(eventsStartTimeComparator(e1, e2)).toBe(0);
    });

    it('should return -1 when first startTime is undefined', () => {
      const e1: EventItemDefinition = {};
      const e2: EventItemDefinition = { startTime: '10:00' };
      expect(eventsStartTimeComparator(e1, e2)).toBe(-1);
    });

    it('should return 1 when second startTime is undefined', () => {
      const e1: EventItemDefinition = { startTime: '10:00' };
      const e2: EventItemDefinition = {};
      expect(eventsStartTimeComparator(e1, e2)).toBe(1);
    });

    it('should return 1 when first is later', () => {
      const e1: EventItemDefinition = { startTime: '14:30' };
      const e2: EventItemDefinition = { startTime: '09:00' };
      expect(eventsStartTimeComparator(e1, e2)).toBe(1);
    });

    it('should return -1 when first is earlier', () => {
      const e1: EventItemDefinition = { startTime: '08:00' };
      const e2: EventItemDefinition = { startTime: '16:00' };
      expect(eventsStartTimeComparator(e1, e2)).toBe(-1);
    });

    it('should return 0 when times are equal', () => {
      const e1: EventItemDefinition = { startTime: '10:00' };
      const e2: EventItemDefinition = { startTime: '10:00' };
      expect(eventsStartTimeComparator(e1, e2)).toBe(0);
    });
  });

  describe('truncate', () => {
    it('should truncate long text with ellipsis', () => {
      expect(truncate('Hello World!', 8)).toBe('Hello...');
    });

    it('should not truncate short text', () => {
      expect(truncate('Hi', 10)).toBe('Hi');
    });
  });

  describe('emailWellFormed', () => {
    it('should return true for valid email', () => {
      expect(emailWellFormed('user@example.com')).toBe(true);
    });

    it('should return false for invalid email', () => {
      expect(emailWellFormed('not-an-email')).toBe(false);
    });

    it('should return false for empty string', () => {
      expect(emailWellFormed('')).toBe(false);
    });
  });

  describe('urlWellFormed', () => {
    it('should return true for valid URL', () => {
      expect(urlWellFormed('https://example.com')).toBe(true);
    });

    it('should return false for invalid URL', () => {
      expect(urlWellFormed('not a url')).toBe(false);
    });
  });

  describe('imageExtensionValid', () => {
    it('should return true for jpg', () => {
      expect(imageExtensionValid('photo.jpg')).toBe(true);
    });

    it('should return true for png (case insensitive)', () => {
      expect(imageExtensionValid('IMAGE.PNG')).toBe(true);
    });

    it('should return true for gif', () => {
      expect(imageExtensionValid('anim.gif')).toBe(true);
    });

    it('should return false for non-image extension', () => {
      expect(imageExtensionValid('doc.pdf')).toBe(false);
    });

    it('should return false for undefined', () => {
      expect(imageExtensionValid(undefined as unknown as string)).toBe(false);
    });
  });

  describe('getUserFullName', () => {
    it('should return "System" for System user', async () => {
      const mockUserService = { getUserAsync: vi.fn() };
      const result = await getUserFullName(
        'System',
        mockUserService as unknown as import('app/core/generated/circabc').UserService
      );
      expect(result).toBe('System');
      expect(mockUserService.getUserAsync).not.toHaveBeenCalled();
    });

    it('should return full name from user service', async () => {
      const mockUserService = {
        getUserAsync: vi
          .fn()
          .mockResolvedValue({ firstname: 'John', lastname: 'Doe' }),
      };
      const result = await getUserFullName(
        'jdoe',
        mockUserService as unknown as import('app/core/generated/circabc').UserService
      );
      expect(result).toBe('John Doe');
    });

    it('should return userId on error', async () => {
      const mockUserService = {
        getUserAsync: vi.fn().mockRejectedValue(new Error('Not found')),
      };
      const result = await getUserFullName(
        'unknown',
        mockUserService as unknown as import('app/core/generated/circabc').UserService
      );
      expect(result).toBe('unknown');
    });
  });

  describe('removeDuplicates', () => {
    it('should remove duplicate items based on comparator', () => {
      const items = [{ id: 1 }, { id: 2 }, { id: 1 }, { id: 3 }];
      const result = removeDuplicates(items, (a, b) => a.id === b.id);
      expect(result).toEqual([{ id: 1 }, { id: 2 }, { id: 3 }]);
    });

    it('should return empty array for empty input', () => {
      const result = removeDuplicates([], (a, b) => a === b);
      expect(result).toEqual([]);
    });
  });

  describe('convertDate', () => {
    it('should convert date string to YYYY-MM-DD format', () => {
      expect(convertDate('Mon Mar 15 2024 10:30:00')).toBe('2024-03-15');
    });
  });

  describe('compensateDST', () => {
    it('should return a Date object', () => {
      const date = new Date(2024, 6, 15);
      const result = compensateDST(date);
      expect(result).toBeInstanceOf(Date);
    });
  });

  describe('convertDateFormat', () => {
    it('should convert DD/MM/YYYY to YYYY-MM-DD', () => {
      expect(convertDateFormat('15/03/2024')).toBe('2024-03-15');
    });

    it('should return null for null input', () => {
      expect(convertDateFormat(null)).toBeNull();
    });
  });
});
