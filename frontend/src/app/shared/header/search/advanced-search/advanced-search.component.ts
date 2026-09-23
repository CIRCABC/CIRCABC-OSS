import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { InterestGroup, SearchConfig } from 'app/core/generated/circabc';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { convertDateFormat } from 'app/core/util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { VersionMaskDirective } from 'app/shared/directives/version-mask.directive';
import { FormKeywordFinderComponent } from 'app/shared/form-keyword-finder/form-keyword-finder.component';
import { FormUserFinderComponent } from 'app/shared/form-user-finder/form-user-finder.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';

/**
 * Standalone component (`cbc-advanced-search`) that renders the advanced
 * search form used from the application header search area.
 *
 * The form lets the user refine a search using multiple criteria such as a
 * free-text search string, language, target scope, creator, creation and
 * modification date ranges, keywords, status, security ranking and version.
 * It also integrates with {@link UserPreferencesService} so that named search
 * configurations can be selected, saved and deleted.
 *
 * When the user triggers a search the collected and normalised criteria are
 * emitted through the {@link AdvancedSearchComponent.advancedSearch} output;
 * closing the advanced search panel is signalled through
 * {@link AdvancedSearchComponent.closeAdvancedSearchIG}.
 */
@Component({
  selector: 'cbc-advanced-search',
  templateUrl: './advanced-search.component.html',
  styleUrl: './advanced-search.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    TranslocoModule,
    MatTooltipModule,
    LangSelectorComponent,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    FormUserFinderComponent,
    FormKeywordFinderComponent,
    VersionMaskDirective,
    ControlMessageComponent,
  ],
})
export class AdvancedSearchComponent
  implements OnInit, AfterViewInit, OnChanges
{
  /** Angular reactive-forms builder used to construct the search form. */
  private readonly fb = inject(FormBuilder);
  /**
   * Service providing access to the user's stored search configurations and
   * used to load, save and delete named advanced-search presets.
   */
  userPreferences = inject(UserPreferencesService);

  /**
   * Optional identifier of the interest group the search is scoped to.
   */
  readonly groupId = input<string>();

  /**
   * Optional initial search string. When it changes to a non-empty value it
   * is propagated into the `searchString` control of the form.
   */
  readonly searchString = input<string>();

  /**
   * Emitted when the advanced search panel should be closed (for the
   * interest-group context).
   */
  public readonly closeAdvancedSearchIG = output();

  /**
   * Emitted when a search is executed, carrying the normalised search
   * criteria object built by {@link AdvancedSearchComponent.search}.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public readonly advancedSearch = output<any>();

  /** Result of a help search, when applicable. */
  public helpResult!: HelpSearchResult;
  /** Reserved secondary form reference. */
  public searchForm!: FormGroup;
  /** Reactive form holding all advanced search criteria. */
  public searchAdvancedForm!: FormGroup;
  /**
   * List of the user's saved search configurations, used to populate the
   * form when a preset is selected.
   */
  public searchConfigOptions: SearchConfig[] =
    this.userPreferences.getSearchConfiguration();
  /** The current interest group context, when available. */
  public currentIg!: InterestGroup;
  /** Identifier of the node the search is related to, when applicable. */
  public nodeId!: string;
  /** Whether the "save search as" input area is currently displayed. */
  public saveAsWindow = false;

  /**
   * Angular lifecycle hook. Propagates changes to the {@link searchString}
   * input into the corresponding form control when a non-empty value is
   * received.
   *
   * @param changes - The set of changed input properties.
   */
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

  /**
   * Angular lifecycle hook. Refreshes the available search configurations
   * from {@link UserPreferencesService} after the view has initialised.
   */
  ngAfterViewInit(): void {
    this.searchConfigOptions = this.userPreferences.getSearchConfiguration();
  }

  /**
   * Angular lifecycle hook. Builds the advanced search reactive form.
   */
  public ngOnInit(): void {
    this.buildForm();
  }

  /**
   * Initialises {@link searchAdvancedForm} with all search-criteria controls
   * and their default values. The `searchString` control is required.
   */
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

  /**
   * Loads a saved search configuration into the form by name.
   *
   * @param searchName - Name of the saved configuration to apply. If no
   * matching configuration exists the form is left unchanged.
   */
  selectSearch(searchName: string) {
    const serachConfigSelected = this.searchConfigOptions.find(
      (item) => item.searchName === searchName
    );
    if (serachConfigSelected) {
      this.searchAdvancedForm.patchValue(serachConfigSelected);
    }
  }

  /**
   * Persists the current form values as a named search configuration via
   * {@link UserPreferencesService}, hides the save area and refreshes the
   * list of available configurations.
   */
  saveSearch() {
    this.userPreferences.saveSearchPreferences(this.searchAdvancedForm.value);
    this.saveAsWindow = false;
    this.searchConfigOptions = this.userPreferences.getSearchConfiguration();
  }

  /**
   * Deletes a previously saved search configuration.
   *
   * @param searchName - Name of the configuration to delete.
   */
  deleteConfiguration(searchName: string) {
    this.userPreferences.deleteConfiguration(searchName);
  }

  /**
   * Collects the current form criteria, normalises them (resolving the
   * creator user id, formatting date ranges via {@link convertDateFormat} and
   * flattening the selected keywords into a bracketed string) and emits the
   * resulting search request through
   * {@link AdvancedSearchComponent.advancedSearch}.
   *
   * @returns A promise that resolves once the search event has been emitted.
   */
  public async search() {
    const userid = this.searchAdvancedForm.value.creatorUser
      ? this.searchAdvancedForm.value.creatorUser.userId
      : undefined;

    let keywords: string | undefined;
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
      keywords = `[${str.replaceAll('"', '')}]`;
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

  /**
   * Convenience accessor for the `searchString` form control, typically used
   * by the template to display validation messages.
   *
   * @returns The `searchString` {@link AbstractControl} of the form.
   */
  get searchStringControl(): AbstractControl {
    return this.searchAdvancedForm.controls.searchString;
  }
}
