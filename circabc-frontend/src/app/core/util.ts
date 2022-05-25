import { ActionType } from 'app/action-result';
import {
  EventItemDefinition,
  Node as ModelNode,
  UserService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

/**
 * remove  properties with null values from object
 * example
 * @param obj An object that we want to remove null values
 */

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const removeNulls = (obj: any) => {
  Object.keys(obj).forEach((key) => obj[key] === null && delete obj[key]);
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
  let c: T[];
  // eslint-disable-next-line prefer-const
  c = a.filter((aitem: T) => {
    return b.findIndex((bitem: T) => aitem[key] === bitem[key]) === -1;
  });
  return c;
}

// to see if we can use collator
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Collator/compare
export function sortI18nProperty(
  a: { [key: string]: string } | undefined,
  b: { [key: string]: string } | undefined,
  currentLang = 'en',
  defaultLang = 'en',
  aname = '',
  bname = ''
): number {
  if (a === undefined) {
    return 0;
  }
  if (b === undefined) {
    return 0;
  }
  let astring = '';
  let bstring = '';

  if (a[currentLang]) {
    astring = a[currentLang];
  } else if (a[defaultLang]) {
    astring = a[defaultLang];
  } else {
    // eslint-disable-next-line no-restricted-syntax
    for (const key in a) {
      if (Object.prototype.hasOwnProperty.call(a, key)) {
        astring = a[key];
        break;
      }
    }
  }

  if (b[currentLang]) {
    bstring = b[currentLang];
  } else if (b[defaultLang]) {
    bstring = b[defaultLang];
  } else {
    // eslint-disable-next-line no-restricted-syntax
    for (const key in b) {
      if (Object.prototype.hasOwnProperty.call(b, key)) {
        bstring = b[key];
        break;
      }
    }
  }
  if (astring.trim() === '') {
    astring = aname;
  }
  if (bstring.trim() === '') {
    bstring = bname;
  }
  return astring.localeCompare(bstring, currentLang);
}
export function getFullDate(date: Date): string {
  if (date === undefined || date === null) {
    return '';
  }
  return date.toISOString().slice(0, 10);
}

export function changeSort(oldSort: string, newSort: string) {
  let result = '';
  if (oldSort.indexOf(newSort) === -1) {
    result = `${newSort}_ASC`;
  } else if (oldSort === `${newSort}_ASC`) {
    result = `${newSort}_DESC`;
  } else if (oldSort === `${newSort}_DESC`) {
    result = `${newSort}_ASC`;
  }
  return result;
}

// pad values with '0' to conform to the date and time formats
export function padWithLeadingZero(value: number): string {
  // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
  // eslint-disable-next-line prefer-template
  const valueString: string = value.toString();
  // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
  // eslint-disable-next-line prefer-template
  return valueString.length === 1 ? '0' + valueString : valueString;
}

export function getFormattedDate(date: Date) {
  return `${date.getFullYear()}-${padWithLeadingZero(
    date.getMonth() + 1
  )}-${padWithLeadingZero(date.getDate())}`;
}

export function getFormattedTime(date: Date) {
  return `${padWithLeadingZero(date.getHours())}:${padWithLeadingZero(
    date.getMinutes()
  )}`;
}

export function getSuccessTranslation(actionType: ActionType): string {
  return `${actionType}.succeed`;
}

export function getErrorTranslation(actionType: ActionType): string {
  return `${actionType}.failed`;
}

/**
 * detect IE
 * returns the version of IE or 0, if the browser is not Internet Explorer
 */
export function detectIEVersion(): number {
  const userAgent: string = window.navigator.userAgent;

  // Test values; Uncomment to check result …

  // IE 10
  // ua = 'Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)';

  // IE 11
  // ua = 'Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko';

  // IE 12 / Spartan
  // ua = 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0';

  // Edge (IE 12+)
  // ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586';

  const msie: number = userAgent.indexOf('MSIE ');
  if (msie > 0) {
    // IE 10 or older => return version number
    return parseInt(
      userAgent.substring(msie + 5, userAgent.indexOf('.', msie)),
      10
    );
  }

  const trident: number = userAgent.indexOf('Trident/');
  if (trident > 0) {
    // IE 11 => return version number
    const rv = userAgent.indexOf('rv:');
    return parseInt(
      userAgent.substring(rv + 3, userAgent.indexOf('.', rv)),
      10
    );
  }

  const edge: number = userAgent.indexOf('Edge/');
  if (edge > 0) {
    // Edge (IE 12+) => return version number
    return parseInt(
      userAgent.substring(edge + 5, userAgent.indexOf('.', edge)),
      10
    );
  }

  // other browser
  return 0;
}

/**
 * Returns the OS name as follows:
 *
 * "Windows"    for all versions of Windows
 * "MacOS"      for all versions of Macintosh OS
 * "Linux"      for all versions of Linux
 * "Unix"       for all other UNIX flavors
 * ""           indicates failure to detect the OS
 */
export interface OSInfo {
  name: string;
  version: number;
}
export function detectOS(): OSInfo {
  if (navigator.appVersion.indexOf('Windows NT 6.1') !== -1) {
    return { name: 'Windows', version: 7 };
  }
  if (navigator.appVersion.indexOf('Windows NT 6.2') !== -1) {
    return { name: 'Windows', version: 8 };
  }
  if (navigator.appVersion.indexOf('Windows NT 10.0') !== -1) {
    return { name: 'Windows', version: 10 };
  }
  if (navigator.appVersion.indexOf('Mac') !== -1) {
    return { name: 'MacOS', version: 0 };
  }
  if (navigator.appVersion.indexOf('X11') !== -1) {
    return { name: 'Unix', version: 0 };
  }
  if (navigator.appVersion.indexOf('Linux') !== -1) {
    return { name: 'Linux', version: 0 };
  }

  return { name: '', version: -1 };
}
/*
export function isContentPreviewable(content: ModelNode): boolean {
  return (
    content !== undefined &&
    content.properties !== undefined &&
    content.properties.mimetype !== undefined &&
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
    ].indexOf(content.properties.mimetype.toLowerCase()) !== -1
  );
*/

export function isContentPreviewable(content: ModelNode): boolean {
  let result =
    content !== undefined &&
    content.properties !== undefined &&
    content.properties.mimetype !== undefined &&
    ['application/pdf', 'image/gif', 'image/jpeg', 'image/png'].indexOf(
      content.properties.mimetype.toLowerCase()
    ) !== -1;
  result = result || isContentAudio(content) || isContentVideo(content);
  return result;
}

export function isContentImage(content: ModelNode): boolean {
  return (
    content !== undefined &&
    content.properties !== undefined &&
    content.properties.mimetype !== undefined &&
    ['image/gif', 'image/png', 'image/jpeg'].includes(
      content.properties.mimetype.toLowerCase()
    )
  );
}

export function isContentVideo(content: ModelNode): boolean {
  return (
    content !== undefined &&
    content.name !== undefined &&
    content.properties !== undefined &&
    content.properties.mimetype !== undefined &&
    content.properties.mimetype.toLowerCase().indexOf('video') !== -1
  );
}

export function isContentAudio(content: ModelNode): boolean {
  return (
    content !== undefined &&
    content.name !== undefined &&
    content.properties !== undefined &&
    content.properties.mimetype !== undefined &&
    content.properties.mimetype.toLowerCase().indexOf('audio') !== -1
  );
}

export function translateOccurrenceRate(
  occurrenceRate: string | undefined
): string[] {
  const result = [];
  if (occurrenceRate === undefined || occurrenceRate === '') {
    result.push('na');
  } else if (occurrenceRate.startsWith('OnlyOnce|')) {
    result.push('once');
  } else if (occurrenceRate.startsWith('Times|')) {
    const match = occurrenceRate.match(/Times\|(.*)\|null\|(\d+)\|[0-9999]/);
    if (match !== null) {
      const when = match[1];
      const times = match[2];
      result.push('times');
      result.push(when);
      result.push(times);
    }
  } else if (occurrenceRate.startsWith('EveryTimes|')) {
    const match = occurrenceRate.match(/EveryTimes\|null\|(.*)\|(\d+)\|(\d+)/);
    if (match !== null) {
      const when = match[1];
      const times = match[2];
      const repeat = match[3];
      result.push('everytimes');
      result.push(when);
      result.push(times);
      result.push(repeat);
    }
  }
  return result;
}

export function getRandomColor(): string {
  const hexNibbles = '0123456789ABCDEF';
  let color = '#';
  for (let idx = 0; idx < 6; idx += 1) {
    // eslint-disable-next-line
    color += hexNibbles[Math.floor(Math.random() * 16)];
  }
  return color;
}

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
  } else if (event1StartTime < event2StartTime) {
    return -1;
  }
  return 0;
}

export function truncate(text: string, length: number): string {
  if (text.length > length) {
    return `${text.substring(0, (length === undefined ? 10 : length) - 3)}...`;
  } else {
    return text;
  }
}

// check if an email address is well formed
export function emailWellFormed(email: string): boolean {
  // eslint-disable-next-line max-len
  const emailExpression =
    /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return emailExpression.test(email);
}

// check if an url is well formed
export function urlWellFormed(email: string): boolean {
  // eslint-disable-next-line max-len
  const expression =
    /(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)(?:\([-A-Z0-9+&@#\/%=~_|$?!:,.]*\)|[-A-Z0-9+&@#\/%=~_|$?!:,.])*(?:\([-A-Z0-9+&@#\/%=~_|$?!:,.]*\)|[A-Z0-9+&@#\/%=~_|$])/i;
  return expression.test(email);
}

// check if a string has extension of image
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

export async function getUserFullName(
  userId: string,
  userService: UserService
) {
  let fullName = '';
  if (userId !== 'System') {
    try {
      const user = await firstValueFrom(userService.getUser(userId));
      fullName = `${user.firstname} ${user.lastname}`;
    } catch (error) {
      console.error(
        `Error getting info about user '${userId}'. User card will not be displayed.`
      );
      console.error(error);
      fullName = userId;
    }
  } else {
    fullName = userId;
  }
  return fullName;
}

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
