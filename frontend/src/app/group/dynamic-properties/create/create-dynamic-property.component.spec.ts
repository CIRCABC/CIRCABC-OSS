import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateDynamicPropertyComponent } from './create-dynamic-property.component';

const mockDynamicPropertiesService = {
  postPropertyDefinition: vi.fn(),
  getDynamicPropertyDefinitionAsync: vi.fn(),
  putDynamicPropertyDefinition: vi.fn(),
  postPropertyDefinitionAsync: vi.fn(),
  putDynamicPropertyDefinitionAsync: vi.fn(),
};

describe('CreateDynamicPropertyComponent', () => {
  let component: CreateDynamicPropertyComponent;
  let componentRef: ComponentRef<CreateDynamicPropertyComponent>;
  let fixture: ComponentFixture<CreateDynamicPropertyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateDynamicPropertyComponent],
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
    }).compileComponents();

    fixture = TestBed.createComponent(CreateDynamicPropertyComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group-1');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize forms on ngOnInit', () => {
    expect(component.createForm).toBeDefined();
    expect(component.editDynPropForm).toBeDefined();
    expect(component.newValueForm).toBeDefined();
  });

  it('should have invalid createForm initially', () => {
    expect(component.createForm.valid).toBe(false);
  });

  it('should not create when form is invalid', async () => {
    await component.create();
    expect(
      mockDynamicPropertiesService.postPropertyDefinitionAsync
    ).not.toHaveBeenCalled();
  });

  it('should call postPropertyDefinition on valid create', async () => {
    const mockResult: DynamicPropertyDefinition = {
      title: { en: 'Test' },
      propertyType: 'TEXT_FIELD',
    };
    mockDynamicPropertiesService.postPropertyDefinitionAsync.mockResolvedValue(
      mockResult
    );

    component.createForm.controls['title'].setValue({ en: 'Test' });
    component.createForm.controls['propertyType'].setValue('TEXT_FIELD');

    await component.create();

    expect(
      mockDynamicPropertiesService.postPropertyDefinitionAsync
    ).toHaveBeenCalledWith({
      id: 'group-1',
      dynamicPropertyDefinition: expect.objectContaining({
        propertyType: 'TEXT_FIELD',
      }),
    });
    expect(component.creating).toBe(false);
  });

  it('should transform possible values from multiline string', async () => {
    const mockResult: DynamicPropertyDefinition = {
      title: { en: 'Select' },
      propertyType: 'SELECTION',
    };
    mockDynamicPropertiesService.postPropertyDefinitionAsync.mockResolvedValue(
      mockResult
    );

    component.createForm.controls['title'].setValue({ en: 'Select' });
    component.createForm.controls['propertyType'].setValue('SELECTION');
    component.createForm.controls['possibleValues'].setValue(
      'val1\nval2\nval3'
    );

    await component.create();

    expect(
      mockDynamicPropertiesService.postPropertyDefinitionAsync
    ).toHaveBeenCalledWith({
      id: 'group-1',
      dynamicPropertyDefinition: expect.objectContaining({
        possibleValues: ['val1', 'val2', 'val3'],
      }),
    });
  });

  it('should return false for canCreateOrUpdate when form is invalid', () => {
    expect(component.canCreateOrUpdate()).toBe(false);
  });

  it('should return true for canCreateOrUpdate when form is valid and no property input', () => {
    component.createForm.controls['title'].setValue({ en: 'Test' });
    component.createForm.controls['propertyType'].setValue('TEXT_FIELD');
    expect(component.canCreateOrUpdate()).toBe(true);
  });

  it('should load dynamic property on ngOnChanges when property is set', async () => {
    const mockProp: DynamicPropertyDefinition = {
      id: 'prop-1',
      title: { en: 'Loaded' },
      propertyType: 'SELECTION',
      possibleValues: ['a', 'b'],
    };
    mockDynamicPropertiesService.getDynamicPropertyDefinitionAsync.mockResolvedValue(
      mockProp
    );

    componentRef.setInput('property', {
      id: 'prop-1',
      title: { en: '' },
      propertyType: 'SELECTION',
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionAsync
    ).toHaveBeenCalledWith({ id: 'prop-1' });
    expect(component.originalProperty).toEqual(mockProp);
  });

  it('should initialize values from originalProperty with SELECTION type', () => {
    component.originalProperty = {
      title: { en: 'Test' },
      propertyType: 'SELECTION',
      possibleValues: ['opt1', 'opt2'],
    };

    const result = component.initValues();

    expect(result).toEqual([
      { old: 'opt1', new: 'opt1', status: '' },
      { old: 'opt2', new: 'opt2', status: '' },
    ]);
  });

  it('should add a new value', () => {
    component.values = [];
    component.newValueForm.controls['newValue'].setValue('newVal');
    component.addValue();

    expect(component.values).toEqual([
      { old: 'newVal', new: 'newVal', status: 'new' },
    ]);
  });

  it('should tag item as deleted', () => {
    const item = { old: 'x', _new: 'x', status: '' };
    component.values = [item];
    component.tagAsRemoved(item);
    expect(component.values[0].status).toBe('deleted');
  });

  it('should toggle deleted status back to empty', () => {
    const item = { old: 'x', _new: 'x', status: 'deleted' };
    component.values = [item];
    component.tagAsRemoved(item);
    expect(component.values[0].status).toBe('');
  });

  it('should remove new items completely on tagAsRemoved', () => {
    const item = { old: 'x', _new: 'x', status: 'new' };
    component.values = [item];
    component.tagAsRemoved(item);
    expect(component.values).toHaveLength(0);
  });

  it('should move item up', () => {
    const a = { old: 'a', _new: 'a', status: '' };
    const b = { old: 'b', _new: 'b', status: '' };
    component.values = [a, b];
    component.moveUp(b);
    expect(component.values[0]).toBe(b);
    expect(component.values[1]).toBe(a);
  });

  it('should move first item to end on moveUp', () => {
    const a = { old: 'a', _new: 'a', status: '' };
    const b = { old: 'b', _new: 'b', status: '' };
    component.values = [a, b];
    component.moveUp(a);
    expect(component.values[0]).toBe(b);
    expect(component.values[1]).toBe(a);
  });

  it('should move item down', () => {
    const a = { old: 'a', _new: 'a', status: '' };
    const b = { old: 'b', _new: 'b', status: '' };
    component.values = [a, b];
    component.moveDown(a);
    expect(component.values[0]).toBe(b);
    expect(component.values[1]).toBe(a);
  });

  it('should move last item to beginning on moveDown', () => {
    const a = { old: 'a', _new: 'a', status: '' };
    const b = { old: 'b', _new: 'b', status: '' };
    component.values = [a, b];
    component.moveDown(b);
    expect(component.values[0]).toBe(b);
    expect(component.values[1]).toBe(a);
  });

  it('should finish edit and update value', () => {
    const item = { old: 'orig', _new: 'orig', status: '' };
    component.values = [item];
    component.currentIndex = 0;
    component.newValueForm.controls['newValue'].setValue('edited');
    component.finishEdit();

    expect(component.values[0].new).toBe('edited');
    expect(component.values[0].status).toBe('edited');
    expect(component.currentIndex).toBe(-1);
  });

  it('should emit canceled result on cancelWizard', () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.cancelWizard();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ result: ActionResult.CANCELED })
    );
  });

  it('should call putDynamicPropertyDefinition on update', async () => {
    const prop: DynamicPropertyDefinition = {
      id: 'prop-1',
      title: { en: 'Title' },
      propertyType: 'TEXT_FIELD',
    };
    mockDynamicPropertiesService.putDynamicPropertyDefinitionAsync.mockResolvedValue(
      prop
    );
    componentRef.setInput('property', prop);

    component.values = [];
    component.editDynPropForm.controls['title'].setValue({ en: 'Updated' });

    await component.update();

    expect(
      mockDynamicPropertiesService.putDynamicPropertyDefinitionAsync
    ).toHaveBeenCalledWith({
      id: 'prop-1',
      dynamicPropertyDefinition: expect.objectContaining({ id: 'prop-1' }),
    });
    expect(component.creating).toBe(false);
  });

  it('should return dynamic property types from getTypes', () => {
    const types = component.getTypes();
    expect(types.length).toBeGreaterThan(0);
  });

  it('should return true for hasValues when originalProperty is SELECTION', () => {
    component.originalProperty = {
      title: { en: 'Test' },
      propertyType: 'SELECTION',
    };
    expect(component.hasValues()).toBe(true);
  });

  it('should return false for hasValues when originalProperty is TEXT_FIELD', () => {
    component.originalProperty = {
      title: { en: 'Test' },
      propertyType: 'TEXT_FIELD',
    };
    expect(component.hasValues()).toBe(false);
  });
});
