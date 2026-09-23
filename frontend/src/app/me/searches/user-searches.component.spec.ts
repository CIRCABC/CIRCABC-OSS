import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { UserSearchesComponent } from './user-searches.component';

describe('UserSearchesComponent', () => {
  function createComponent() {
    TestBed.configureTestingModule({
      imports: [UserSearchesComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(UserSearchesComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(UserSearchesComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should render the nothing to display message placeholder', () => {
    const fixture = createComponent();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.main--listing')).not.toBeNull();
  });
});
