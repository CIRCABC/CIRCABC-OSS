import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { MembersDropdownComponent } from './members-dropdown.component';

describe('MembersDropdownComponent', () => {
  let component: MembersDropdownComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MembersDropdownComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(MembersDropdownComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(MembersDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('onClick', () => {
    it('should toggle showActionsDropdown when clicking dropdown-trigger', () => {
      const event = {
        target: {
          classList: { contains: (cls: string) => cls === 'dropdown-trigger' },
        },
      };

      component.onClick(event);
      expect(component.showActionsDropdown).toBe(true);

      component.onClick(event);
      expect(component.showActionsDropdown).toBe(false);
    });

    it('should set showActionsDropdown to false when clicking outside', () => {
      component.showActionsDropdown = true;
      const event = { target: { classList: { contains: () => false } } };

      component.onClick(event);
      expect(component.showActionsDropdown).toBe(false);
    });
  });

  describe('showWizardClick', () => {
    it('should emit true on showWizard output', () => {
      const spy = vi.fn();
      component.showWizard.subscribe(spy);

      component.showWizardClick();
      expect(spy).toHaveBeenCalledWith(true);
    });
  });

  describe('showUserCreateWizardClick', () => {
    it('should emit true on showUserCreateWizard output', () => {
      const spy = vi.fn();
      component.showUserCreateWizard.subscribe(spy);

      component.showUserCreateWizardClick();
      expect(spy).toHaveBeenCalledWith(true);
    });
  });
});
