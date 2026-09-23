/**
 * Represents a single localized title value.
 *
 * Associates a title string with the language it is expressed in, allowing a
 * node's title to be stored and rendered per language within the dynamic
 * properties feature.
 */
export interface TitleTag {
  /** ISO language code (e.g. `en`, `fr`) identifying the language of {@link TitleTag.value}. */
  lang: string;
  /** The localized title text for the language given by {@link TitleTag.lang}. */
  value: string;
}
