/* eslint-disable @typescript-eslint/no-explicit-any */

/**
 * Describes a single file queued in the library upload form.
 *
 * A `FileUploadItem` bundles together the raw browser {@link File} to be
 * uploaded, transient UI/upload state (selection, progress, status) and the
 * document metadata (multilingual titles/descriptions, keywords, security
 * ranking, translation information, etc.) that is submitted alongside the
 * file when it is stored in the CIRCABC library.
 *
 * The interface also exposes a set of generic dynamic attribute slots
 * (`dynAttr1`–`dynAttr20`) and an open-ended index signature so that
 * category- or header-specific custom properties can be attached without a
 * dedicated typed field.
 */
export interface FileUploadItem {
  /** Client-side unique identifier used to track the item within the upload queue. */
  id: string;
  /** The raw browser {@link File} object to be uploaded. */
  file: File;
  /** Whether the item is currently selected in the upload form UI. */
  selected?: boolean;
  /** Upload progress as a percentage (0–100). */
  progress?: number;
  /** Current upload state label (e.g. pending, uploading, done, error). */
  uploadStatus?: string;
  /** Display name of the document (typically the file name). */
  name: string;
  /** Multilingual title, keyed by language code. */
  title?: { [key: string]: string };
  /** Multilingual description, keyed by language code. */
  description?: { [key: string]: string };
  /** List of keywords/tags associated with the document. */
  keywords?: string[];
  /** Author of the document. */
  author?: string;
  /** Free-text reference identifier for the document. */
  reference?: string;
  /** Expiration date of the document, as an ISO/formatted date string. */
  expirationDate?: string;
  /** Security ranking/classification level of the document. */
  securityRanking?: string;
  /** Document workflow status. */
  status?: string;
  /** Whether this item is the pivot (reference) document of a translation set. */
  isPivot?: boolean;
  /** Whether this item is a translation of another document. */
  isTranslation?: boolean;
  /** Identifier/reference of the source document this item translates, when applicable. */
  translationOf?: string;
  /** Language code of the document content. */
  lang?: string;
  /** Backend node reference assigned once the document is stored. */
  nodeRef?: string;
  /** Dynamic/custom attribute slot 1. */
  dynAttr1?: any;
  /** Dynamic/custom attribute slot 2. */
  dynAttr2?: any;
  /** Dynamic/custom attribute slot 3. */
  dynAttr3?: any;
  /** Dynamic/custom attribute slot 4. */
  dynAttr4?: any;
  /** Dynamic/custom attribute slot 5. */
  dynAttr5?: any;
  /** Dynamic/custom attribute slot 6. */
  dynAttr6?: any;
  /** Dynamic/custom attribute slot 7. */
  dynAttr7?: any;
  /** Dynamic/custom attribute slot 8. */
  dynAttr8?: any;
  /** Dynamic/custom attribute slot 9. */
  dynAttr9?: any;
  /** Dynamic/custom attribute slot 10. */
  dynAttr10?: any;
  /** Dynamic/custom attribute slot 11. */
  dynAttr11?: any;
  /** Dynamic/custom attribute slot 12. */
  dynAttr12?: any;
  /** Dynamic/custom attribute slot 13. */
  dynAttr13?: any;
  /** Dynamic/custom attribute slot 14. */
  dynAttr14?: any;
  /** Dynamic/custom attribute slot 15. */
  dynAttr15?: any;
  /** Dynamic/custom attribute slot 16. */
  dynAttr16?: any;
  /** Dynamic/custom attribute slot 17. */
  dynAttr17?: any;
  /** Dynamic/custom attribute slot 18. */
  dynAttr18?: any;
  /** Dynamic/custom attribute slot 19. */
  dynAttr19?: any;
  /** Dynamic/custom attribute slot 20. */
  dynAttr20?: any;

  /** Open-ended index signature allowing arbitrary additional custom properties. */
  [key: string]: any;
}
