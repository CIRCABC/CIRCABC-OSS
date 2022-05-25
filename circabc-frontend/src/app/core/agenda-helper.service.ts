import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AgendaHelperService {
  private eventDisplaycolors: Map<string, string> = new Map<string, string>();

  private showRibbons = false;

  public saveAgendaViewState(viewState: string) {
    sessionStorage.setItem('agenda', viewState);
  }

  public getAgendaViewState(): string {
    const value = sessionStorage.getItem('agenda');
    return value === null ? 'month' : value;
  }

  public saveMyCalendarViewState(viewState: string) {
    sessionStorage.setItem('myCalendar', viewState);
  }

  public getMyCalendarViewState(): string {
    const value = sessionStorage.getItem('myCalendar');
    return value === null ? 'month' : value;
  }

  public setEventDisplayColor(eventId: string, color: string) {
    this.eventDisplaycolors.set(eventId, color);
  }

  public getEventDisplayColor(eventId: string): string | undefined {
    return this.eventDisplaycolors.get(eventId);
  }

  public toggleShowRibbons(): boolean {
    this.showRibbons = !this.showRibbons;
    return this.showRibbons;
  }

  public isShowRibbons(): boolean {
    return this.showRibbons;
  }
}
