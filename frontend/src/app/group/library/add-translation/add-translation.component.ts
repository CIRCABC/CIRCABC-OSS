import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ContentService,
  Node as ModelNode,
  NodesService,
  Translations,
  TranslationsService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that lets a user add a translation to an
 * existing library document node.
 *
 * The component renders a translation form which supports two mutually
 * exclusive modes:
 * - `FILE_UPLOAD`: the user picks a target language and uploads a translated
 *   file which is posted to the backend.
 * - `MACHINE_TRANSLATION`: the user picks a target language and requests an
 *   automated (machine) translation of the source document.
 *
 * It displays the source document context, a language selector (with already
 * translated languages disabled), file input, and loading/processing
 * indicators. The target node id is resolved from the `nodeId` route
 * parameter.
 *
 * Key collaborators:
 * - {@link NodesService} to fetch the source node metadata.
 * - {@link ContentService} to fetch existing translations of the node.
 * - {@link TranslationsService} to post file uploads and machine translation
 *   requests.
 * - {@link UiMessageService} to surface validation errors to the user.
 * - {@link TranslocoService} for i18n message resolution.
 * - {@link Location} to navigate back after submitting or cancelling.
 */
@Component({
  selector: 'cbc-add-translation',
  templateUrl: './add-translation.component.html',
  styleUrl: './add-translation.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReactiveFormsModule,
    LangSelectorComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddTranslationComponent implements OnInit {
  /** Builder used to create the reactive translation form. */
  private readonly fb = inject(FormBuilder);
  /** Provides access to the current route parameters (e.g. `nodeId`). */
  private readonly route = inject(ActivatedRoute);
  /** Retrieves node metadata for the document being translated. */
  private readonly nodesService = inject(NodesService);
  /** Posts file uploads and machine translation requests to the backend. */
  private readonly translationsService = inject(TranslationsService);
  /** Displays user-facing error/validation messages. */
  private readonly uiMessageService = inject(UiMessageService);
  private readonly loadingService = inject(LoadingService);
  /** Resolves translated i18n strings. */
  private readonly translateService = inject(TranslocoService);
  /** Fetches the set of existing translations for the current node. */
  private readonly contentService = inject(ContentService);
  /** Enables navigation back to the previous view after submit/cancel. */
  private readonly location = inject(Location);

  /** Reactive form holding the selected target language (`lang` control). */
  public addTranslationForm!: FormGroup;
  /** Identifier of the source node being translated (from the route). */
  public id!: string;
  /** The translated file selected by the user for upload. */
  public myfile!: File;
  /** Whether the source node and its translations are currently being loaded. */
  public readonly loading = signal(false);
  /** Whether a submit (upload or machine translation) is in progress. */
  public readonly processing = signal(false);
  /** The source document node metadata loaded from the backend. */
  public readonly currentNode = signal<ModelNode | undefined>(undefined);
  /** The existing translations of the source node, including the pivot. */
  public readonly translations = signal<Translations | undefined>(undefined);
  /** The currently active translation mode, or `undefined` if none selected. */
  private mode: 'FILE_UPLOAD' | 'MACHINE_TRANSLATION' | undefined;
  /** File extensions accepted for machine translation requests. */
  private readonly validExtensions = [
    'doc',
    'docx',
    'xls',
    'xlsx',
    'ppt',
    'pptx',
    'odt',
    'rtf',
    'txt',
    'html',
    'tmx',
    'xliff',
    'pdf',
  ];
  /** Locales that already have a translation and must be disabled in the selector. */
  public readonly disabledLangs = signal<string[]>([]);

  /**
   * Angular lifecycle hook. Subscribes to route parameters to load the target
   * node when `nodeId` changes and initializes the reactive form with an empty
   * `lang` control.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => this.loadNode(params.nodeId));
    this.addTranslationForm = this.fb.group({
      lang: [],
    });
  }

  /**
   * Loads the source node and its existing translations, then computes the
   * list of languages that should be disabled in the language selector.
   *
   * @param id The identifier of the node to load and translate.
   * @returns A promise that resolves once loading and preparation completes.
   */
  async loadNode(id: string) {
    this.id = id;
    await this.loadingService.run(this.loading, async () => {
      this.currentNode.set(
        await this.nodesService.getNodeAsync({ id: this.id })
      );
      this.translations.set(
        await this.contentService.getTranslationsAsync({ id: this.id })
      );
      this.prepareDisabledLang();
    });
  }

  /**
   * Returns the number of existing translations, excluding the pivot document.
   *
   * @returns The translation count minus one (the pivot), or `0` when none exist.
   */
  getNbTranslations(): number {
    const translations = this.translations();
    if (translations?.translations) {
      return translations.translations.length - 1;
    }

    return 0;
  }

  /**
   * Handles the file input change event by storing the first selected file.
   *
   * @param event The DOM change event emitted by the file input element.
   */
  fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;
    this.myfile = filesList[0];
  }

  /** Switches the component into file upload mode. */
  setModeFileUpload() {
    this.mode = 'FILE_UPLOAD';
  }

  /**
   * @returns `true` when the component is in file upload mode.
   */
  isModeFileUpload() {
    return this.mode === 'FILE_UPLOAD';
  }

  /** Switches the component into machine translation mode. */
  setModeMachineTranslation() {
    this.mode = 'MACHINE_TRANSLATION';
  }

  /**
   * @returns `true` when the component is in machine translation mode.
   */
  isModeMachineTranslation() {
    return this.mode === 'MACHINE_TRANSLATION';
  }
  /**
   * @returns `true` when a translation mode has been selected.
   */
  isModeSet() {
    return this.mode !== undefined;
  }

  /**
   * Submits a file-upload translation. Validates that both a language and a
   * file are selected, posts the translated file to the backend, then
   * navigates back. Sets {@link processing} during the request.
   *
   * @returns A promise that resolves once the upload completes or validation fails.
   */
  public async submit() {
    if (!(await this.isLanguageSelected())) {
      return;
    }
    if (!(await this.isFileSelected())) {
      return;
    }
    this.processing.set(true);
    await this.translationsService.postTranslationAsync({
      id: this.id,
      lang: this.addTranslationForm.value.lang,
      fileData: this.myfile,
    });
    this.location.back();
    this.processing.set(false);
  }

  /**
   * Submits a machine translation request. Validates that a language is
   * selected and the source file has a supported extension, requests the
   * machine translation from the backend, then navigates back. Sets
   * {@link processing} during the request.
   *
   * @returns A promise that resolves once the request completes or validation fails.
   */
  public async submitMachineTranlationRequest() {
    if (!(await this.isLanguageSelected())) {
      return;
    }
    if (!(await this.isValidExtension())) {
      return;
    }
    this.processing.set(true);
    await this.translationsService.postMachineTranslationAsync({
      id: this.id,
      language: this.addTranslationForm.value.lang,
      notify: true,
    });
    this.processing.set(false);
    this.location.back();
  }

  /** Cancels the operation and navigates back to the previous view. */
  public cancel() {
    this.location.back();
  }

  /**
   * Rebuilds {@link disabledLangs} from the existing translations so that
   * already-translated locales cannot be selected again.
   */
  public prepareDisabledLang() {
    const disabledLangs: string[] = [];

    const translations = this.translations();
    if (translations?.translations) {
      for (const tr of translations.translations) {
        if (tr.properties?.locale) {
          disabledLangs.push(tr.properties.locale);
        }
      }
    }

    this.disabledLangs.set(disabledLangs);
  }

  /**
   * Provides the i18n key for the processing label based on the active mode.
   *
   * @returns The translation key for the current mode, or an empty string when
   * no mode is set.
   */
  get labelProcessing(): string {
    let result = '';
    if (this.mode === 'FILE_UPLOAD') {
      result = 'label.saving';
    } else if (this.mode === 'MACHINE_TRANSLATION') {
      result = 'translations.request.machine.translation.sending';
    }
    return result;
  }

  /**
   * Provides the locale of the pivot (source) translation.
   *
   * @returns The pivot locale, or an empty string when unavailable.
   */
  get pivotLocale(): string {
    const pivot = this.translations()?.pivot;
    if (pivot?.properties) {
      return pivot.properties.locale;
    }
    return '';
  }

  /**
   * Validates that a target language has been selected, surfacing an error
   * message otherwise.
   *
   * @returns A promise resolving to `true` when a language is selected,
   * `false` otherwise.
   */
  private async isLanguageSelected() {
    if (this.addTranslationForm.controls.lang.value === null) {
      const txt = this.translateService.translate('validation.selectLanguage');
      this.uiMessageService.addErrorMessage(txt);
      return false;
    }
    return true;
  }

  /**
   * @returns `true` when a non-empty target language is selected in the form.
   */
  public hasLanguageSelected() {
    if (
      this.addTranslationForm.value.lang === null ||
      this.addTranslationForm.value.lang === ''
    ) {
      return false;
    }
    return true;
  }
  /**
   * Validates that a file has been selected for upload, surfacing an error
   * message otherwise.
   *
   * @returns A promise resolving to `true` when a file is selected, `false`
   * otherwise.
   */
  private async isFileSelected() {
    if (this.myfile === undefined || this.myfile === null) {
      const txt = this.translateService.translate('validation.selectFile');
      this.uiMessageService.addErrorMessage(txt);
      return false;
    }
    return true;
  }

  /**
   * @returns `true` when a file has been selected by the user.
   */
  public hasFileSelected() {
    if (this.myfile === undefined || this.myfile === null) {
      return false;
    }
    return true;
  }

  /**
   * Validates that the source document's file extension is supported for
   * machine translation, surfacing an error message with the list of valid
   * extensions otherwise.
   *
   * @returns A promise resolving to `true` when the extension is valid (or when
   * no extension can be determined), `false` otherwise.
   */
  private async isValidExtension() {
    const fileName = this.currentNode()?.name;
    if (fileName) {
      const lastIndex = fileName.lastIndexOf('.');
      if (lastIndex > -1) {
        const extension = fileName.substring(lastIndex + 1);
        if (this.validExtensions.includes(extension.toLowerCase())) {
          return true;
        }
        const txt = this.translateService.translate(
          'validation.invalidFileType',
          {
            validFileExtensions: this.validExtensions.toString(),
          }
        );
        this.uiMessageService.addErrorMessage(txt);
        return false;
      }
    }
    return true;
  }
}
