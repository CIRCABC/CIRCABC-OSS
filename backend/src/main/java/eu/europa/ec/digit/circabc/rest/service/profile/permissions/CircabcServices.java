package eu.europa.ec.digit.circabc.rest.service.profile.permissions;

/**
 * Enumerates the distinct CIRCABC service areas for which profile permissions can be defined and
 * evaluated.
 *
 * <p>Each constant identifies a functional scope within the CIRCABC domain hierarchy (from the
 * top-level application down to individual Interest Group services). These values are used by the
 * permission model to group and resolve the access rights granted to a profile for a given part of
 * the platform.
 */
public enum CircabcServices {
  /** The top-level CIRCABC application scope. */
  CIRCABC,
  /** The category header scope grouping categories. */
  CATEGORY_HEADER,
  /** The category scope containing Interest Groups. */
  CATEGORY,
  /** The Interest Group (IG) scope. */
  INTEREST_GROUP,
  /** The Library service, providing document management. */
  LIBRARY,
  /** The directory scope (folder-based organisation). */
  DIRECTORY,
  /** The applicant scope, covering membership applications. */
  APPLICANT,
  /** The visibility scope, controlling content visibility settings. */
  VISIBILITY,
  /** The Newsgroup service, providing discussion forums. */
  NEWSGROUP,
  /** The survey scope, covering surveys and questionnaires. */
  SURVEY,
  /** The Event service, providing calendars and events. */
  EVENT,
  /** The Information service, providing informational content. */
  INFORMATION,
}
