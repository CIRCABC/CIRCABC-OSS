import { Profile, User } from 'app/core/generated/circabc';

/**
 * Associates an authority (either an individual {@link User} or a
 * {@link Profile} group) with the permission granted to it.
 *
 * Used when adding or editing permission assignments so that a target
 * authority can be paired with the permission level it should receive.
 */
export interface PermDef {
  /** The authority being granted the permission: an individual user or a profile/group. */
  authority: User | Profile;
  /** The permission identifier granted to the {@link authority} (e.g. a permission/role key). */
  permission: string;
}

/**
 * A keyed permission definition, pairing a unique lookup key with its
 * associated {@link PermDef}.
 *
 * Enables keeping a collection of permission assignments addressable by a
 * stable key (for example when tracking or updating entries in a form).
 */
export interface AuthConfig {
  /** Unique key identifying this authority/permission entry. */
  authKey: string;
  /** The permission definition associated with {@link authKey}. */
  authValue: PermDef;
}
