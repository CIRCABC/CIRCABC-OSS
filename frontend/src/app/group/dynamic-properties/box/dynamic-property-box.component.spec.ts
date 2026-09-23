import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { DynamicPropertyDefinition } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DynamicPropertyBoxComponent } from './dynamic-property-box.component';

describe('DynamicPropertyBoxComponent', () => {
  let component: DynamicPropertyBoxComponent;
  let componentRef: ComponentRef<DynamicPropertyBoxComponent>;
  let fixture: ComponentFixture<DynamicPropertyBoxComponent>;

  const mockProperty: DynamicPropertyDefinition = {
    title: { en: 'English Title', fr: 'Titre Français' },
    propertyType: 'TEXT_FIELD',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamicPropertyBoxComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(DynamicPropertyBoxComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DynamicPropertyBoxComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('property', mockProperty);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('titleAsArray', () => {
    it('should convert title object to TitleTag array', () => {
      const result = component.getTitleAsArray();
      expect(result).toEqual([
        { lang: 'en', value: 'English Title' },
        { lang: 'fr', value: 'Titre Français' },
      ]);
    });

    it('should exclude empty string values', () => {
      componentRef.setInput('property', {
        title: { en: 'Hello', fr: '' },
        propertyType: 'TEXT_FIELD',
      } satisfies DynamicPropertyDefinition);
      fixture.detectChanges();

      const result = component.getTitleAsArray();
      expect(result).toEqual([{ lang: 'en', value: 'Hello' }]);
    });
  });

  describe('bubbleDynpropDeleted', () => {
    it('should emit the property on delete', () => {
      const spy = vi.fn();
      component.dynnamicPropDelete.subscribe(spy);
      component.bubbleDynpropDeleted();
      expect(spy).toHaveBeenCalledWith(mockProperty);
    });
  });

  describe('bubbleDynpropEdit', () => {
    it('should emit the property on edit', () => {
      const spy = vi.fn();
      component.dynamicPropEdit.subscribe(spy);
      component.bubbleDynpropEdit();
      expect(spy).toHaveBeenCalledWith(mockProperty);
    });
  });

  describe('isSelection', () => {
    it('should return true for SELECTION type', () => {
      componentRef.setInput('property', {
        title: {},
        propertyType: 'SELECTION',
      } satisfies DynamicPropertyDefinition);
      fixture.detectChanges();
      expect(component.isSelection()).toBe(true);
    });

    it('should return true for MULTI_SELECTION type', () => {
      componentRef.setInput('property', {
        title: {},
        propertyType: 'MULTI_SELECTION',
      } satisfies DynamicPropertyDefinition);
      fixture.detectChanges();
      expect(component.isSelection()).toBe(true);
    });

    it('should return false for TEXT_FIELD type', () => {
      expect(component.isSelection()).toBe(false);
    });
  });
});
