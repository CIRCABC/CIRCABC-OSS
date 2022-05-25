import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  KeywordDefinition,
  KeywordsService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'cbc-file-metadata',
  templateUrl: './file-metadata.component.html',
  styleUrls: ['./file-metadata.component.scss'],
})
export class FileMetadataComponent implements OnInit, OnChanges {
  @Input() file: FileUploadItem | undefined;
  @Input() pivots: FileUploadItem[] = [];
  @Input() translations: FileUploadItem[] = [];
  @Output() readonly fileChange = new EventEmitter<FileUploadItem>();

  public fileForm!: FormGroup;
  public filterForm!: FormGroup;
  public pivotForm!: FormGroup;
  public translationForm!: FormGroup;
  public step = 'main';
  public groupNodeId!: string;
  public keywordDefinition!: KeywordDefinition[];
  public selectedKeywords: KeywordDefinition[] = [];
  public disabledLangs: string[] = [];
  public dynamicProperties: DynamicPropertyDefinition[] = [];

  constructor(
    private fb: FormBuilder,
    private keywordsService: KeywordsService,
    private route: ActivatedRoute,
    private loginService: LoginService,
    private dynamicPropertiesService: DynamicPropertiesService
  ) {}

  async ngOnInit() {
    this.route.params.subscribe((params) => {
      if (params && params.id) {
        this.groupNodeId = params.id;
      }
    });

    this.step = 'main';

    this.fileForm = this.fb.group({
      name: [],
      title: [],
      description: [],
      author: [],
      reference: [],
      expirationDate: [],
      securityRanking: [],
      status: [],
      keywords: [],
    });

    this.dynamicProperties = await firstValueFrom(
      this.dynamicPropertiesService.getDynamicPropertyDefinitions(
        this.groupNodeId
      )
    );

    for (const dynprop of this.dynamicProperties) {
      this.fileForm.addControl(this.getName(dynprop), new FormControl());
    }

    if (this.file) {
      this.patchForm(this.file);
    }
    this.fileForm.valueChanges
      .pipe(debounceTime(100), distinctUntilChanged())
      .subscribe((_data) => {
        this.updateFileProperties();
      });

    this.keywordsService
      .getKeywordDefinitions(this.groupNodeId)
      .subscribe((keywords) => {
        this.keywordDefinition = keywords;
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
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes.file &&
      changes.file.currentValue &&
      changes.file.previousValue
    ) {
      this.step = 'main';
      this.patchForm(changes.file.currentValue);
      this.selectedKeywords = [];
      this.resetKeywordSearch();
      if (changes.file.currentValue.keywords) {
        for (const keyword of this.keywordDefinition) {
          if (changes.file.currentValue.keywords.indexOf(keyword.id) !== -1) {
            this.selectedKeywords.push(keyword);
          }
        }
      }

      if (changes.file.currentValue.isPivot) {
        this.pivotForm.reset({ lang: changes.file.currentValue.lang });
        this.getDisabledLang(changes.file.currentValue.lang);
      } else {
        this.pivotForm.reset({ lang: '' });
        this.getDisabledLang(undefined);
      }

      if (changes.file.currentValue.isTranslation) {
        this.translationForm.reset({
          translationLang: changes.file.currentValue.lang,
          pivotId: changes.file.currentValue.translationOf,
        });
        this.getDisabledLang(changes.file.currentValue.translationOf);
      } else {
        this.translationForm.reset({ translationLang: '', pivotId: '' });
        this.getDisabledLang(undefined);
      }
    } else if (
      changes.file &&
      changes.file.currentValue === undefined &&
      changes.file.previousValue !== undefined
    ) {
      this.step = 'main';
      this.selectedKeywords = [];
      this.resetKeywordSearch();
      if (this.file) {
        this.patchForm(this.file);
      }
      this.pivotForm.reset({ lang: '' });
      this.translationForm.reset({ translationLang: '', pivotId: '' });
      this.getDisabledLang(undefined);
    } else if (
      changes.file &&
      changes.file.currentValue !== undefined &&
      changes.file.previousValue === undefined
    ) {
      this.step = 'main';
      if (this.file) {
        this.fileForm.controls.name.patchValue(
          this.file.name ? this.file.name : this.file.id
        );
      }
    }
  }

  public patchForm(file: FileUploadItem) {
    if (file && this.fileForm) {
      this.fileForm.controls.name.patchValue(file.name ? file.name : file.id);

      if (file.title !== null && file.title !== undefined) {
        this.fileForm.controls.title.patchValue(file.title);
      } else {
        this.fileForm.controls.title.patchValue({ en: '' });
      }
      if (file.description !== null && file.description !== undefined) {
        this.fileForm.controls.description.patchValue(file.description);
      } else {
        this.fileForm.controls.description.patchValue({ en: '' });
      }
      this.fileForm.controls.author.patchValue(file.author);
      this.fileForm.controls.reference.patchValue(file.reference);
      this.fileForm.controls.securityRanking.patchValue(file.securityRanking);
      this.fileForm.controls.status.patchValue(file.status);
      this.fileForm.controls.keywords.patchValue(file.keywords);
      this.fileForm.controls.expirationDate.patchValue(file.expirationDate);

      const keys = Object.keys(file);
      for (const dynProp of this.dynamicProperties) {
        const key = this.getName(dynProp);
        this.fileForm.controls[key].reset();
        if (keys.indexOf(key) !== -1) {
          const value = this.getFileDynAttrValue(file, key);
          if (this.isMultiSelection(dynProp) && value) {
            this.fileForm.controls[key].patchValue(value.split(', '));
          } else {
            this.fileForm.controls[key].patchValue(
              this.getFileDynAttrValue(file, key)
            );
          }
        }
      }
    }
  }

  private getFileDynAttrValue(
    file: FileUploadItem,
    key: string
  ): string | undefined {
    return file[key];
  }

  get nameControl() {
    return this.fileForm.controls.name;
  }

  get titleControl() {
    return this.fileForm.controls.title;
  }

  get descriptionControl() {
    return this.fileForm.controls.description;
  }

  get authorControl() {
    return this.fileForm.controls.author;
  }

  get referenceControl() {
    return this.fileForm.controls.reference;
  }

  get expirationDateControl() {
    return this.fileForm.controls.expirationDate;
  }

  get securityRankingControl() {
    return this.fileForm.controls.securityRanking;
  }

  get statusControl() {
    return this.fileForm.controls.status;
  }

  public updateFileProperties() {
    if (this.file === undefined) {
      return;
    }
    this.file.name = this.fileForm.value.name;
    this.file.title = this.fileForm.value.title;
    this.file.description = this.fileForm.value.description;
    this.file.author = this.fileForm.value.author;
    this.file.securityRanking = this.fileForm.value.securityRanking;
    this.file.reference = this.fileForm.value.reference;
    if (this.fileForm.value.keywords) {
      this.file.keywords = this.selectedKeywords.map((keyword) => {
        return keyword.id;
      }) as string[];
    }
    this.file.expirationDate = this.fileForm.value.expirationDate;
    this.file.status = this.fileForm.value.status;

    for (const dynProp of this.dynamicProperties) {
      const key = this.getName(dynProp);
      const value = this.fileForm.value[key];
      if (
        this.isMultiSelection(dynProp) &&
        value !== undefined &&
        value !== null &&
        value !== ''
      ) {
        this.file[key] = value.join(', ');
      } else {
        this.file[key] = value;
      }
    }

    this.fileChange.emit(this.file);
  }

  get lang() {
    return this.loginService.getUser().uiLang;
  }

  public toggleKeyword(keyword: KeywordDefinition) {
    if (this.file && this.file.keywords && keyword.id) {
      if (this.file.keywords.indexOf(keyword.id) === -1) {
        this.selectedKeywords.push(keyword);
      } else {
        this.selectedKeywords.splice(this.selectedKeywords.indexOf(keyword), 1);
      }
    } else if (this.file && !this.file.keywords && keyword.id) {
      this.selectedKeywords.push(keyword);
    }

    this.fileForm.controls.keywords.setValue(this.selectedKeywords.join(','));
  }

  public isSelectedKeyword(keyword: KeywordDefinition) {
    if (this.file && this.file.keywords && keyword.id) {
      return this.file.keywords.indexOf(keyword.id) !== -1;
    } else {
      return false;
    }
  }

  get filteredKeywords() {
    if (this.keywordDefinition) {
      const searchValue = this.filterForm.value.keywordSearch
        ? this.filterForm.value.keywordSearch.toLowerCase().trim()
        : '';
      return this.keywordDefinition.filter((keyword) => {
        let foundInTitle = 0;
        if (keyword.title) {
          const keys = Object.keys(keyword.title);
          for (const key of keys) {
            if (keyword.title[key].toLowerCase().indexOf(searchValue) !== -1) {
              foundInTitle += 1;
            }
          }
        }
        return foundInTitle > 0;
      });
    } else {
      return [];
    }
  }

  public resetKeywordSearch() {
    if (this.filterForm) {
      this.filterForm.reset({ keywordSearch: '' });
    }
  }

  public makeAsPivot() {
    if (this.file === undefined) {
      return;
    }
    this.file.isPivot = true;
    this.file.lang = this.pivotForm.value.pivotLang;
    this.fileChange.emit(this.file);
  }

  public cancelPivot() {
    if (this.file === undefined) {
      return;
    }
    this.file.isPivot = false;
    this.file.lang = '';
    this.fileChange.emit(this.file);
  }

  public getDisabledLang(pivotId: string | undefined) {
    this.disabledLangs = [];

    if (pivotId) {
      this.pivots.forEach((pivot) => {
        if (pivot.id === pivotId && pivot.lang) {
          this.disabledLangs.push(pivot.lang);
        }
      });

      this.translations.forEach((translation) => {
        if (translation.translationOf === pivotId && translation.lang) {
          if (this.disabledLangs.indexOf(translation.lang)) {
            this.disabledLangs.push(translation.lang);
          }
        }
      });
    }
  }

  public defineAsTranslation() {
    if (this.file === undefined) {
      return;
    }
    this.file.isTranslation = true;
    this.file.translationOf = this.translationForm.value.pivotId;
    this.file.lang = this.translationForm.value.translationLang;
    this.fileChange.emit(this.file);
  }

  public cancelTranslation() {
    if (this.file === undefined) {
      return;
    }
    this.file.isTranslation = false;
    this.file.translationOf = undefined;
    this.file.lang = undefined;
    this.fileChange.emit(this.file);
  }

  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  public isTextField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_FIELD';
  }

  public isTextArea(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_AREA';
  }

  public isSelectionOrMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  public isMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'MULTI_SELECTION';
  }

  public isSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'SELECTION';
  }

  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  private getName(dynprop: DynamicPropertyDefinition): string {
    return `dynAttr${dynprop.index}`;
  }

  public compareFn(optionOne?: string, optionTwo?: string): boolean {
    if (optionOne && optionTwo) {
      return optionOne === optionTwo;
    } else {
      return false;
    }
  }
}
