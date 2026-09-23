import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroup,
  InterestGroupService,
  KeywordsService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { KeywordsComponent } from './keywords.component';

const mockKeywords: SelectableKeyword[] = [
  { id: '1', title: { en: 'Keyword 1' } },
  { id: '2', title: { en: 'Keyword 2' } },
];

const mockIg: InterestGroup = {
  id: 'ig1',
  name: 'Test IG',
  permissions: {},
  libraryId: 'lib1',
};

const mockLibrary: ModelNode = {
  id: 'lib1',
  permissions: { LibAdmin: 'ALLOWED' },
};

describe('KeywordsComponent', () => {
  let component: KeywordsComponent;
  const paramsSubject = new Subject<Record<string, string>>();

  const mockKeywordsService = {
    getKeywordDefinitionsAsync: vi.fn().mockResolvedValue(mockKeywords),
  };
  const mockGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
  };
  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockLibrary),
  };
  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
    addSuccessMessage: vi.fn(),
  };
  const mockPermEvalService = {
    isLibAdmin: vi.fn().mockReturnValue(true),
  };
  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KeywordsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: KeywordsService, useValue: mockKeywordsService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: BASE_PATH, useValue: 'http://localhost' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(KeywordsComponent, {
        set: {
          imports: [TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
          template: '',
        },
      })
      .overrideProvider(TranslocoModule, { useValue: {} })
      .compileComponents();

    const fixture = TestBed.createComponent(KeywordsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load keywords on route params change', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toEqual(mockKeywords);
    });
    expect(component.nodeId()).toBe('ig1');
    expect(component.currentIg).toEqual(mockIg);
    expect(component.currentLibrary()).toEqual(mockLibrary);
    expect(component.loading()).toBe(false);
  });

  it('should show error message when nodeId is undefined', () => {
    paramsSubject.next({} as Record<string, string>);
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
  });

  it('should reset loading and show error toast when the load fails', async () => {
    mockUiMessageService.addErrorMessage.mockClear();
    mockGroupService.getInterestGroupAsync.mockRejectedValueOnce(
      new Error('network error')
    );

    paramsSubject.next({ id: 'ig-err' });

    await vi.waitFor(() => {
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });
    expect(component.loading()).toBe(false);
  });

  it('should toggle select all keywords', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    component.toggleSelect();
    expect(component.allSelected()).toBe(true);
    expect(component.keywords().every((k) => k.selected === true)).toBe(true);
    expect(component.selection()).toHaveLength(2);

    component.toggleSelect();
    expect(component.allSelected()).toBe(false);
    expect(component.keywords().every((k) => k.selected === false)).toBe(true);
    expect(component.selection()).toHaveLength(0);
  });

  it('should toggle individual keyword selection', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    component.toggleSelected(component.keywords()[0]);
    expect(component.keywords()[0].selected).toBe(true);
    expect(component.selection()).toHaveLength(1);

    component.toggleSelected(component.keywords()[0]);
    expect(component.keywords()[0].selected).toBe(false);
    expect(component.selection()).toHaveLength(0);
  });

  it('should refresh after keyword deletion on success', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    mockKeywordsService.getKeywordDefinitionsAsync.mockClear();
    await component.afterKeywordDeletion({
      result: ActionResult.SUCCEED,
      type: ActionType.DELETE_KEYWORD,
    });
    expect(mockKeywordsService.getKeywordDefinitionsAsync).toHaveBeenCalled();
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should not refresh after keyword deletion on cancel', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    mockKeywordsService.getKeywordDefinitionsAsync.mockClear();
    await component.afterKeywordDeletion({
      result: ActionResult.CANCELED,
      type: ActionType.DELETE_KEYWORD,
    });
    expect(
      mockKeywordsService.getKeywordDefinitionsAsync
    ).not.toHaveBeenCalled();
  });

  it('should show and hide multiple delete wizard', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    component.showDeleteAllModal();
    expect(component.showMultipleDeleteWizard()).toBe(true);

    await component.refreshAfterAllDeletion({
      result: ActionResult.SUCCEED,
      type: ActionType.DELETE_ALL,
    });
    expect(component.showMultipleDeleteWizard()).toBe(false);
    expect(component.selection()).toEqual([]);
  });

  it('should refresh after creation on success', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.keywords()).toHaveLength(2);
    });

    component.showCreateModal = true;
    await component.refreshAfterCreation({
      result: ActionResult.SUCCEED,
      type: ActionType.CREATE_KEYWORD,
    });
    expect(component.showCreateModal).toBe(false);
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should set selectedKeyword and show create modal on update', () => {
    const keyword: SelectableKeyword = { id: '1', title: { en: 'K1' } };
    component.showUpdateKeyword(keyword);
    expect(component.showCreateModal).toBe(true);
    expect(component.selectedKeyword).toBe(keyword);
  });

  it('should return true for isLibAdmin when library has admin permission', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.currentLibrary()).toBeDefined();
    });
    expect(component.isLibAdmin()).toBe(true);
    expect(mockPermEvalService.isLibAdmin).toHaveBeenCalledWith(mockLibrary);
  });

  it('should return false for isLibAdmin when no library', () => {
    expect(component.isLibAdmin()).toBe(false);
  });

  it('should call saveAsService.saveUrlAs on bulkDownload', async () => {
    paramsSubject.next({ id: 'ig1' });
    await vi.waitFor(() => {
      expect(component.currentIg).toBeDefined();
    });

    component.bulkDownload();
    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
      'http://localhost/groups/ig1/keywords/bulk',
      'Keywords.Test IG.xls'
    );
  });
});
