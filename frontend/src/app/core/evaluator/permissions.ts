/**
 * Union of every permission role string recognised across the CIRCABC
 * service areas.
 *
 * Each literal encodes a service-area prefix followed by the granted access
 * level, and is used by the permission evaluators to determine what actions a
 * user may perform within a given area:
 * - `Eve*` — event/calendar (agenda) roles: no access, access, admin.
 * - `Dir*` — directory/group roles: admin, manage members, access, no access.
 * - `Inf*` — information service roles: admin, manage, access, no access.
 * - `Lib*` — library (document) roles ranging from no access through
 *   read-only, edit-only, manage-own, full-edit up to admin.
 * - `Nws*` — newsgroup/forum roles: no access, access, post, moderate, admin.
 */
export type AllPermission =
  | 'EveNoAccess'
  | 'EveAccess'
  | 'EveAdmin'
  | 'DirAdmin'
  | 'DirManageMembers'
  | 'DirAccess'
  | 'DirNoAccess'
  | 'InfAdmin'
  | 'InfManage'
  | 'InfAccess'
  | 'InfNoAccess'
  | 'LibNoAccess'
  | 'LibAccess'
  | 'LibEditOnly'
  | 'LibManageOwn'
  | 'LibFullEdit'
  | 'LibAdmin'
  | 'NwsNoAccess'
  | 'NwsAccess'
  | 'NwsPost'
  | 'NwsModerate'
  | 'NwsAdmin';
