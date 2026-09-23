import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { KeywordsService, Node as ModelNode } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddKeywordComponent } from './add-keyword.component';

const mockKeywordsService = {
  getKeywordsAsync: vi
    .fn()
    .mockImplementation(() =>
      Promise.resolve([
        { id: 'kw1', title: { en: 'Used' } },
      ] as SelectableKeyword[])
    ),
  getKeywordDefinitionsAsync: vi.fn().mockImplementation(() =>
    Promise.resolve([
      { id: 'kw1', title: { en: 'Used' } },
      { id: 'kw2', title: { en: 'Available1' } },
      { id: 'kw3', title: { en: 'Available2' } },
    ] as SelectableKeyword[])
  ),
  postKeyword: vi.fn().mockReturnValue(of(undefined)),
  postKeywordAsync: vi.fn().mockResolvedValue(undefined),
};

describe('AddKeywordComponent', () => {
  let component: AddKeywordComponent;
  let componentRef: ComponentRef<AddKeywordComponent>;
  let fixture: ComponentFixture<AddKeywordComponent>;

  const groupNode: ModelNode = { id: 'group-1' };
  const documentNode: ModelNode = { id: 'doc-1' };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AddKeywordComponent],
      providers: [
        { provide: KeywordsService, useValue: mockKeywordsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddKeywordComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupNode', groupNode);
    componentRef.setInput('documentNode', documentNode);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load available keywords on init filtering out used ones', async () => {
    await component.loadAvailableKeywords();

    expect(mockKeywordsService.getKeywordsAsync).toHaveBeenCalledWith({
      id: 'doc-1',
    });
    expect(mockKeywordsService.getKeywordDefinitionsAsync).toHaveBeenCalledWith(
      {
        id: 'group-1',
      }
    );
    expect(component.availableKeywords()).toHaveLength(2);
    expect(component.availableKeywords()[0].id).toBe('kw2');
    expect(component.availableKeywords()[1].id).toBe('kw3');
  });

  it('should not load keywords when ids are missing', async () => {
    componentRef.setInput('groupNode', {});
    componentRef.setInput('documentNode', {});
    vi.clearAllMocks();

    await component.loadAvailableKeywords();

    expect(mockKeywordsService.getKeywordsAsync).not.toHaveBeenCalled();
    expect(component.availableKeywords()).toEqual([]);
  });

  it('should open modal and reload keywords', async () => {
    await component.openModal();

    expect(component.showModal()).toBe(true);
    expect(mockKeywordsService.getKeywordsAsync).toHaveBeenCalled();
  });

  it('should toggle keyword selection', async () => {
    await component.loadAvailableKeywords();

    const keyword = component.availableKeywords()[0];
    expect(keyword.selected).toBeFalsy();

    component.toggleSelected(keyword);
    expect(component.availableKeywords()[0].selected).toBe(true);

    component.toggleSelected(keyword);
    expect(component.availableKeywords()[0].selected).toBe(false);
  });

  it('should return true from hasKeywords when keywords exist', async () => {
    await component.loadAvailableKeywords();
    expect(component.hasKeywords()).toBe(true);
  });

  it('should return false from hasKeywords when no keywords', () => {
    component.availableKeywords.set([]);
    expect(component.hasKeywords()).toBe(false);
  });

  it('should close modal on cancelWizard', () => {
    component.showModal.set(true);
    component.cancelWizard('cancel');
    expect(component.showModal()).toBe(false);
  });

  it('should add selected keywords and emit result', async () => {
    await component.loadAvailableKeywords();
    const keywordToAdd = component.availableKeywords()[0];
    keywordToAdd.selected = true;

    const emitSpy = vi.spyOn(component.addedKeyword, 'emit');

    await component.add();

    expect(mockKeywordsService.postKeywordAsync).toHaveBeenCalledWith({
      id: 'doc-1',
      keywordDefinition: keywordToAdd,
    });
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.ADD_KEYWORD,
      result: ActionResult.SUCCEED,
    });
    expect(component.showModal()).toBe(false);
    expect(component.adding()).toBe(false);
  });

  it('should not emit when no keywords are selected', async () => {
    await component.loadAvailableKeywords();
    const emitSpy = vi.spyOn(component.addedKeyword, 'emit');

    await component.add();

    expect(mockKeywordsService.postKeywordAsync).not.toHaveBeenCalled();
    expect(emitSpy).not.toHaveBeenCalled();
    expect(component.showModal()).toBe(false);
  });
});
