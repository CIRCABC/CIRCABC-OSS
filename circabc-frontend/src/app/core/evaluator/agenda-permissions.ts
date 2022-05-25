export enum AgendaPermissions {
  EveNoAccess,
  EveAccess,
  EveAdmin,
}

export const agendaPermissionKeys = Object.keys(AgendaPermissions).filter(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (k) => typeof AgendaPermissions[k as any] === 'number'
);
