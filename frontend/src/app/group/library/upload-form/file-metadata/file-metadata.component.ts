import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  OnChanges,
  output,
  resource,
  SimpleChange,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  KeywordDefinition,
  KeywordsService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { nameValidator } from 'app/core/validation.service';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { environment } from 'environments/environment';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

/**
 * Metadata editing form used within the library upload workflow.
 *
 * Renders the reactive form that captures the metadata of a single file
 * being uploaded (name, multilingual title/description, author, reference,
 * security ranking, status, expiration date and keywords) together with any
 * group-specific dynamic properties. It also exposes sub-forms to mark the
 * file as a pivot (source language) document or as a translation of an
 * existing pivot, and a keyword picker with search/filtering.
 *
 * The component keeps the bound {@link FileUploadItem} in sync with the form:
 * every relevant change is written back onto the item and re-emitted through
 * {@link FileMetadataComponent.fileChange}. Behaviour is adapted for the
 * "olaf" release (forced sensitive ranking and a computed expiration date).
 *
 * Key collaborators: {@link KeywordsService} and
 * {@link DynamicPropertiesService} (generated CIRCABC API clients) for
 * loading keyword and dynamic-property definitions, {@link LoginService} for
 * the current user's UI language, and {@link ActivatedRoute} for resolving
 * the current group node id.
 */
@Component({
  selector: 'cbc-file-metadata',
  templateUrl: './file-metadata.component.html',
  styleUrl: './file-metadata.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RichTextEditorComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    LangSelectorComponent,
    I18nPipe,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    KeywordTagComponent,
    TranslocoModule,
  ],
})
export class FileMetadataComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly keywordsService = inject(KeywordsService);
  private readonly route = inject(ActivatedRoute);
  private readonly loginService = inject(LoginService);
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);

  /** The file whose metadata is being edited by this form. */
  readonly file = input<FileUploadItem>();
  /** Files already marked as pivots, used to build the translation options and disabled languages. */
  readonly pivots = input<FileUploadItem[]>([]);
  /** Existing translation files, used to compute which languages are already taken for a pivot. */
  readonly translations = input<FileUploadItem[]>([]);
  /** Emits the updated {@link FileUploadItem} whenever its metadata changes. */
  readonly fileChange = output<FileUploadItem>();

  /** Main reactive form holding the file's editable metadata and dynamic properties. */
  public fileForm: FormGroup;
  /** Form backing the keyword search/filter field. */
  public filterForm: FormGroup;
  /** Form used when marking the file as a pivot (source language) document. */
  public pivotForm: FormGroup;
  /** Form used when defining the file as a translation of an existing pivot. */
  public translationForm: FormGroup;
  /** Current view step of the component (e.g. `'main'`). */
  public step = 'main';
  /** Identifier of the current group node, resolved from the route params. */
  public groupNodeId = signal<string>('');
  /** Keywords currently selected for the file. */
  public selectedKeywords: KeywordDefinition[] = [];
  /** Languages that are disabled in the translation selector because they are already used. */
  public disabledLangs = signal<string[]>([]);
  /** Minimum selectable date for date pickers (today). */
  public minDate = new Date();
  /** Computed expiration date enforced for the "olaf" release (one month ahead, end of day). */
  public expirationDateOlaf: Date = new Date();
  /** Names of the dynamic-property controls already added to {@link fileForm}. */
  private readonly addedDynamicPropertyControls = new Set<string>();

  /**
   * Loads the group-specific dynamic property definitions for
   * {@link groupNodeId}. Stays idle until the group node id is known.
   */
  private readonly dynamicPropertiesResource = resource({
    params: () => this.groupNodeId() || undefined,
    loader: ({ params: id }) =>
      this.dynamicPropertiesService.getDynamicPropertyDefinitionsAsync({
        id,
      }),
  });

  /** Group-specific dynamic property definitions added as extra form controls. */
  public dynamicProperties = computed(
    () => this.dynamicPropertiesResource.value() ?? []
  );

  /**
   * Loads the keyword definitions available for {@link groupNodeId}. Stays
   * idle until the group node id is known.
   */
  private readonly keywordsResource = resource({
    params: () => this.groupNodeId() || undefined,
    loader: ({ params: id }) =>
      this.keywordsService.getKeywordDefinitionsAsync({ id }),
  });

  /** All keyword definitions available for the current group. */
  public keywordDefinition = computed(
    () => this.keywordsResource.value() ?? []
  );

  /**
   * Builds the reactive forms, applies olaf-specific defaults, wires up
   * value-change subscriptions and resolves the group node id from the route.
   * Dynamic property definitions and keyword definitions are loaded reactively
   * via {@link dynamicPropertiesResource} and {@link keywordsResource}.
   */
  constructor() {
    this.route.params.subscribe((params) => {
      if (params?.id) {
        this.groupNodeId.set(params.id);
      }
    });

    this.fileForm = this.fb.group({
      name: ['', [Validators.required, nameValidator]],
      title: [],
      description: [],
      author: [],
      reference: [],
      expirationDate: [],
      securityRanking: ['NORMAL'],
      status: ['DRAFT'],
      keywords: [],
    });

    if (environment.circabcRelease === 'olaf') {
      this.expirationDateOlaf.setMonth(this.expirationDateOlaf.getMonth() + 1);
      this.expirationDateOlaf.setHours(23);
      this.expirationDateOlaf.setMinutes(59);
      this.expirationDateOlaf.setSeconds(59);
      this.fileForm.controls.securityRanking.setValue('SENSITIVE');
      this.fileForm.controls.expirationDate.setValue(this.expirationDateOlaf);
      this.fileForm.controls.expirationDate.disable();
    }

    this.fileForm.valueChanges
      .pipe(debounceTime(100), distinctUntilChanged())
      .subscribe((_data) => {
        this.updateFileProperties();
      });

    this.filterForm = this.fb.group({
      keywordSearch: [],
    });

    this.pivotForm = this.fb.group({
      pivotLang: [''],
    });

    this.translationForm = this.fb.group({
      pivotId: [''],
      translationLang: [''],
    });

    this.translationForm.controls.pivotId.valueChanges.subscribe((_value) => {
      this.getDisabledLang(_value);
      this.translationForm.controls.translationLang.setValue('');
    });

    // Adds a form control for every newly loaded dynamic property definition
    // and (re)patches the form from the currently bound file. Mirrors the
    // imperative "add control then patch" sequence the old ngOnInit performed
    // once dynamic properties resolved, but now runs as a side effect on the
    // imperative FormGroup API whenever the resource (re)loads.
    effect(() => {
      const dynamicProperties = this.dynamicProperties();
      for (const dynprop of dynamicProperties) {
        const key = this.getName(dynprop);
        if (!this.addedDynamicPropertyControls.has(key)) {
          this.fileForm.addControl(key, new FormControl());
          this.addedDynamicPropertyControls.add(key);
        }
      }

      const file = this.file();
      if (file) {
        this.patchForm(file);
      }
    });
  }

  /**
   * Reacts to changes of the {@link FileMetadataComponent.file} input and
   * dispatches to the appropriate handler depending on whether the file was
   * updated, cleared or newly set.
   *
   * @param changes The set of input changes provided by Angular.
   */
  ngOnChanges(changes: SimpleChanges) {
    const fileChange = changes.file;

    if (this.isFileUpdate(fileChange)) {
      this.handleFileUpdate(fileChange.currentValue);
    } else if (this.isFileCleared(fileChange)) {
      this.handleFileCleared();
    } else if (this.isNewFile(fileChange)) {
      this.handleNewFile();
    }
  }

  /**
   * Determines whether the change represents an update of an already present file.
   *
   * @param fileChange The change record for the `file` input.
   * @returns `true` when both the current and previous values are set.
   */
  private isFileUpdate(fileChange: SimpleChange | undefined): boolean {
    return !!(fileChange?.currentValue && fileChange.previousValue);
  }

  /**
   * Determines whether the change represents the file being cleared.
   *
   * @param fileChange The change record for the `file` input.
   * @returns `true` when the current value is undefined but a previous value existed.
   */
  private isFileCleared(fileChange: SimpleChange | undefined): boolean {
    return (
      fileChange?.currentValue === undefined &&
      fileChange?.previousValue !== undefined
    );
  }

  /**
   * Determines whether the change represents a brand new file being set.
   *
   * @param fileChange The change record for the `file` input.
   * @returns `true` when a current value is set and no previous value existed.
   */
  private isNewFile(fileChange: SimpleChange | undefined): boolean {
    return (
      fileChange?.currentValue !== undefined &&
      fileChange?.previousValue === undefined
    );
  }

  /**
   * Handles an update of an existing file by resetting the step and
   * re-synchronizing the form, keywords, pivot and translation sub-forms.
   *
   * @param file The updated file.
   */
  private handleFileUpdate(file: FileUploadItem) {
    this.step = 'main';
    this.patchForm(file);
    this.updateKeywords(file);
    this.updatePivotForm(file);
    this.updateTranslationForm(file);
  }

  /**
   * Handles clearing of the file input by resetting keyword selection and the
   * pivot/translation sub-forms to their empty state.
   */
  private handleFileCleared() {
    this.step = 'main';
    this.selectedKeywords = [];
    this.resetKeywordSearch();

    const file = this.file();
    if (file) {
      this.patchForm(file);
    }

    this.pivotForm.reset({ lang: '' });
    this.translationForm.reset({ translationLang: '', pivotId: '' });
    this.getDisabledLang(undefined);
  }

  /**
   * Handles a newly set file by initializing the name control from the file's
   * name (falling back to its id).
   */
  private handleNewFile() {
    this.step = 'main';
    const file = this.file();
    if (file) {
      this.fileForm.controls.name.patchValue(file.name || file.id);
    }
  }

  /**
   * Rebuilds the selected-keyword list from the file's stored keyword ids.
   *
   * @param file The file whose keywords should be reflected in the selection.
   */
  private updateKeywords(file: FileUploadItem) {
    this.selectedKeywords = [];
    this.resetKeywordSearch();

    if (file.keywords) {
      this.selectedKeywords = this.keywordDefinition().filter(
        (keyword) => keyword.id && file.keywords?.includes(keyword.id)
      );
    }
  }

  /**
   * Synchronizes the pivot sub-form and disabled-language list with the file's
   * pivot state.
   *
   * @param file The file whose pivot state should be reflected.
   */
  private updatePivotForm(file: FileUploadItem) {
    if (file.isPivot) {
      this.pivotForm.reset({ lang: file.lang });
      this.getDisabledLang(file.lang);
    } else {
      this.pivotForm.reset({ lang: '' });
      this.getDisabledLang(undefined);
    }
  }

  /**
   * Synchronizes the translation sub-form and disabled-language list with the
   * file's translation state.
   *
   * @param file The file whose translation state should be reflected.
   */
  private updateTranslationForm(file: FileUploadItem) {
    if (file.isTranslation) {
      this.translationForm.reset({
        translationLang: file.lang,
        pivotId: file.translationOf,
      });
      this.getDisabledLang(file.translationOf);
    } else {
      this.translationForm.reset({ translationLang: '', pivotId: '' });
      this.getDisabledLang(undefined);
    }
  }

  /**
   * Patches the main form with the given file's basic fields and dynamic
   * property values. Does nothing if the file or the form is not available.
   *
   * @param file The file whose values should populate the form.
   */
  public patchForm(file: FileUploadItem) {
    if (!(file && this.fileForm)) return;

    this.patchBasicFields(file);
    this.patchDynamicProperties(file);
  }

  /**
   * Patches the fixed metadata controls (name, title, description, author,
   * reference, security ranking, status, keywords and expiration date),
   * applying olaf-specific defaults where relevant.
   *
   * @param file The file whose basic fields populate the form.
   */
  private patchBasicFields(file: FileUploadItem) {
    const controls = this.fileForm.controls;

    controls.name.patchValue(file.name || file.id);
    controls.title.patchValue(file.title ?? { en: '' });
    controls.description.patchValue(file.description ?? { en: '' });
    controls.author.patchValue(file.author);
    controls.reference.patchValue(file.reference);
    controls.securityRanking.patchValue(
      file.securityRanking ??
        (environment.circabcRelease === 'olaf' ? 'SENSITIVE' : undefined)
    );
    controls.status.patchValue(file.status);
    controls.keywords.patchValue(file.keywords);
    controls.expirationDate.patchValue(
      environment.circabcRelease === 'olaf'
        ? this.expirationDateOlaf
        : file.expirationDate
    );
  }

  /**
   * Resets and patches each dynamic-property control from the file's stored
   * values, splitting comma-separated values into arrays for multi-selection
   * properties.
   *
   * @param file The file whose dynamic attribute values populate the form.
   */
  private patchDynamicProperties(file: FileUploadItem) {
    const keys = Object.keys(file);

    for (const dynProp of this.dynamicProperties()) {
      const key = this.getName(dynProp);
      this.fileForm.controls[key].reset();

      if (keys.includes(key)) {
        const value = this.getFileDynAttrValue(file, key);
        const patchValue =
          this.isMultiSelection(dynProp) && value ? value.split(', ') : value;
        this.fileForm.controls[key].patchValue(patchValue);
      }
    }
  }

  /**
   * Reads the raw value of a dynamic attribute from the file.
   *
   * @param file The file to read from.
   * @param key The dynamic attribute key (e.g. `dynAttr3`).
   * @returns The stored string value, or `undefined` when not present.
   */
  private getFileDynAttrValue(
    file: FileUploadItem,
    key: string
  ): string | undefined {
    return file[key];
  }

  /** The reactive control for the file name. */
  get nameControl() {
    return this.fileForm.controls.name;
  }

  /** The reactive control for the multilingual title. */
  get titleControl() {
    return this.fileForm.controls.title;
  }

  /** The reactive control for the multilingual description. */
  get descriptionControl() {
    return this.fileForm.controls.description;
  }

  /** The reactive control for the author. */
  get authorControl() {
    return this.fileForm.controls.author;
  }

  /** The reactive control for the reference. */
  get referenceControl() {
    return this.fileForm.controls.reference;
  }

  /** The reactive control for the expiration date. */
  get expirationDateControl() {
    return this.fileForm.controls.expirationDate;
  }

  /** The reactive control for the security ranking. */
  get securityRankingControl() {
    return this.fileForm.controls.securityRanking;
  }

  /** The reactive control for the status. */
  get statusControl() {
    return this.fileForm.controls.status;
  }

  /**
   * Copies the current form values back onto the bound {@link FileUploadItem}
   * (including keywords and dynamic properties), applies olaf-specific
   * expiration handling and emits the updated file via
   * {@link FileMetadataComponent.fileChange}. Does nothing when no file is bound.
   */
  public updateFileProperties() {
    const file = this.file();
    if (file === undefined) {
      return;
    }
    file.name = this.fileForm.value.name;
    file.title = this.fileForm.value.title;
    file.description = this.fileForm.value.description;
    file.author = this.fileForm.value.author;
    file.securityRanking = this.fileForm.value.securityRanking;
    file.reference = this.fileForm.value.reference;
    if (this.fileForm.value.keywords) {
      file.keywords = this.selectedKeywords.map((keyword) => {
        return keyword.id;
      }) as string[];
    }
    file.expirationDate = this.fileForm.value.expirationDate;
    if (environment.circabcRelease === 'olaf') {
      file.expirationDate = this.expirationDateOlaf.toISOString();
    }
    file.status = this.fileForm.value.status;

    for (const dynProp of this.dynamicProperties()) {
      const key = this.getName(dynProp);
      const value = this.fileForm.value[key];
      if (
        this.isMultiSelection(dynProp) &&
        value !== undefined &&
        value !== null &&
        value !== ''
      ) {
        file[key] = value.join(', ');
      } else {
        file[key] = value;
      }
    }

    this.fileChange.emit(file);
  }

  /** The current user's UI language, used to render multilingual labels. */
  get lang() {
    return this.loginService.getUser().uiLang;
  }

  /**
   * Toggles the selection state of a keyword for the current file and updates
   * the keywords form control accordingly.
   *
   * @param keyword The keyword definition to select or deselect.
   */
  public toggleKeyword(keyword: KeywordDefinition) {
    const file = this.file();
    if (file?.keywords && keyword.id) {
      if (file.keywords.includes(keyword.id)) {
        this.selectedKeywords.splice(this.selectedKeywords.indexOf(keyword), 1);
      } else {
        this.selectedKeywords.push(keyword);
      }
    } else if (file && !file.keywords && keyword.id) {
      this.selectedKeywords.push(keyword);
    }

    this.fileForm.controls.keywords.setValue(
      this.selectedKeywords.map((k) => k.id).join(',')
    );
  }

  /**
   * Indicates whether a keyword is currently selected for the bound file.
   *
   * @param keyword The keyword definition to check.
   * @returns `true` when the file's keywords include the given keyword id.
   */
  public isSelectedKeyword(keyword: KeywordDefinition) {
    const file = this.file();
    if (file?.keywords && keyword.id) {
      return file.keywords.includes(keyword.id);
    }
    return false;
  }

  /**
   * The keyword definitions matching the current search term (case-insensitive
   * match against any localized title). Returns an empty array when no
   * definitions are loaded.
   */
  get filteredKeywords() {
    const keywordDefinition = this.keywordDefinition();
    if (keywordDefinition) {
      const searchValue = this.filterForm.value.keywordSearch
        ? this.filterForm.value.keywordSearch.toLowerCase().trim()
        : '';
      return keywordDefinition.filter((keyword) => {
        let foundInTitle = 0;
        if (keyword.title) {
          const keys = Object.keys(keyword.title);
          for (const key of keys) {
            if (keyword.title[key].toLowerCase().includes(searchValue)) {
              foundInTitle += 1;
            }
          }
        }
        return foundInTitle > 0;
      });
    }
    return [];
  }

  /** Clears the keyword search field if the filter form has been initialized. */
  public resetKeywordSearch() {
    if (this.filterForm) {
      this.filterForm.reset({ keywordSearch: '' });
    }
  }

  /**
   * Marks the bound file as a pivot (source language) document using the pivot
   * sub-form's selected language and emits the change. Does nothing when no
   * file is bound.
   */
  public makeAsPivot() {
    const file = this.file();
    if (file === undefined) {
      return;
    }
    file.isPivot = true;
    file.lang = this.pivotForm.value.pivotLang;
    this.fileChange.emit(file);
  }

  /**
   * Removes the pivot flag and language from the bound file and emits the
   * change. Does nothing when no file is bound.
   */
  public cancelPivot() {
    const file = this.file();
    if (file === undefined) {
      return;
    }
    file.isPivot = false;
    file.lang = '';
    this.fileChange.emit(file);
  }

  /**
   * Recomputes {@link FileMetadataComponent.disabledLangs} for the given pivot,
   * collecting the languages already taken by that pivot and its translations.
   *
   * @param pivotId The id of the pivot to compute disabled languages for, or
   * `undefined` to clear the list.
   */
  public getDisabledLang(pivotId: string | undefined) {
    const disabledLangs: string[] = [];

    if (pivotId) {
      this.pivots().forEach((pivot) => {
        if (pivot.id === pivotId && pivot.lang) {
          disabledLangs.push(pivot.lang);
        }
      });

      this.translations().forEach((translation) => {
        if (translation.translationOf === pivotId && translation.lang) {
          if (disabledLangs.indexOf(translation.lang)) {
            disabledLangs.push(translation.lang);
          }
        }
      });
    }

    this.disabledLangs.set(disabledLangs);
  }

  /**
   * Marks the bound file as a translation of the selected pivot, using the
   * translation sub-form's pivot id and language, and emits the change. Does
   * nothing when no file is bound.
   */
  public defineAsTranslation() {
    const file = this.file();
    if (file === undefined) {
      return;
    }
    file.isTranslation = true;
    file.translationOf = this.translationForm.value.pivotId;
    file.lang = this.translationForm.value.translationLang;
    this.fileChange.emit(file);
  }

  /**
   * Removes the translation flag, target pivot and language from the bound file
   * and emits the change. Does nothing when no file is bound.
   */
  public cancelTranslation() {
    const file = this.file();
    if (file === undefined) {
      return;
    }
    file.isTranslation = false;
    file.translationOf = undefined;
    file.lang = undefined;
    this.fileChange.emit(file);
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a date field.
   */
  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a single-line text field.
   */
  public isTextField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_FIELD';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a multi-line text area.
   */
  public isTextArea(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_AREA';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a single- or multi-selection field.
   */
  public isSelectionOrMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a multi-selection field.
   */
  public isMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'MULTI_SELECTION';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a single-selection field.
   */
  public isSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'SELECTION';
  }

  /**
   * @param dpd The dynamic property definition.
   * @returns The property's display index.
   */
  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  /**
   * Builds the form control name for a dynamic property.
   *
   * @param dynprop The dynamic property definition.
   * @returns The control name in the form `dynAttr{index}`.
   */
  private getName(dynprop: DynamicPropertyDefinition): string {
    return `dynAttr${dynprop.index}`;
  }

  /**
   * Equality comparator used by selection controls to match option values.
   *
   * @param optionOne First option value.
   * @param optionTwo Second option value.
   * @returns `true` when both values are defined and strictly equal.
   */
  public compareFn(optionOne?: string, optionTwo?: string): boolean {
    if (optionOne && optionTwo) {
      return optionOne === optionTwo;
    }
    return false;
  }
}
