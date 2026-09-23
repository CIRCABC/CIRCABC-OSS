import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ExplorerDropdownComponent } from './explorer-dropdown.component';

describe('ExplorerDropdownComponent', () => {
  let component: ExplorerDropdownComponent;
  let componentRef: ComponentRef<ExplorerDropdownComponent>;
  let fixture: ComponentFixture<ExplorerDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExplorerDropdownComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ExplorerDropdownComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

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

  it('should close dropdown when clicking outside', () => {
    component.showActionsDropdown = true;
    const event = { target: { classList: { contains: () => false } } };

    component.onClick(event);
    expect(component.showActionsDropdown).toBe(false);
  });

  it('should accept currentHeader and currentCategory inputs', () => {
    componentRef.setInput('currentHeader', { id: 'h1', name: 'Header 1' });
    componentRef.setInput('currentCategory', { id: 'c1', name: 'Category 1' });
    fixture.detectChanges();

    expect(component.currentHeader()).toEqual({ id: 'h1', name: 'Header 1' });
    expect(component.currentCategory()).toEqual({
      id: 'c1',
      name: 'Category 1',
    });
  });
});
