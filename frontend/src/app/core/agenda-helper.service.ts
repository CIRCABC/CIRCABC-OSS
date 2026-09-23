import { Service } from '@angular/core';

/**
 * Root-provided service that holds transient, view-related state for the
 * agenda and personal calendar features.
 *
 * Its responsibilities are:
 * - Persisting the selected calendar view (e.g. `month`, `week`, `day`) for
 *   both the group agenda and the personal "My Calendar" view across
 *   navigation, using the browser `sessionStorage`.
 * - Keeping an in-memory mapping of event identifiers to display colours so
 *   that events can be rendered consistently within a session.
 * - Tracking a UI toggle that controls whether calendar "ribbons" are shown.
 *
 * Because the colour map and ribbon flag are stored in memory, they live for
 * the lifetime of the singleton (i.e. until a full page reload), whereas the
 * view states survive reloads through `sessionStorage`.
 *
 * Key collaborator: the browser `sessionStorage` Web Storage API.
 */
@Service()
export class AgendaHelperService {
  /**
   * In-memory mapping of event identifiers to their display colour.
   * Populated via {@link setEventDisplayColor} and read via
   * {@link getEventDisplayColor}. Not persisted across page reloads.
   */
  private readonly eventDisplaycolors: Map<string, string> = new Map<
    string,
    string
  >();

  /**
   * In-memory flag indicating whether calendar ribbons should be displayed.
   * Toggled via {@link toggleShowRibbons} and read via {@link isShowRibbons}.
   */
  private showRibbons = false;

  /**
   * Persists the currently selected group agenda view state in
   * `sessionStorage` under the `agenda` key.
   *
   * @param viewState The agenda view identifier to store (e.g. `month`,
   * `week`, `day`).
   */
  public saveAgendaViewState(viewState: string) {
    sessionStorage.setItem('agenda', viewState);
  }

  /**
   * Retrieves the previously stored group agenda view state.
   *
   * @returns The stored agenda view identifier, or `'month'` if none has
   * been saved yet.
   */
  public getAgendaViewState(): string {
    const value = sessionStorage.getItem('agenda');
    return value ?? 'month';
  }

  /**
   * Persists the currently selected personal "My Calendar" view state in
   * `sessionStorage` under the `myCalendar` key.
   *
   * @param viewState The calendar view identifier to store (e.g. `month`,
   * `week`, `day`).
   */
  public saveMyCalendarViewState(viewState: string) {
    sessionStorage.setItem('myCalendar', viewState);
  }

  /**
   * Retrieves the previously stored personal "My Calendar" view state.
   *
   * @returns The stored calendar view identifier, or `'month'` if none has
   * been saved yet.
   */
  public getMyCalendarViewState(): string {
    const value = sessionStorage.getItem('myCalendar');
    return value ?? 'month';
  }

  /**
   * Associates a display colour with a given event identifier in the
   * in-memory colour map.
   *
   * @param eventId The identifier of the event.
   * @param color The colour to associate with the event.
   */
  public setEventDisplayColor(eventId: string, color: string) {
    this.eventDisplaycolors.set(eventId, color);
  }

  /**
   * Returns the display colour associated with the given event identifier.
   *
   * @param eventId The identifier of the event.
   * @returns The stored colour for the event, or `undefined` if no colour
   * has been set for it.
   */
  public getEventDisplayColor(eventId: string): string | undefined {
    return this.eventDisplaycolors.get(eventId);
  }

  /**
   * Flips the ribbon visibility flag between shown and hidden.
   *
   * @returns The new state of the flag after toggling (`true` if ribbons
   * are now shown, `false` otherwise).
   */
  public toggleShowRibbons(): boolean {
    this.showRibbons = !this.showRibbons;
    return this.showRibbons;
  }

  /**
   * Indicates whether calendar ribbons are currently set to be shown.
   *
   * @returns `true` if ribbons should be displayed, `false` otherwise.
   */
  public isShowRibbons(): boolean {
    return this.showRibbons;
  }
}
