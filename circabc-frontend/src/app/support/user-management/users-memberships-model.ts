import { User } from 'app/core/generated/circabc';
import { InterestGroupProfileSelectable } from 'app/support/user-management/interest-group-profile-selectable';

export interface UsersMembershipsModel {
  userid: string;
  user: User;
  memberships: InterestGroupProfileSelectable[];
  loadingMemberships: boolean;
}
