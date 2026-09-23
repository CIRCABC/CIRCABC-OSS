import { TestBed } from '@angular/core/testing';

import { AgendaHelperService } from 'app/core/agenda-helper.service';

describe('AgendaHelperService', () => {
  let service: AgendaHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AgendaHelperService);
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  describe('saveAgendaViewState / getAgendaViewState', () => {
    it('should return "month" when no state is saved', () => {
      expect(service.getAgendaViewState()).toBe('month');
    });

    it('should return the saved view state', () => {
      service.saveAgendaViewState('week');
      expect(service.getAgendaViewState()).toBe('week');
    });
  });

  describe('saveMyCalendarViewState / getMyCalendarViewState', () => {
    it('should return "month" when no state is saved', () => {
      expect(service.getMyCalendarViewState()).toBe('month');
    });

    it('should return the saved view state', () => {
      service.saveMyCalendarViewState('day');
      expect(service.getMyCalendarViewState()).toBe('day');
    });
  });

  describe('setEventDisplayColor / getEventDisplayColor', () => {
    it('should return undefined for unknown event', () => {
      expect(service.getEventDisplayColor('unknown')).toBeUndefined();
    });

    it('should return the color set for an event', () => {
      service.setEventDisplayColor('evt1', '#ff0000');
      expect(service.getEventDisplayColor('evt1')).toBe('#ff0000');
    });
  });

  describe('toggleShowRibbons / isShowRibbons', () => {
    it('should default to false', () => {
      expect(service.isShowRibbons()).toBe(false);
    });

    it('should toggle to true and return the new value', () => {
      expect(service.toggleShowRibbons()).toBe(true);
      expect(service.isShowRibbons()).toBe(true);
    });

    it('should toggle back to false', () => {
      service.toggleShowRibbons();
      expect(service.toggleShowRibbons()).toBe(false);
      expect(service.isShowRibbons()).toBe(false);
    });
  });
});
