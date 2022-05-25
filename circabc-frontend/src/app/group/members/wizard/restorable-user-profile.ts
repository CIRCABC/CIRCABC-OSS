import { User, UserRecoveryOption } from 'app/core/generated/circabc';

export interface RestorableUserProfile {
  userId: string;
  recoveryOption: UserRecoveryOption;
  user: User;
}
