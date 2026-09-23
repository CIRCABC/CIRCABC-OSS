import { Routes } from '@angular/router';

/**
 * Child route configuration for the group agenda feature.
 *
 * Each route lazy-loads its target standalone component via `loadComponent`
 * so that the agenda screens are only fetched when navigated to:
 * - `''` — the agenda overview ({@link AgendaComponent}).
 * - `':eventId/details'` — view/edit details of a single event
 *   ({@link ViewEditDetailsEventComponent}); `eventId` identifies the event.
 * - `'list'` — the tabular list of agenda events ({@link ListComponent}).
 */
export const agendaRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/agenda/agenda.component').then(
        (m) => m.AgendaComponent
      ),
  },
  {
    path: ':eventId/details',
    loadComponent: () =>
      import('app/group/agenda/view-edit-details-event/view-edit-details-event.component').then(
        (m) => m.ViewEditDetailsEventComponent
      ),
  },
  {
    path: 'list',
    loadComponent: () =>
      import('app/group/agenda/list/list.component').then(
        (m) => m.ListComponent
      ),
  },
];
