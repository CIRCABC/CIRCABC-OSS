import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ReponsiveSubMenuComponent } from './reponsive-sub-menu.component';

describe('ReponsiveSubMenuComponent', () => {
  let component: ReponsiveSubMenuComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReponsiveSubMenuComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(ReponsiveSubMenuComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });
    const fixture = TestBed.createComponent(ReponsiveSubMenuComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should have initial className "sub-menu"', () => {
    expect(component.className).toBe('sub-menu');
  });

  it('should append " responsive" on first toggle', () => {
    component.toggle();
    expect(component.className).toBe('sub-menu responsive');
  });

  it('should reset to "sub-menu" on second toggle', () => {
    component.toggle();
    component.toggle();
    expect(component.className).toBe('sub-menu');
  });
});
