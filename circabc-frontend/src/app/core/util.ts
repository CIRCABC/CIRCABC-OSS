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
  const c: T[] = a.filter((aitem: T) => {
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
  return valueString.length === 1 ? `0${valueString}` : valueString;
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

export function isContentPreviewableFull(content: ModelNode): boolean {
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
      'text/html',
      'image/gif',
      'image/jpeg',
      'image/png',
    ].indexOf(content.properties.mimetype.toLowerCase()) !== -1
  );
}

/**
 * Preview support for the standard (OSS) deployment where `useAlfrescoAPI` is
 * false. Only formats that are actually rendered are listed here, so the
 * preview button never shows up for unsupported formats (e.g. legacy .doc,
 * PowerPoint, OpenDocument, RTF, Visio), which would otherwise fail because no
 * server-side converter (LibreOffice) is available.
 *
 * Supported: PDF, images, audio, video, plain text/XML, HTML, CSV, Markdown,
 * Word (.docx) and Excel (.xls/.xlsx).
 */
export function isContentPreviewableOss(content: ModelNode): boolean {
  return (
    isContentPdf(content) ||
    isContentImage(content) ||
    isContentAudio(content) ||
    isContentVideo(content) ||
    isContentText(content) ||
    isContentHtml(content) ||
    isContentCsv(content) ||
    isContentMarkdown(content) ||
    isContentSvg(content) ||
    isContentWord(content) ||
    isContentExcel(content) ||
    isContentTiff(content) ||
    isContentRtf(content) ||
    isContentPptx(content) ||
    isContentWebp(content)
  );
}

export function isContentPreviewable(content: ModelNode): boolean {
  let result =
    content?.properties?.mimetype !== undefined &&
    ['application/pdf', 'image/gif', 'image/jpeg', 'image/png'].indexOf(
      content.properties.mimetype.toLowerCase()
    ) !== -1;
  result =
    result ||
    isContentAudio(content) ||
    isContentVideo(content) ||
    isContentText(content) ||
    isContentHtml(content) ||
    isContentCsv(content) ||
    isContentMarkdown(content) ||
    isContentSvg(content) ||
    isContentWord(content) ||
    isContentExcel(content) ||
    isContentTiff(content) ||
    isContentRtf(content) ||
    isContentPptx(content) ||
    isContentWebp(content);
  return result;
}

/** Returns true when the node file name ends with one of the given extensions. */
function nodeNameEndsWith(content: ModelNode, extensions: string[]): boolean {
  const name = content?.name?.toLowerCase();
  return name !== undefined && extensions.some((ext) => name.endsWith(ext));
}

/** Returns true when the node file name exactly matches one of the given names. */
function nodeNameIs(content: ModelNode, names: string[]): boolean {
  const name = content?.name?.toLowerCase();
  return name !== undefined && names.includes(name);
}

export function isContentText(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    (mimetype !== undefined &&
      [
        'text/plain',
        'text/xml',
        'application/xml',
        'text/css',
        'application/x-javascript',
        'application/javascript',
        'application/json',
        'application/xhtml+xml',
        'text/calendar',
        'application/rss+xml',
        'text/mediawiki',
        'text/sgml',
        'text/richtext',
        'application/x-latex',
        'application/x-tex',
        'application/x-sh',
        'application/x-csh',
        'application/x-tcl',
      ].includes(mimetype)) ||
    nodeNameEndsWith(content, [
      // Plain text / config / data
      '.txt',
      '.log',
      '.ini',
      '.properties',
      '.conf',
      '.config',
      '.cfg',
      '.env',
      '.yaml',
      '.yml',
      '.toml',
      '.gitignore',
      '.mk',
      '.mak',
      '.diff',
      '.patch',
      '.rst',
      '.adoc',
      // Web / markup / styles
      '.css',
      '.scss',
      '.sass',
      '.less',
      '.styl',
      '.js',
      '.mjs',
      '.jsx',
      '.ts',
      '.tsx',
      '.vue',
      '.svelte',
      '.json',
      '.xhtml',
      '.graphql',
      '.gql',
      '.proto',
      // Existing / misc text formats
      '.ics',
      '.rss',
      '.mw',
      '.sgml',
      '.sgm',
      '.rtx',
      '.latex',
      '.tex',
      '.sh',
      '.bat',
      '.cmd',
      '.csh',
      '.awk',
      '.sed',
      '.tcl',
      // C / C++ / C#
      '.c',
      '.h',
      '.cpp',
      '.cc',
      '.cxx',
      '.hpp',
      '.hh',
      '.hxx',
      '.ino',
      '.cs',
      // Objective-C
      '.m',
      '.mm',
      // Python / Ruby / Go / Rust / PHP / Perl
      '.py',
      '.pyw',
      '.pyi',
      '.rb',
      '.go',
      '.rs',
      '.php',
      '.php3',
      '.phtml',
      '.pl',
      '.pm',
      // Pascal / Delphi
      '.pas',
      '.pp',
      '.dpr',
      '.dfm',
      '.inc',
      // Fortran
      '.f',
      '.for',
      '.f90',
      '.f95',
      // Visual Basic / PowerShell
      '.vb',
      '.vbs',
      '.bas',
      '.ps1',
      '.psm1',
      '.psd1',
      // JVM / mobile
      '.kt',
      '.kts',
      '.swift',
      '.scala',
      '.sc',
      '.groovy',
      '.gradle',
      // Scripting / functional / others
      '.lua',
      '.dart',
      '.jl',
      '.r',
      '.hs',
      '.erl',
      '.ex',
      '.exs',
      '.clj',
      '.cljs',
      '.ml',
      '.mli',
      // Assembly
      '.asm',
      '.s',
      '.nasm',
    ]) ||
    // Common extension-less build/config files
    nodeNameIs(content, [
      'makefile',
      'gnumakefile',
      'dockerfile',
      'containerfile',
      'jenkinsfile',
      'vagrantfile',
      'gemfile',
      'rakefile',
      'procfile',
    ])
  );
}

export function isContentHtml(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    ['text/html'].includes(content.properties.mimetype.toLowerCase())
  );
}

export function isContentCsv(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype === 'text/csv' ||
    mimetype === 'text/tab-separated-values' ||
    nodeNameEndsWith(content, ['.csv', '.tsv'])
  );
}

export function isContentMarkdown(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype === 'text/markdown' ||
    mimetype === 'text/x-markdown' ||
    nodeNameEndsWith(content, ['.md', '.markdown'])
  );
}

/**
 * Modern Word documents (OOXML .docx). The legacy binary .doc format is not
 * covered: no reliable open-source client-side renderer exists for it.
 */
export function isContentWord(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype ===
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
    nodeNameEndsWith(content, ['.docx'])
  );
}

/** Excel spreadsheets: both modern .xlsx and legacy binary .xls. */
export function isContentExcel(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype === 'application/vnd.ms-excel' ||
    mimetype ===
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
    nodeNameEndsWith(content, ['.xls', '.xlsx'])
  );
}

export function isContentImage(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    ['image/gif', 'image/png', 'image/jpeg', 'image/bmp'].includes(
      content.properties.mimetype.toLowerCase()
    )
  );
}

/**
 * WebP images. Rendered natively by the browser via <img>, served from the raw
 * content servlet (Alfresco 4.2.f does not map the webp mimetype, so detection
 * also relies on the file extension).
 */
export function isContentWebp(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return mimetype === 'image/webp' || nodeNameEndsWith(content, ['.webp']);
}

/**
 * SVG images. Rendered inside the sandboxed iframe (not via <img>) so the strict
 * CSP + DOM scrub neutralise any script embedded in the SVG.
 */
export function isContentSvg(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return mimetype === 'image/svg+xml' || nodeNameEndsWith(content, ['.svg']);
}

/** TIFF images, decoded client-side with UTIF.js and drawn to a canvas. */
export function isContentTiff(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype === 'image/tiff' || nodeNameEndsWith(content, ['.tif', '.tiff'])
  );
}

/** Rich Text Format documents, rendered client-side with rtf.js. */
export function isContentRtf(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype === 'application/rtf' ||
    mimetype === 'text/rtf' ||
    nodeNameEndsWith(content, ['.rtf'])
  );
}

/** Modern PowerPoint (OOXML .pptx), rendered client-side with pptx-preview. */
export function isContentPptx(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    mimetype ===
      'application/vnd.openxmlformats-officedocument.presentationml.presentation' ||
    nodeNameEndsWith(content, ['.pptx'])
  );
}

/**
 * Video formats natively playable by the <video> element. Containers/codecs the
 * browser cannot decode (e.g. mkv, avi, wmv, flv, mpeg, 3gp) are intentionally
 * excluded so the preview button is not shown for something that cannot play.
 */
export function isContentVideo(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    (mimetype !== undefined &&
      ['video/mp4', 'video/x-m4v', 'video/webm', 'video/ogg'].includes(
        mimetype
      )) ||
    nodeNameEndsWith(content, ['.mp4', '.m4v', '.webm', '.ogv'])
  );
}

/** Audio formats natively playable by the <audio> element. */
export function isContentAudio(content: ModelNode): boolean {
  const mimetype = content?.properties?.mimetype?.toLowerCase();
  return (
    (mimetype !== undefined &&
      [
        'audio/mpeg',
        'audio/mp4',
        'audio/aac',
        'audio/wav',
        'audio/x-wav',
        'audio/ogg',
        'audio/vorbis',
        'audio/webm',
        'audio/flac',
        'audio/x-flac',
      ].includes(mimetype)) ||
    nodeNameEndsWith(content, [
      '.mp3',
      '.m4a',
      '.aac',
      '.wav',
      '.oga',
      '.ogg',
      '.flac',
      '.weba',
    ])
  );
}

export function isContentPdf(content: ModelNode): boolean {
  return (
    content?.properties?.mimetype !== undefined &&
    ['application/pdf'].includes(content.properties.mimetype.toLowerCase())
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
  }
  if (event1StartTime < event2StartTime) {
    return -1;
  }
  return 0;
}

export function truncate(text: string, length: number): string {
  if (text.length > length) {
    return `${text.substring(0, (length === undefined ? 10 : length) - 3)}...`;
  }
  return text;
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
