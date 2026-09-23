/**
 * Represents a single quotation displayed in the UI.
 *
 * A lightweight UI model pairing an optional quotation body with its
 * optional attribution. Both fields are optional so partial or unknown
 * quote data can still be represented.
 */
export interface Quote {
  /** Name of the person credited with the quotation, if known. */
  author?: string;
  /** The textual content of the quotation. */
  text?: string;
}
