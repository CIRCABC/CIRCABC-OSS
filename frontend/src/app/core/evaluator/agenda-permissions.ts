/**
 * Enumerates the access levels a user can have on the agenda (event/calendar)
 * service of an interest group.
 *
 * Defined as a `const` object (rather than a TypeScript `enum`) so the numeric
 * values can be used both as runtime lookup values and as a literal union type
 * via the companion {@link AgendaPermissions} type alias.
 *
 * Ordered by increasing privilege:
 * - `EveNoAccess` (0): the user cannot access the agenda service.
 * - `EveAccess` (1): the user can view/consult agenda events.
 * - `EveAdmin` (2): the user can administer agenda events (create, edit, delete).
 */
export const AgendaPermissions = {
  EveNoAccess: 0,
  EveAccess: 1,
  EveAdmin: 2,
} as const;

/**
 * Union type of the valid agenda permission values (`0 | 1 | 2`) derived from
 * the {@link AgendaPermissions} const object.
 *
 * Shares the same name as the const object, allowing it to be used as both a
 * value (e.g. `AgendaPermissions.EveAdmin`) and a type annotation
 * (e.g. `permission: AgendaPermissions`).
 */
export type AgendaPermissions =
  (typeof AgendaPermissions)[keyof typeof AgendaPermissions];

/**
 * Array of string keys from the AgendaPermissions object
 * Used for permission lookups and UI components
 */
export const agendaPermissionKeys: string[] = Object.keys(AgendaPermissions);
