import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  signal,
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';

/**
 * Standalone form control that lets the user browse and toggle the keywords
 * defined for an interest group as part of an advanced search form.
 *
 * The component fetches the keyword definitions for the given group from the
 * {@link KeywordsService} and renders them as a list of selectable
 * {@link KeywordTagComponent} tags. Selecting or deselecting a tag updates the
 * `keywords` control of the parent {@link FormGroup}, and the component keeps
 * its visual selection state in sync with external changes to that form.
 *
 * Key collaborators:
 * - {@link KeywordsService} — retrieves the keyword definitions for a group.
 * - {@link KeywordTagComponent} — renders each individual selectable keyword.
 */
@Component({
  selector: 'cbc-form-keyword-finder',
  templateUrl: './form-keyword-finder.component.html',
  styleUrl: './form-keyword-finder.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [KeywordTagComponent, TranslocoModule],
})
export class FormKeywordFinderComponent implements OnInit {
  /** Service used to fetch the keyword definitions available for a group. */
  private readonly keywordsService = inject(KeywordsService);

  /**
   * Required input holding the identifier of the interest group whose keyword
   * definitions should be loaded. May be `undefined`, in which case no keywords
   * are fetched.
   */
  public readonly groupId = input.required<string | undefined>();

  /**
   * Transloco translation key used as the label for the keyword finder.
   * Defaults to `'label.keywords'`.
   */
  public readonly label = input('label.keywords');

  /**
   * Required input referencing the parent advanced-search {@link FormGroup}.
   * Its `keywords` control is read to reflect the current selection and written
   * to whenever a keyword is toggled.
   */
  public readonly searchAdvancedForm = input.required<FormGroup>();

  /**
   * The keyword definitions available for the current group, augmented with a
   * `selected` flag driving the UI selection state.
   */
  public readonly availableKeywords = signal<SelectableKeyword[]>([]);
  /** The keyword definitions currently considered selected. */
  public selectedKeywords: KeywordDefinition[] = [];

  /**
   * Angular lifecycle hook. Loads the available keywords for the group and
   * subscribes to the advanced-search form's value changes so the local
   * selection state stays synchronized with the form's `keywords` control.
   *
   * @returns Nothing.
   */
  ngOnInit(): void {
    this.loadAvailableKeywords();
    this.searchAdvancedForm().valueChanges.subscribe((dataForm) => {
      const loadKeywordsSelected: KeywordDefinition[] = dataForm.keywords;
      this.availableKeywords.update((keywords) =>
        this.withSelectionState(keywords, loadKeywordsSelected)
      );
    });
  }

  /**
   * Returns a copy of the given keywords with their `selected` flag set to
   * reflect whether each keyword appears in the provided selection.
   *
   * @param keywords The available keywords to update.
   * @param selectedKeywords The keywords currently selected in the form.
   * @returns The keywords with an updated `selected` flag.
   */
  private withSelectionState(
    keywords: SelectableKeyword[],
    selectedKeywords: KeywordDefinition[]
  ): SelectableKeyword[] {
    return keywords.map((keywordTmp) => ({
      ...keywordTmp,
      selected:
        selectedKeywords?.some((keyword) => keyword.id === keywordTmp.id) ??
        false,
    }));
  }

  /**
   * Fetches the keyword definitions for the current group from the backend and
   * stores them in {@link availableKeywords}. If no group id is set, the list is
   * cleared and no request is made.
   *
   * @returns A promise that resolves once the available keywords have been
   * loaded (or cleared).
   */
  public async loadAvailableKeywords() {
    this.availableKeywords.set([]);
    const groupId = this.groupId();
    if (groupId) {
      this.availableKeywords.set(
        await this.keywordsService.getKeywordDefinitionsAsync({ id: groupId })
      );
    }
  }

  /**
   * Toggles the selection state of the given keyword and pushes the resulting
   * set of selected keywords into the advanced-search form's `keywords` control.
   *
   * @param keyword The keyword whose selection state should be toggled.
   */
  public toggleSelected(keyword: SelectableKeyword) {
    this.availableKeywords.update((keywords) =>
      keywords.map((keywordTmp) =>
        keywordTmp.id === keyword.id
          ? { ...keywordTmp, selected: !keywordTmp.selected }
          : keywordTmp
      )
    );

    this.searchAdvancedForm().controls.keywords.setValue(
      this.availableKeywords().filter((keyword) => keyword.selected)
    );
  }

  /**
   * Indicates whether any keywords are available to display.
   *
   * @returns `true` if at least one keyword is available, `false` otherwise.
   */
  public hasKeywords(): boolean {
    return this.availableKeywords().length > 0;
  }
}
