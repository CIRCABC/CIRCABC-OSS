import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService, Node as ModelNode } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { arrayDiff } from 'app/core/util';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component (`cbc-add-keyword`) that lets a user attach keywords
 * to a document within an interest group.
 *
 * It renders a trigger and a modal listing the keywords defined for the group
 * that are not yet applied to the document. Each available keyword is shown as
 * a selectable {@link KeywordTagComponent}; a {@link SpinnerComponent} is
 * displayed while the addition is in progress. Selected keywords are persisted
 * through the {@link KeywordsService}, and the outcome is reported to the
 * parent via the {@link AddKeywordComponent.addedKeyword} output.
 */
@Component({
  selector: 'cbc-add-keyword',
  templateUrl: './add-keyword.component.html',
  styleUrl: './add-keyword.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class AddKeywordComponent implements OnInit {
  /** API client used to read keyword definitions and persist keyword assignments. */
  private readonly keywordsService = inject(KeywordsService);

  /** Required input: the document node to which keywords are added. */
  readonly documentNode = input.required<ModelNode>();
  /** Required input: the group node whose keyword definitions are the source of choices. */
  readonly groupNode = input.required<ModelNode>();
  /** Emits an {@link ActionEmitterResult} once one or more keywords have been added successfully. */
  readonly addedKeyword = output<ActionEmitterResult>();

  /** Controls visibility of the keyword-selection modal. */
  public readonly showModal = signal(false);
  /** True while keyword assignments are being persisted, driving the spinner. */
  public readonly adding = signal(false);
  /** Group-defined keywords not yet applied to the document, selectable by the user. */
  public readonly availableKeywords = signal<SelectableKeyword[]>([]);
  /** Keywords already applied to the document. */
  public usedKeywords: SelectableKeyword[] = [];

  /**
   * Angular lifecycle hook. Loads the available keywords when the component
   * is initialized.
   */
  ngOnInit(): void {
    this.loadAvailableKeywords();
  }

  /**
   * Refreshes the available keywords and opens the selection modal.
   *
   * @returns A promise that resolves once the keywords are loaded and the modal is shown.
   */
  public async openModal() {
    await this.loadAvailableKeywords();
    this.showModal.set(true);
  }

  /**
   * Loads the keywords already applied to the document and the keywords defined
   * for the group, then computes {@link availableKeywords} as the difference
   * (group definitions minus already-used keywords). Does nothing when either
   * the group or document node has no id.
   *
   * @returns A promise that resolves once the keyword lists have been populated.
   */
  public async loadAvailableKeywords() {
    this.availableKeywords.set([]);
    const groupNode = this.groupNode();
    const documentNode = this.documentNode();
    if (groupNode.id && documentNode.id) {
      this.usedKeywords = await this.keywordsService.getKeywordsAsync({
        id: documentNode.id,
      });
      const sourceKeywords =
        await this.keywordsService.getKeywordDefinitionsAsync({
          id: groupNode.id,
        });
      this.availableKeywords.set(
        arrayDiff(sourceKeywords, this.usedKeywords, 'id')
      );
    }
  }

  /**
   * Toggles the selected state of the matching keyword within
   * {@link availableKeywords}.
   *
   * @param keyword The keyword whose selection should be toggled (matched by id).
   */
  public toggleSelected(keyword: SelectableKeyword) {
    this.availableKeywords.update((keywords) =>
      keywords.map((keywordTmp) =>
        keywordTmp.id === keyword.id
          ? { ...keywordTmp, selected: !keywordTmp.selected }
          : keywordTmp
      )
    );
  }

  /**
   * Indicates whether there are any keywords available to add.
   *
   * @returns `true` if at least one available keyword exists, otherwise `false`.
   */
  public hasKeywords(): boolean {
    return this.availableKeywords().length > 0;
  }

  /**
   * Closes the modal, cancelling the add-keyword wizard.
   *
   * @param _action The wizard action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);
  }

  /**
   * Persists all currently selected keywords onto the document via the
   * {@link KeywordsService}. When at least one keyword is added, the available
   * keyword list is refreshed and a success {@link ActionEmitterResult} is
   * emitted through {@link addedKeyword}. Finally clears the loading flag and
   * closes the modal.
   *
   * @returns A promise that resolves once the selected keywords have been processed.
   */
  public async add() {
    let keywordsAdded = false;
    this.adding.set(true);
    for (const keyword of this.availableKeywords()) {
      const documentNode = this.documentNode();
      if (keyword.selected && documentNode.id) {
        await this.keywordsService.postKeywordAsync({
          id: documentNode.id,
          keywordDefinition: keyword,
        });
        keywordsAdded = true;
      }
    }

    if (keywordsAdded) {
      await this.loadAvailableKeywords();
      const result: ActionEmitterResult = {};
      result.type = ActionType.ADD_KEYWORD;
      result.result = ActionResult.SUCCEED;
      this.addedKeyword.emit(result);
    }
    this.adding.set(false);
    this.showModal.set(false);
  }
}
