export const AgendaPermissions = {
  EveNoAccess: 0,
  EveAccess: 1,
  EveAdmin: 2,
} as const;

export type AgendaPermissions =
  (typeof AgendaPermissions)[keyof typeof AgendaPermissions];

/**
 * Array of string keys from the AgendaPermissions object
 * Used for permission lookups and UI components
 */
export const agendaPermissionKeys: string[] = Object.keys(AgendaPermissions);
