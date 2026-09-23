import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { DashboardHelpLayerComponent } from './dashboard-help-layer.component';

describe('DashboardHelpLayerComponent', () => {
  let component: DashboardHelpLayerComponent;

  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [DashboardHelpLayerComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(DashboardHelpLayerComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(DashboardHelpLayerComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  describe('ngOnInit', () => {
    it('should show overlay help when not previously viewed', () => {
      component.ngOnInit();
      expect(component.showOverlayHelp).toBe(true);
    });

    it('should hide overlay help when session storage has viewed flag', () => {
      sessionStorage.setItem('session-user-dashboard-viewed', 'yes');
      component.ngOnInit();
      expect(component.showOverlayHelp).toBe(false);
    });

    it('should hide overlay help when local storage has viewed flag', () => {
      localStorage.setItem('user-dashboard-viewed', 'yes');
      component.ngOnInit();
      expect(component.showOverlayHelp).toBe(false);
    });

    it('should set session storage after init', () => {
      component.ngOnInit();
      expect(sessionStorage.getItem('session-user-dashboard-viewed')).toBe(
        'yes'
      );
    });
  });

  describe('saveAsViewed', () => {
    it('should set local storage to yes when doNotShow is true', () => {
      component.saveAsViewed(true);
      expect(localStorage.getItem('user-dashboard-viewed')).toBe('yes');
    });

    it('should set local storage to no when doNotShow is false', () => {
      component.saveAsViewed(false);
      expect(localStorage.getItem('user-dashboard-viewed')).toBe('no');
    });
  });

  describe('toStep', () => {
    it('should update step', () => {
      component.toStep(2);
      expect(component.step).toBe(2);
    });

    it('should show close button at step 3', () => {
      component.toStep(3);
      expect(component.showClose).toBe(true);
    });

    it('should not show close button before step 3', () => {
      component.toStep(2);
      expect(component.showClose).toBe(false);
    });
  });
});
