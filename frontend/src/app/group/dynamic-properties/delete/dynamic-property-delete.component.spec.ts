import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { DynamicPropertiesService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DynamicPropertyDeleteComponent } from './dynamic-property-delete.component';

const mockDynamicPropertiesService = {
  deleteDynamicPropertyDefinition: vi.fn().mockReturnValue(of(undefined)),
  deleteDynamicPropertyDefinitionAsync: vi.fn().mockResolvedValue(undefined),
};

describe('DynamicPropertyDeleteComponent', () => {
  let component: DynamicPropertyDeleteComponent;
  let componentRef: ComponentRef<DynamicPropertyDeleteComponent>;
  let fixture: ComponentFixture<DynamicPropertyDeleteComponent>;

  beforeEach(() => {
    mockDynamicPropertiesService.deleteDynamicPropertyDefinition.mockClear();
    mockDynamicPropertiesService.deleteDynamicPropertyDefinitionAsync.mockClear();

    TestBed.configureTestingModule({
      imports: [DynamicPropertyDeleteComponent],
      providers: [
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(DynamicPropertyDeleteComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(DynamicPropertyDeleteComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('titleAsArray', () => {
    it('should return empty array when no property', () => {
      expect(component.getTitleAsArray()).toEqual([]);
    });

    it('should convert title object to TitleTag array', () => {
      componentRef.setInput('property', {
        title: { en: 'English', fr: 'French' },
        propertyType: 'TEXT_FIELD',
      });
      fixture.detectChanges();

      expect(component.getTitleAsArray()).toEqual([
        { lang: 'en', value: 'English' },
        { lang: 'fr', value: 'French' },
      ]);
    });
  });

  describe('delete', () => {
    it('should call service and emit SUCCEED when property has id', async () => {
      componentRef.setInput('property', {
        id: '123',
        title: { en: 'Test' },
        propertyType: 'TEXT_FIELD',
      });
      component.showModal.set(true);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(
        mockDynamicPropertiesService.deleteDynamicPropertyDefinitionAsync
      ).toHaveBeenCalledWith({ id: '123' });
      expect(component.deleting()).toBe(false);
      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_DYNAMIC_PROPERTY,
        result: ActionResult.SUCCEED,
      });
    });

    it('should emit FAILED when property has no id', async () => {
      componentRef.setInput('property', {
        title: { en: 'Test' },
        propertyType: 'TEXT_FIELD',
      });
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(
        mockDynamicPropertiesService.deleteDynamicPropertyDefinitionAsync
      ).not.toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_DYNAMIC_PROPERTY,
        result: ActionResult.FAILED,
      });
    });
  });

  describe('cancelWizard', () => {
    it('should close modal and emit CANCELED', () => {
      component.showModal.set(true);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_DYNAMIC_PROPERTY,
        result: ActionResult.CANCELED,
      });
    });
  });
});
