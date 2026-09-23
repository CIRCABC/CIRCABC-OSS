import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { DynamicPropertiesComponent } from './dynamic-properties.component';

const mockDynProps: DynamicPropertyDefinition[] = [
  {
    id: 'dp1',
    title: { en: 'Prop 1' },
    propertyType: 'TEXT_FIELD',
  },
];

const mockIg: InterestGroup = {
  name: 'Test Group',
  permissions: {},
  libraryId: 'lib-123',
};

const mockNode: ModelNode = { id: 'lib-123', name: 'Library' };

describe('DynamicPropertiesComponent', () => {
  let component: DynamicPropertiesComponent;
  let fixture: ComponentFixture<DynamicPropertiesComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockDynamicPropertiesService = {
    getDynamicPropertyDefinitionsAsync: vi.fn().mockResolvedValue(mockDynProps),
  };
  const mockGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
  };
  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };
  const mockPermEvalService = {
    isLibAdmin: vi.fn().mockReturnValue(true),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [DynamicPropertiesComponent],
      providers: [
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
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

    fixture = TestBed.createComponent(DynamicPropertiesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load dynamic properties on route params change', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'group-1' });

    // Wait for the async operations inside the subscription
    await new Promise((resolve) => setTimeout(resolve));

    expect(component.groupId()).toBe('group-1');
    expect(component.dynamicProperties()).toEqual(mockDynProps);
    expect(component.currentIg).toEqual(mockIg);
    expect(component.currentLibrary()).toEqual(mockNode);
    expect(component.loading()).toBe(false);
  });

  it('should refresh on DELETE_DYNAMIC_PROPERTY succeed', async () => {
    component.groupId.set('group-1');
    component.deleteModalShown.set(true);
    component.selectedProperty.set(mockDynProps[0]);

    await component.refresh({
      type: ActionType.DELETE_DYNAMIC_PROPERTY,
      result: ActionResult.SUCCEED,
    });

    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync
    ).toHaveBeenCalledWith({ id: 'group-1' });
    expect(component.deleteModalShown()).toBe(false);
    expect(component.selectedProperty()).toBeUndefined();
  });

  it('should refresh on CREATE_DYNAMIC_PROPERTY succeed', async () => {
    component.groupId.set('group-1');
    component.createModalShown.set(true);

    await component.refresh({
      type: ActionType.CREATE_DYNAMIC_PROPERTY,
      result: ActionResult.SUCCEED,
    });

    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync
    ).toHaveBeenCalledWith({ id: 'group-1' });
    expect(component.createModalShown()).toBe(false);
    expect(component.propertyToUpdate()).toBeUndefined();
  });

  it('should refresh on UPDATE_DYNAMIC_PROPERTIES succeed', async () => {
    component.groupId.set('group-1');

    await component.refresh({
      type: ActionType.UPDATE_DYNAMIC_PROPERTIES,
      result: ActionResult.SUCCEED,
    });

    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync
    ).toHaveBeenCalledWith({ id: 'group-1' });
  });

  it('should show delete modal', () => {
    component.showModalDelete(mockDynProps[0]);
    expect(component.deleteModalShown()).toBe(true);
    expect(component.selectedProperty()).toEqual(mockDynProps[0]);
  });

  it('should show edit modal', () => {
    component.showModalEdit(mockDynProps[0]);
    expect(component.createModalShown()).toBe(true);
    expect(component.propertyToUpdate()).toEqual(mockDynProps[0]);
  });

  it('should return true from isLibAdmin when currentLibrary is set', () => {
    component.currentLibrary.set(mockNode);
    expect(component.isLibAdmin()).toBe(true);
    expect(mockPermEvalService.isLibAdmin).toHaveBeenCalledWith(mockNode);
  });

  it('should return false from isLibAdmin when currentLibrary is not set', () => {
    component.currentLibrary.set(undefined);
    expect(component.isLibAdmin()).toBe(false);
  });

  it('should open create modal only when fewer than 20 properties', () => {
    component.dynamicProperties.set([]);
    component.prepareCreateModal();
    expect(component.createModalShown()).toBe(true);
  });

  it('should not open create modal when 20 or more properties exist', () => {
    component.createModalShown.set(false);
    component.dynamicProperties.set(
      Array.from({ length: 20 }, (_, i) => ({
        id: `dp${i}`,
        title: { en: `Prop ${i}` },
        propertyType:
          'TEXT_FIELD' as DynamicPropertyDefinition.PropertyTypeEnum,
      }))
    );
    component.prepareCreateModal();
    expect(component.createModalShown()).toBe(false);
  });
});
