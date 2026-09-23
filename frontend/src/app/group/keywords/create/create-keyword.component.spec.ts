import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateKeywordComponent } from './create-keyword.component';

const mockKeywordsService = {
  postKeywordDefinition: vi.fn().mockReturnValue(of({})),
  putKeywordDefinition: vi.fn().mockReturnValue(of({})),
  postKeywordDefinitionAsync: vi.fn().mockResolvedValue({}),
  putKeywordDefinitionAsync: vi.fn().mockResolvedValue({}),
};

describe('CreateKeywordComponent', () => {
  let component: CreateKeywordComponent;
  let componentRef: ComponentRef<CreateKeywordComponent>;
  let fixture: ComponentFixture<CreateKeywordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateKeywordComponent, ReactiveFormsModule],
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

    fixture = TestBed.createComponent(CreateKeywordComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('showModal', true);
    componentRef.setInput('parentIgId', 'ig-123');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with empty title', () => {
    expect(component.createKeywordForm).toBeDefined();
    expect(component.createKeywordForm.controls['title']).toBeDefined();
  });

  it('should expose titleControl', () => {
    expect(component.titleControl).toBe(
      component.createKeywordForm.controls['title']
    );
  });

  describe('create', () => {
    it('should call postKeywordDefinition and emit success', async () => {
      component.createKeywordForm.controls['title'].setValue({ en: 'Test' });

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.create();

      expect(
        mockKeywordsService.postKeywordDefinitionAsync
      ).toHaveBeenCalledWith({
        id: 'ig-123',
        keywordDefinition: expect.objectContaining({
          title: expect.objectContaining({ en: 'Test' }),
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.CREATE_KEYWORD,
      });
      expect(component.creating()).toBe(false);
    });

    it('should reset form after create', async () => {
      component.createKeywordForm.controls['title'].setValue({ en: 'Test' });
      await component.create();
      expect(component.createKeywordForm.controls['title'].pristine).toBe(true);
      expect(component.createKeywordForm.controls['title'].untouched).toBe(
        true
      );
    });
  });

  describe('update', () => {
    it('should call putKeywordDefinition and emit success', async () => {
      componentRef.setInput('keyword', {
        id: 'kw-1',
        title: { en: 'Old' },
      });
      fixture.detectChanges();

      component.createKeywordForm.controls['title'].setValue({ en: 'New' });
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.update();

      expect(
        mockKeywordsService.putKeywordDefinitionAsync
      ).toHaveBeenCalledWith({
        keywordId: 'kw-1',
        keywordDefinition: expect.objectContaining({
          id: 'kw-1',
          title: expect.objectContaining({ en: 'New' }),
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.UPDATE_KEYWORD,
      });
      expect(component.creating()).toBe(false);
    });

    it('should emit FAILED when putKeywordDefinition returns undefined', async () => {
      mockKeywordsService.putKeywordDefinitionAsync.mockResolvedValueOnce(
        undefined
      );
      componentRef.setInput('keyword', {
        id: 'kw-1',
        title: { en: 'Old' },
      });
      fixture.detectChanges();

      component.createKeywordForm.controls['title'].setValue({ en: 'New' });
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.update();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.FAILED,
        type: ActionType.UPDATE_KEYWORD,
      });
    });

    it('should emit FAILED when keyword has no id', async () => {
      componentRef.setInput('keyword', { title: { en: 'NoId' } });
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.update();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.FAILED,
        type: ActionType.UPDATE_KEYWORD,
      });
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED with CREATE_KEYWORD type when keyword is set', () => {
      componentRef.setInput('keyword', { id: 'kw-1', title: { en: 'X' } });
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.CREATE_KEYWORD,
      });
    });

    it('should emit CANCELED with UPDATE_KEYWORD type when no keyword', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.UPDATE_KEYWORD,
      });
    });

    it('should reset form on cancel', () => {
      component.createKeywordForm.controls['title'].setValue({ en: 'Dirty' });
      component.createKeywordForm.controls['title'].markAsDirty();
      component.cancelWizard();
      expect(component.createKeywordForm.controls['title'].pristine).toBe(true);
      expect(component.createKeywordForm.controls['title'].untouched).toBe(
        true
      );
    });
  });

  describe('ngOnChanges', () => {
    it('should update title when keyword input changes', () => {
      const titleValue = { en: 'Updated' };
      component.ngOnChanges({
        keyword: {
          currentValue: { id: 'kw-1', title: titleValue },
          previousValue: undefined,
          firstChange: true,
          isFirstChange: () => true,
        },
      });
      expect(component.createKeywordForm.controls['title'].value).toEqual(
        expect.objectContaining({ en: 'Updated' })
      );
    });
  });
});
