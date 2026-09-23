import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { NoContentFoundComponent } from './no-content-found.component';

describe('NoContentFoundComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoContentFoundComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(NoContentFoundComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(NoContentFoundComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });
});
