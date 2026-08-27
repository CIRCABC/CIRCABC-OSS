import {
  AfterViewInit,
  Component,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
  inject,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  InterestGroup,
  SearchConfig,
} from 'app/core/generated/circabc';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { DatePicker } from 'primeng/datepicker';
import { FormUserFinderComponent } from 'app/shared/form-user-finder/form-user-finder.component';
import { FormKeywordFinderComponent } from 'app/shared/form-keyword-finder/form-keyword-finder.component';
import { CommonModule } from '@angular/common';
import { VersionMaskDirective } from 'app/shared/directives/version-mask.directive';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { convertDateFormat } from 'app/core/util';

@Component({
  selector: 'cbc-advanced-search',
  templateUrl: './advanced-search.component.html',
  styleUrl: './advanced-search.component.scss',
  preserveWhitespaces: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,
    MatTooltipModule,
    LangSelectorComponent,
    DatePicker,
    FormUserFinderComponent,
    FormKeywordFinderComponent,
    VersionMaskDirective,
    ControlMessageComponent,
  ],
})
export class AdvancedSearchComponent
  implements OnInit, AfterViewInit, OnChanges
{
  readonly groupId = input<string>();

  readonly searchString = input<string>();

  public readonly closeAdvancedSearchIG = output();

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public readonly advancedSearch = output<any>();

  public helpResult!: HelpSearchResult;
  public searchForm!: FormGroup;
  public searchAdvancedForm!: FormGroup;
  public searchConfigOptions: SearchConfig[] =
    this.userPreferences.getSearchConfiguration();
  public currentIg!: InterestGroup;
  public nodeId!: string;
  public saveAsWindow = false;

  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly translateService = inject(TranslocoService);
  public readonly dynamicProperties = signal<DynamicPropertyDefinition[]>([]);
  public readonly dynamicPropertiesExpanded = signal<boolean>(true);
  public readonly dynamicPropertiesError = signal<boolean>(false);

  public constructor(
    private fb: FormBuilder,
    public userPreferences: UserPreferencesService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes.searchString.currentValue &&
      changes.searchString.currentValue !== ''
    ) {
      this.searchAdvancedForm.controls.searchString.setValue(
        changes.searchString.currentValue
      );
    }
  }

  ngAfterViewInit(): void {
    this.searchConfigOptions = this.userPreferences.getSearchConfiguration();
  }

  public ngOnInit(): void {
    this.buildForm();
    this.loadDynamicProperties();
  }

  private async loadDynamicProperties(): Promise<void> {
    const groupId = this.groupId();
    if (!groupId) {
      return;
    }

    try {
      const definitions = await firstValueFrom(
        this.dynamicPropertiesService.getDynamicPropertyDefinitions(groupId)
      );

      const validProperties = definitions.filter(
        (prop) => prop.index !== undefined && prop.index !== null
      );

      this.dynamicProperties.set(validProperties);

      for (const prop of validProperties) {
        const initialValue =
          prop.propertyType === 'SELECTION' ||
          prop.propertyType === 'MULTI_SELECTION'
            ? ''
            : null;
        this.searchAdvancedForm.addControl(
          `dynAttr_${prop.index}`,
          new FormControl(initialValue)
        );
      }
    } catch (error) {
      this.dynamicPropertiesError.set(true);
      console.error(
        `Failed to load dynamic properties for group ${groupId}:`,
        error
      );
    }
  }

  private buildForm(): void {
    this.searchAdvancedForm = this.fb.group(
      {
        searchName: null,
        searchString: [null],
        language: null,
        searchIn: 'ALL',
        creatorUser: null,
        creationDateFrom: null,
        creationDateTo: null,
        modifiedDateFrom: null,
        modifiedDateTo: null,
        keywords: [],
        status: null,
        securityRanking: null,
        version: null,
      },
      {
        updateOn: 'change',
      }
    );
  }

  selectSearch(searchName: string) {
    const serachConfigSelected = this.searchConfigOptions.find(
      (item) => item.searchName === searchName
    );
    if (serachConfigSelected) {
      this.searchAdvancedForm.patchValue(serachConfigSelected);
    }
  }

  saveSearch() {
    this.userPreferences.saveSearchPreferences(this.searchAdvancedForm.value);
    this.saveAsWindow = false;
    this.searchConfigOptions = this.userPreferences.getSearchConfiguration();
  }

  deleteConfiguration(searchName: string) {
    this.userPreferences.deleteConfiguration(searchName);
  }

  public toggleDynamicSection(): void {
    this.dynamicPropertiesExpanded.update((v) => !v);
  }

  public resetForm(): void {
    this.searchAdvancedForm.reset({
      searchIn: 'ALL',
    });

    for (const prop of this.dynamicProperties()) {
      const controlKey = `dynAttr_${prop.index}`;
      const control = this.searchAdvancedForm.get(controlKey);
      if (control) {
        const resetValue =
          prop.propertyType === 'SELECTION' ||
          prop.propertyType === 'MULTI_SELECTION'
            ? ''
            : null;
        control.setValue(resetValue);
      }
    }

    this.dynamicPropertiesExpanded.set(true);
  }

  get currentLang(): string {
    return this.translateService.getActiveLang();
  }

  public async search() {
    const userid = this.searchAdvancedForm.value.creatorUser
      ? this.searchAdvancedForm.value.creatorUser.userId
      : undefined;

    let keywords: string | undefined = undefined;
    if (
      this.searchAdvancedForm.value.keywords &&
      this.searchAdvancedForm.value.keywords.length > 0
    ) {
      const key: string[] = [];
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      this.searchAdvancedForm.value.keywords.forEach((kw: any) => {
        key.push(kw.id);
      });

      const str = key.join(', ');
      keywords = `[${str.replace(/"/g, '')}]`;
    } else {
      keywords = undefined;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data: Record<string, any> = {
      searchString: this.searchAdvancedForm.value.searchString,
      language: this.searchAdvancedForm.value.language,
      searchIn: this.searchAdvancedForm.value.searchIn,
      creator: userid,
      creationDateFrom: convertDateFormat(
        this.searchAdvancedForm.value.creationDateFrom
      ),
      creationDateTo: convertDateFormat(
        this.searchAdvancedForm.value.creationDateTo
      ),
      modifiedDateFrom: convertDateFormat(
        this.searchAdvancedForm.value.modifiedDateFrom
      ),
      modifiedDateTo: convertDateFormat(
        this.searchAdvancedForm.value.modifiedDateTo
      ),
      keywords: keywords,
      status: this.searchAdvancedForm.value.status,
      securityRanking: this.searchAdvancedForm.value.securityRanking,
      version: this.searchAdvancedForm.value.version,
    };

    // Map dynamic property values to dynAttr1-dynAttr20
    for (let i = 1; i <= 20; i++) {
      const controlKey = `dynAttr_${i}`;
      const control = this.searchAdvancedForm.get(controlKey);

      if (control) {
        const value = control.value;
        // Use == for comparison since prop.index may come as string from API
        const propDef = this.dynamicProperties().find(
          (p) => Number(p.index) === i
        );

        if (value === null || value === undefined || value === '') {
          data[`dynAttr${i}`] = undefined;
        } else if (propDef?.propertyType === 'DATE_FIELD') {
          data[`dynAttr${i}`] = convertDateFormat(value);
        } else {
          data[`dynAttr${i}`] = value;
        }
      } else {
        data[`dynAttr${i}`] = undefined;
      }
    }

    this.advancedSearch.emit({ ...data, isAdvancedSearch: true });
  }

  get searchStringControl(): AbstractControl {
    return this.searchAdvancedForm.controls.searchString;
  }
}
