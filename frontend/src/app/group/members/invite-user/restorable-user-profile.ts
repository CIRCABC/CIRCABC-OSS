import { User, UserRecoveryOption } from 'app/core/generated/circabc';

/**
 * Describes a user account that has been detected as restorable during the
 * invite-user flow, pairing the account identity with the strategy that can be
 * used to recover/reactivate it.
 *
 * This model is produced when an invited email matches an existing (typically
 * deactivated or previously removed) account, allowing the UI to offer a
 * restore path instead of creating a duplicate user.
 */
export interface RestorableUserProfile {
  /** Unique identifier of the existing user account that can be restored. */
  userId: string;
  /** Recovery strategy available for reactivating the matched account. */
  recoveryOption: UserRecoveryOption;
  /** Full user record associated with the restorable account. */
  user: User;
}
