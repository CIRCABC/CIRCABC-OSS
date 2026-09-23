import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { DropdownComponent } from './dropdown.component';

describe('DropdownComponent', () => {
  let component: DropdownComponent;
  let fixture: ReturnType<typeof TestBed.createComponent<DropdownComponent>>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DropdownComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(DropdownComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(DropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('toggleAddDropdown', () => {
    it('should toggle showCreateDropdown when target has dropdown-trigger class', () => {
      const event = {
        target: { classList: { contains: vi.fn().mockReturnValue(true) } },
      };

      component.toggleAddDropdown(event);
      expect(component.showCreateDropdown).toBe(true);

      component.toggleAddDropdown(event);
      expect(component.showCreateDropdown).toBe(false);
    });

    it('should not toggle when target lacks dropdown-trigger class', () => {
      const event = {
        target: { classList: { contains: vi.fn().mockReturnValue(false) } },
      };

      component.toggleAddDropdown(event);
      expect(component.showCreateDropdown).toBe(false);
    });

    it('should not toggle when disabled', () => {
      fixture.componentRef.setInput('disabled', true);
      fixture.detectChanges();

      const event = {
        target: { classList: { contains: vi.fn().mockReturnValue(true) } },
      };

      component.toggleAddDropdown(event);
      expect(component.showCreateDropdown).toBe(false);
    });
  });

  describe('launchAddPermissionWizard', () => {
    it('should emit false and hide dropdown', () => {
      const emitSpy = vi.spyOn(component.launchCreate, 'emit');
      component.showCreateDropdown = true;

      component.launchAddPermissionWizard();

      expect(component.showCreateDropdown).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(false);
    });
  });

  describe('launchAddShareWizard', () => {
    it('should emit true and hide dropdown', () => {
      const emitSpy = vi.spyOn(component.launchCreate, 'emit');
      component.showCreateDropdown = true;

      component.launchAddShareWizard();

      expect(component.showCreateDropdown).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(true);
    });
  });

  describe('onClick', () => {
    it('should close dropdown and emit clickOutside when clicking outside', () => {
      const outsideSpy = vi.spyOn(component.clickOutside, 'emit');
      component.showCreateDropdown = true;

      const outsideElement = document.createElement('div');
      document.body.appendChild(outsideElement);
      const event = new MouseEvent('click');

      component.onClick(event, outsideElement);

      expect(component.showCreateDropdown).toBe(false);
      expect(outsideSpy).toHaveBeenCalledWith(event);
      document.body.removeChild(outsideElement);
    });

    it('should not close dropdown when clicking inside', () => {
      component.showCreateDropdown = true;
      const event = new MouseEvent('click');

      component.onClick(event, fixture.nativeElement);

      expect(component.showCreateDropdown).toBe(true);
    });

    it('should do nothing when targetElement is null', () => {
      const outsideSpy = vi.spyOn(component.clickOutside, 'emit');
      component.showCreateDropdown = true;

      component.onClick(new MouseEvent('click'), null);

      expect(component.showCreateDropdown).toBe(true);
      expect(outsideSpy).not.toHaveBeenCalled();
    });
  });
});
