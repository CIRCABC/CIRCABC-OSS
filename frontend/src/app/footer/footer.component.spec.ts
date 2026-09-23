import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  APP_ALF_VERSION,
  APP_VERSION,
  BUILD_DATE,
  NODE_NAME,
} from 'app/core/variables';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FooterComponent } from './footer.component';

describe('FooterComponent', () => {
  function createComponent(
    tokens: {
      appAlfVersion?: string;
      appVersion?: string;
      nodeName?: string;
      buildDate?: string;
    } = {}
  ) {
    TestBed.configureTestingModule({
      imports: [FooterComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: APP_ALF_VERSION, useValue: tokens.appAlfVersion ?? '' },
        { provide: APP_VERSION, useValue: tokens.appVersion ?? '' },
        { provide: NODE_NAME, useValue: tokens.nodeName ?? '' },
        { provide: BUILD_DATE, useValue: tokens.buildDate ?? '' },
      ],
    }).overrideComponent(FooterComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(FooterComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should set circabcRelease from environment', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.circabcRelease).toBe(
      environment.circabcRelease
    );
  });

  it('should assign injected token values', () => {
    const fixture = createComponent({
      appAlfVersion: '5.2',
      appVersion: '1.0.0',
      nodeName: 'node-1',
      buildDate: '2026-01-01',
    });
    const comp = fixture.componentInstance;
    expect(comp.appAlfVersion).toBe('5.2');
    expect(comp.appVersion).toBe('1.0.0');
    expect(comp.nodeName).toBe('node-1');
    expect(comp.buildDate).toBe('2026-01-01');
  });

  it('should default to empty strings when tokens are empty', () => {
    const fixture = createComponent();
    const comp = fixture.componentInstance;
    expect(comp.appAlfVersion).toBe('');
    expect(comp.appVersion).toBe('');
    expect(comp.nodeName).toBe('');
    expect(comp.buildDate).toBe('');
  });
});
