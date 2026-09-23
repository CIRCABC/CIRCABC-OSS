import { InterestGroupProfile } from 'app/core/generated/circabc';

/**
 * View-model extension of {@link InterestGroupProfile} used in user-management UI.
 *
 * Augments the generated API model with a UI selection flag so that interest
 * group profiles can be rendered and toggled in selectable lists (for example,
 * multi-select tables or checkboxes) without mutating the underlying API type.
 */
export interface InterestGroupProfileSelectable extends InterestGroupProfile {
  /**
   * Whether this profile is currently selected in the UI.
   *
   * `true` when the user has selected the profile, `false` otherwise.
   */
  selected: boolean;
}
