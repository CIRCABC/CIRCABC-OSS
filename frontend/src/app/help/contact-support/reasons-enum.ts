/**
 * Enumerates the selectable reasons a user can pick when submitting a
 * contact-support request from the help section.
 *
 * Each member's value is a Transloco i18n translation key rather than a
 * human-readable label, allowing the corresponding reason to be displayed
 * in the user's chosen language and included in the support request payload.
 */
export enum ContactReasons {
  /** Generic fallback reason for enquiries that do not fit other categories. */
  OTHER = 'help.reason.other',
  /** The user needs help uploading or downloading documents. */
  UPLOAD_DOWNLOAD = 'help.reason.upload.or.download',
  /** The user has an issue related to access rights or permissions. */
  ACCESS_PERMISSION = 'help.reason.access.or.permission',
  /** The user needs assistance administering their interest group. */
  ADMINISTER_GROUP = 'help.reason.administer.my.group',
  /** The user is raising an accessibility-related enquiry. */
  ACCESSIBILITY_ENQUIRY = 'help.reason.accessibility-enquiry',
}
