import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SwUpdate } from '@angular/service-worker';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  const mockAnalyticsService = {
    init: vi.fn(),
    trackPageChange: vi.fn(),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
    messageAnnounced$: new Subject(),
    messageDestroyed$: new Subject(),
  };

  const mockSwUpdate = {
    isEnabled: false,
    versionUpdates: new Subject(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: SwUpdate, useValue: mockSwUpdate },
      ],
    }).overrideComponent(AppComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should initialize analytics on ngOnInit', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(mockAnalyticsService.init).toHaveBeenCalled();
  });

  it('should set default language to en', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const transloco = TestBed.inject(TranslocoService);
    expect(transloco.getDefaultLang()).toBe('en');
  });

  it('should unsubscribe on destroy', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const navSpy = vi.spyOn(
      fixture.componentInstance.navigationSubscription,
      'unsubscribe'
    );
    const langSpy = vi.spyOn(
      fixture.componentInstance.languageChangeSubscription,
      'unsubscribe'
    );
    fixture.componentInstance.ngOnDestroy();
    expect(navSpy).toHaveBeenCalled();
    expect(langSpy).toHaveBeenCalled();
  });

  it('should set localStorage systemMessageAlreadyShown to -1', () => {
    const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(setItemSpy).toHaveBeenCalledWith('systemMessageAlreadyShown', '-1');
    setItemSpy.mockRestore();
  });
});
