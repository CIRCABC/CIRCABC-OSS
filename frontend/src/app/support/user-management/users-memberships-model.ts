import { User } from 'app/core/generated/circabc';
import { InterestGroupProfileSelectable } from 'app/support/user-management/interest-group-profile-selectable';

/**
 * View model that ties a single user to their interest-group memberships
 * within the user management support area.
 *
 * It bundles the user's identity, the full user record and the collection of
 * interest-group profiles the user belongs to, together with a flag used by
 * the UI to indicate that the membership list is still being loaded.
 */
export interface UsersMembershipsModel {
  /** Unique identifier of the user this model describes. */
  userid: string;
  /** Full user record associated with {@link UsersMembershipsModel.userid}. */
  user: User;
  /**
   * Interest-group profiles the user is a member of, each augmented with
   * selection state so the UI can track which memberships are chosen.
   */
  memberships: InterestGroupProfileSelectable[];
  /**
   * `true` while the memberships are being fetched, allowing the UI to show a
   * loading indicator; `false` once loading has completed.
   */
  loadingMemberships: boolean;
}
