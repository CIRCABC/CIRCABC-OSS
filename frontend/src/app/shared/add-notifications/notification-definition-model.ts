import { Profile, User } from 'app/core/generated/circabc';

/**
 * Describes the notification configuration for a single authority
 * (an individual {@link User} or a {@link Profile} representing a group).
 *
 * Used by the add-notifications feature to associate a chosen authority
 * with the notification level/setting that should apply to it.
 */
export interface NotifDef {
  /** The authority the notification setting applies to: either an individual user or a profile/group. */
  authority: User | Profile;
  /** The notification level or setting selected for the authority (e.g. the notification type identifier). */
  notifications: string;
}

/**
 * Pairs a unique authority key with its notification definition.
 *
 * Acts as a keyed entry (typically keyed by the authority's identifier)
 * so notification definitions can be stored and looked up efficiently
 * when building or editing a set of notification configurations.
 */
export interface AuthConfig {
  /** Unique key identifying the authority (e.g. the user id or profile/group name). */
  authKey: string;
  /** The notification definition associated with {@link AuthConfig.authKey}. */
  authValue: NotifDef;
}
