import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { EditDynamicPropertyComponent } from './edit-dynamic-property.component';

const mockProperty: DynamicPropertyDefinition = {
  id: 'dp-1',
  title: { en: 'Test Property' },
  propertyType: 'SELECTION',
  possibleValues: ['Value1', 'Value2', 'Value3'],
};

describe('EditDynamicPropertyComponent', () => {
  let component: EditDynamicPropertyComponent;

  const mockDynamicPropertiesService = {
    getDynamicPropertyDefinitionAsync: vi.fn().mockResolvedValue(mockProperty),
    putDynamicPropertyDefinition: vi.fn().mockReturnValue(of({})),
    putDynamicPropertyDefinitionAsync: vi.fn().mockResolvedValue({}),
  };

  const mockLocation = { back: vi.fn() };

  const mockRoute = { params: of({ dpId: 'dp-1' }) };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [EditDynamicPropertyComponent, ReactiveFormsModule],
      providers: [
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        { provide: Location, useValue: mockLocation },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(EditDynamicPropertyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load dynamic property on init', () => {
    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionAsync
    ).toHaveBeenCalledWith({ id: 'dp-1' });
    expect(component.originalProperty()).toEqual(mockProperty);
    expect(component.loading()).toBe(false);
  });

  it('should initialize values from possibleValues for SELECTION type', () => {
    expect(component.values()).toHaveLength(3);
    expect(component.values()[0]).toEqual({
      old: 'Value1',
      new: 'Value1',
      status: '',
    });
    expect(component.values()[1]).toEqual({
      old: 'Value2',
      new: 'Value2',
      status: '',
    });
  });

  it('should return true for hasValues when propertyType is SELECTION', () => {
    expect(component.hasValues()).toBe(true);
  });

  it('should return false for hasValues when propertyType is TEXT_FIELD', () => {
    component.originalProperty.set({
      ...mockProperty,
      propertyType: 'TEXT_FIELD',
    });
    expect(component.hasValues()).toBe(false);
  });

  it('should add a new value', () => {
    component.newValueForm.controls['newValue'].setValue('NewVal');
    component.addValue();
    expect(component.values()).toHaveLength(4);
    expect(component.values()[3]).toEqual({
      old: 'NewVal',
      new: 'NewVal',
      status: 'new',
    });
  });

  it('should tag item as deleted', () => {
    component.tagAsRemoved(component.values()[0]);
    expect(component.values()[0].status).toBe('deleted');
  });

  it('should toggle deleted status back to empty', () => {
    component.values()[0].status = 'deleted';
    component.tagAsRemoved(component.values()[0]);
    expect(component.values()[0].status).toBe('');
  });

  it('should remove new items entirely', () => {
    component.values.update((values) => [
      ...values,
      { old: 'X', _new: 'X', status: 'new' },
    ]);
    const newItem = component.values()[component.values().length - 1];
    component.tagAsRemoved(newItem);
    expect(component.values()).not.toContain(newItem);
  });

  it('should tag item as edited and update _new value', () => {
    component.tagAsEdited(component.values()[1]);
    expect(component.currentIndex).toBe(1);
    component.newValueForm.controls['newValue'].setValue('Edited');
    component.finishEdit();
    expect(component.values()[1].status).toBe('edited');
    expect(component.values()[1].new).toBe('Edited');
    expect(component.currentIndex).toBe(-1);
  });

  it('should move item up', () => {
    const item = component.values()[1];
    component.moveUp(item);
    expect(component.values()[0]).toBe(item);
  });

  it('should wrap first item to end when moving up', () => {
    const item = component.values()[0];
    component.moveUp(item);
    expect(component.values()[component.values().length - 1]).toBe(item);
  });

  it('should move item down', () => {
    const item = component.values()[0];
    component.moveDown(item);
    expect(component.values()[1]).toBe(item);
  });

  it('should wrap last item to beginning when moving down', () => {
    const item = component.values()[component.values().length - 1];
    component.moveDown(item);
    expect(component.values()[0]).toBe(item);
  });

  it('should call location.back on cancel', () => {
    component.cancel();
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should save and navigate back on ok', async () => {
    component.editDynPropForm.controls['title'].setValue({ en: 'Updated' });
    await component.ok();
    expect(
      mockDynamicPropertiesService.putDynamicPropertyDefinitionAsync
    ).toHaveBeenCalled();
    const args =
      mockDynamicPropertiesService.putDynamicPropertyDefinitionAsync.mock
        .calls[0];
    expect(args[0].id).toBe('dp-1');
    expect(args[0].dynamicPropertyDefinition.title.en).toBe('Updated');
    expect(mockLocation.back).toHaveBeenCalled();
    expect(component.executing()).toBe(false);
  });

  it('should not navigate back when save fails', async () => {
    mockDynamicPropertiesService.putDynamicPropertyDefinitionAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    await component.ok();
    expect(mockLocation.back).not.toHaveBeenCalled();
  });

  it('should reset edited item', () => {
    component.values()[0].status = 'edited';
    component.values()[0].new = 'Changed';
    component.resetEdited(component.values()[0]);
    expect(component.values()[0].status).toBe('');
    expect(component.values()[0].new).toBe(component.values()[0].old);
    expect(component.currentIndex).toBe(-1);
  });

  it('should cancel edit and reset form', () => {
    component.currentIndex = 1;
    component.newValueForm.controls['newValue'].setValue('something');
    component.cancelEdit(component.values()[0]);
    expect(component.currentIndex).toBe(-1);
    expect(component.newValueForm.value.newValue).toBeNull();
  });
});
