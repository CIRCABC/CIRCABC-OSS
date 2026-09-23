import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionService } from 'app/action-result/action.service';
import { ActionResult, ActionType } from 'app/action-result/index';
import {
  CategoryService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { SERVER_URL } from 'app/core/variables';
import { UrlHelperService } from 'app/core/url-helper.service';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategoryCustomisationComponent } from './category-customisation.component';

describe('CategoryCustomisationComponent', () => {
  let component: CategoryCustomisationComponent;
  let fixture: ComponentFixture<CategoryCustomisationComponent>;

  const paramsSubject = new Subject<{ id: string }>();

  const mockNode: ModelNode = {
    id: 'cat-1',
    name: 'Test Category',
    properties: { logoRef: 'logo-1' },
  };

  const mockLogos: ModelNode[] = [
    { id: 'logo-1', name: 'logo1.png' },
    { id: 'logo-2', name: 'logo2.png' },
  ];

  const mockCategoryService = {
    getCategoryLogoByCategoryIdAsync: vi.fn().mockResolvedValue(mockLogos),
    selectCategoryLogoByLogoId: vi.fn().mockReturnValue(of(null)),
    deleteCategoryLogoByLogoId: vi.fn().mockReturnValue(of([mockLogos[1]])),
    selectCategoryLogoByLogoIdAsync: vi.fn().mockResolvedValue(null),
    deleteCategoryLogoByLogoIdAsync: vi.fn().mockResolvedValue([mockLogos[1]]),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };

  const mockActionService = {
    propagateActionFinished: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryCustomisationComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: ActionService, useValue: mockActionService },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
        {
          provide: DownloadUtilService,
          useValue: { getDownloadUrl: vi.fn().mockReturnValue('http://mock') },
        },
        {
          provide: UrlHelperService,
          useValue: { get: vi.fn().mockReturnValue(of('blob:mock')) },
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

    fixture = TestBed.createComponent(CategoryCustomisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load category and logos on route params change', async () => {
    paramsSubject.next({ id: 'cat-1' });
    // Allow async operations inside the subscription to complete
    await new Promise((resolve) => setTimeout(resolve));

    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'cat-1' });
    expect(
      mockCategoryService.getCategoryLogoByCategoryIdAsync
    ).toHaveBeenCalledWith({ id: 'cat-1' });
    expect(component.category()).toEqual(mockNode);
    expect(component.logos()).toEqual(mockLogos);
    expect(component.categoryId()).toBe('cat-1');
  });

  it('should return true from isSelected when logo id matches logoRef', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(component.isSelected('logo-1')).toBe(true);
  });

  it('should return false from isSelected when logo id does not match', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(component.isSelected('logo-999')).toBe(false);
  });

  it('should return false from isSelected when id is undefined', () => {
    expect(component.isSelected(undefined)).toBe(false);
  });

  it('should select a logo and propagate action', async () => {
    component.categoryId.set('cat-1');
    component.category.set(mockNode);

    await component.select('logo-2');

    expect(
      mockCategoryService.selectCategoryLogoByLogoIdAsync
    ).toHaveBeenCalledWith({ id: 'cat-1', logoId: 'logo-2' });
    expect(mockActionService.propagateActionFinished).toHaveBeenCalledWith({
      type: ActionType.ADD_CATEGORY_LOGO,
      node: { id: 'cat-1' },
      result: ActionResult.SUCCEED,
    });
  });

  it('should not select when id is undefined', async () => {
    component.categoryId.set('cat-1');
    await component.select(undefined);
    expect(
      mockCategoryService.selectCategoryLogoByLogoId
    ).not.toHaveBeenCalled();
  });

  it('should delete a logo', async () => {
    component.categoryId.set('cat-1');

    await component.delete('logo-1');

    expect(
      mockCategoryService.deleteCategoryLogoByLogoIdAsync
    ).toHaveBeenCalledWith({ id: 'cat-1', logoId: 'logo-1' });
    expect(component.logos()).toEqual([mockLogos[1]]);
  });

  it('should not delete when id is undefined', async () => {
    component.categoryId.set('cat-1');
    await component.delete(undefined);
    expect(
      mockCategoryService.deleteCategoryLogoByLogoId
    ).not.toHaveBeenCalled();
  });

  it('should refresh logos on successful action result', async () => {
    component.categoryId.set('cat-1');
    component.showUploadModal.set(true);

    await component.refresh({
      result: ActionResult.SUCCEED,
      type: ActionType.ADD_CATEGORY_LOGO,
    });

    expect(
      mockCategoryService.getCategoryLogoByCategoryIdAsync
    ).toHaveBeenCalledWith({ id: 'cat-1' });
    expect(component.showUploadModal()).toBe(false);
  });

  it('should close modal but not refresh on failed action result', async () => {
    component.categoryId.set('cat-1');
    component.showUploadModal.set(true);
    mockCategoryService.getCategoryLogoByCategoryIdAsync.mockClear();

    await component.refresh({
      result: ActionResult.FAILED,
      type: ActionType.ADD_CATEGORY_LOGO,
    });

    expect(
      mockCategoryService.getCategoryLogoByCategoryIdAsync
    ).not.toHaveBeenCalled();
    expect(component.showUploadModal()).toBe(false);
  });
});
