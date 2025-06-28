import {
  AfterViewInit,
  Component,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import { InterestGroup, SearchConfig } from 'app/core/generated/circabc';
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
  }

  private buildForm(): void {
    this.searchAdvancedForm = this.fb.group(
      {
        searchName: null,
        searchString: [null, Validators.required],
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

    const data = {
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
    this.advancedSearch.emit(data);
  }

  get searchStringControl(): AbstractControl {
    return this.searchAdvancedForm.controls.searchString;
  }
}
