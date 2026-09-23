import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ImportKeywordComponent } from './import-keyword.component';

describe('ImportKeywordComponent', () => {
  let component: ImportKeywordComponent;
  let fixture: ComponentFixture<ImportKeywordComponent>;
  const mockKeywordsService = {
    postBulkKeywordDefinitions: vi.fn().mockReturnValue(of(undefined)),
    postBulkKeywordDefinitionsAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportKeywordComponent],
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

    fixture = TestBed.createComponent(ImportKeywordComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('parentIgId', 'ig-123');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('cancelWizard', () => {
    it('should emit modalHide with CANCELED result and clear file', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.fileToUpload.set(new File([''], 'test.csv'));

      component.cancelWizard();

      expect(component.fileToUpload()).toBeUndefined();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.IMPORT_KEYWORD,
      });
    });
  });

  describe('drop', () => {
    it('should set fileToUpload from dropped files', () => {
      const file = new File(['content'], 'keywords.csv');
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: { files: [file] },
      } as unknown as DragEvent;

      component.drop(event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.fileToUpload()).toBe(file);
    });

    it('should not set fileToUpload when dataTransfer is null', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: null,
      } as unknown as DragEvent;

      component.drop(event);

      expect(component.fileToUpload()).toBeUndefined();
    });
  });

  describe('fileChangeEvent', () => {
    it('should set fileToUpload from input event', () => {
      const file = new File(['content'], 'keywords.csv');
      const event = {
        target: { files: [file] },
      } as unknown as Event;

      component.fileChangeEvent(event);

      expect(component.fileToUpload()).toBe(file);
    });
  });

  describe('import', () => {
    it('should call postBulkKeywordDefinitions and emit SUCCEED', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      const file = new File(['content'], 'keywords.csv');
      component.fileToUpload.set(file);

      await component.import();

      expect(
        mockKeywordsService.postBulkKeywordDefinitionsAsync
      ).toHaveBeenCalledWith({ id: 'ig-123', fileData: file });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.IMPORT_KEYWORD,
      });
      expect(component.uploading()).toBe(false);
      expect(component.fileToUpload()).toBeUndefined();
    });

    it('should emit SUCCEED without calling service when no file', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.fileToUpload.set(undefined);

      await component.import();

      expect(
        mockKeywordsService.postBulkKeywordDefinitionsAsync
      ).not.toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.IMPORT_KEYWORD,
      });
    });

    it('should emit FAILED when service throws', async () => {
      mockKeywordsService.postBulkKeywordDefinitionsAsync.mockRejectedValue(
        new Error('fail')
      );
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.fileToUpload.set(new File(['content'], 'keywords.csv'));

      await component.import();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.FAILED,
        type: ActionType.IMPORT_KEYWORD,
      });
      expect(component.uploading()).toBe(false);
    });
  });
});
