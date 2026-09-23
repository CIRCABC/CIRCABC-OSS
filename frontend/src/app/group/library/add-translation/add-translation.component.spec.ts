import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ContentService,
  Node as ModelNode,
  NodesService,
  Translations,
  TranslationsService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddTranslationComponent } from './add-translation.component';

describe('AddTranslationComponent', () => {
  let component: AddTranslationComponent;

  const mockNode: ModelNode = {
    id: '123',
    name: 'test.docx',
    properties: { locale: 'en' },
  };
  const mockTranslations = {
    pivot: { properties: { locale: 'en' } },
    translations: [
      { properties: { locale: 'en' } },
      { properties: { locale: 'fr' } },
    ],
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };
  const mockContentService = {
    getTranslationsAsync: vi.fn().mockResolvedValue(mockTranslations),
  };
  const mockTranslationsService = {
    postTranslation: vi.fn().mockReturnValue(of({})),
    postMachineTranslation: vi.fn().mockReturnValue(of({})),
    postTranslationAsync: vi.fn().mockResolvedValue({}),
    postMachineTranslationAsync: vi.fn().mockResolvedValue({}),
  };
  const mockUiMessageService = { addErrorMessage: vi.fn() };
  const mockLocation = { back: vi.fn() };
  const mockRoute = { params: of({ nodeId: '123' }) };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AddTranslationComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: NodesService, useValue: mockNodesService },
        { provide: ContentService, useValue: mockContentService },
        { provide: TranslationsService, useValue: mockTranslationsService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Location, useValue: mockLocation },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(AddTranslationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load node on init', () => {
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: '123' });
    expect(mockContentService.getTranslationsAsync).toHaveBeenCalledWith({
      id: '123',
    });
    expect(component.currentNode()).toEqual(mockNode);
    expect(component.loading()).toBe(false);
  });

  it('should compute disabled languages from translations', () => {
    expect(component.disabledLangs()).toEqual(['en', 'fr']);
  });

  it('should return number of translations minus pivot', () => {
    expect(component.getNbTranslations()).toBe(1);
  });

  it('should return 0 translations when none exist', () => {
    component.translations.set({} as Translations);
    expect(component.getNbTranslations()).toBe(0);
  });

  it('should set and check FILE_UPLOAD mode', () => {
    expect(component.isModeSet()).toBe(false);
    component.setModeFileUpload();
    expect(component.isModeFileUpload()).toBe(true);
    expect(component.isModeMachineTranslation()).toBe(false);
    expect(component.isModeSet()).toBe(true);
  });

  it('should set and check MACHINE_TRANSLATION mode', () => {
    component.setModeMachineTranslation();
    expect(component.isModeMachineTranslation()).toBe(true);
    expect(component.isModeFileUpload()).toBe(false);
  });

  it('should return pivotLocale from translations', () => {
    expect(component.pivotLocale).toBe('en');
  });

  it('should return empty string for pivotLocale when no pivot properties', () => {
    component.translations.set({ pivot: {} } as Translations);
    expect(component.pivotLocale).toBe('');
  });

  it('should report hasLanguageSelected as false when no lang selected', () => {
    expect(component.hasLanguageSelected()).toBe(false);
  });

  it('should report hasFileSelected as false when no file set', () => {
    expect(component.hasFileSelected()).toBe(false);
  });

  it('should report hasFileSelected as true when file is set', () => {
    component.myfile = new File([''], 'test.txt');
    expect(component.hasFileSelected()).toBe(true);
  });

  it('should call location.back on cancel', () => {
    component.cancel();
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should show error and not submit when no language selected', async () => {
    component.myfile = new File([''], 'test.txt');
    await component.submit();
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    expect(mockTranslationsService.postTranslationAsync).not.toHaveBeenCalled();
  });

  it('should show error and not submit when no file selected', async () => {
    component.addTranslationForm.controls['lang'].setValue('fr');
    await component.submit();
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    expect(mockTranslationsService.postTranslationAsync).not.toHaveBeenCalled();
  });

  it('should submit translation and navigate back', async () => {
    component.addTranslationForm.controls['lang'].setValue('de');
    component.myfile = new File(['content'], 'doc.docx');
    await component.submit();
    expect(mockTranslationsService.postTranslationAsync).toHaveBeenCalledWith({
      id: '123',
      lang: 'de',
      fileData: component.myfile,
    });
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should submit machine translation and navigate back', async () => {
    component.addTranslationForm.controls['lang'].setValue('de');
    await component.submitMachineTranlationRequest();
    expect(
      mockTranslationsService.postMachineTranslationAsync
    ).toHaveBeenCalledWith({
      id: '123',
      language: 'de',
      notify: true,
    });
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should reject machine translation for invalid file extension', async () => {
    component.currentNode.set({ name: 'file.zip' });
    component.addTranslationForm.controls['lang'].setValue('de');
    await component.submitMachineTranlationRequest();
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    expect(
      mockTranslationsService.postMachineTranslationAsync
    ).not.toHaveBeenCalled();
  });

  it('should return correct labelProcessing based on mode', () => {
    expect(component.labelProcessing).toBe('');
    component.setModeFileUpload();
    expect(component.labelProcessing).toBe('label.saving');
    component.setModeMachineTranslation();
    expect(component.labelProcessing).toBe(
      'translations.request.machine.translation.sending'
    );
  });
});
