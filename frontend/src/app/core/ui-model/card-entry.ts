/**
 * View-model describing a single card rendered in the UI (for example on a
 * dashboard or in a card-based listing).
 *
 * A `CardEntry` bundles together the display data and presentation hints
 * needed to render one card, decoupling the card UI components from the
 * underlying domain/API models.
 */
export interface CardEntry {
  /** Unique numeric identifier of the card within its list. */
  id: number;
  /** Discriminator describing the kind of content the card represents. */
  type: string;
  /** Heading text shown at the top of the card. */
  title: string;
  /** Body text or description displayed inside the card. */
  text: string;
  /** Date associated with the card, formatted as a string for display. */
  date: string;
  /** Size metric for the card's content (for example a file size or count). */
  size: number;
  /** Visual theme/style variant applied when rendering the card. */
  skin: string;
  /** Optional explicit render height, in pixels, for the card. */
  height?: number;
  /**
   * Optional bag of arbitrary additional properties keyed by name, used to
   * carry extra metadata that does not map to a dedicated field.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  properties?: { [key: string]: any };
}
