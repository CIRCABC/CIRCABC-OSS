import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';

import { TranslocoService } from '@jsverse/transloco';
import {
  type KeywordDefinition,
  KeywordsService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { supportedLanguages } from 'app/shared/langs/supported-langs';
import { CapitalizePipe } from 'app/shared/pipes/capitalize.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders a single keyword as a visual tag/chip.
 *
 * The tag displays the keyword's localized title (resolved against the
 * requested language, the active Transloco language, or the first available
 * translation) and optionally the language it is shown in. When configured as
 * removable, it exposes a control that deletes the keyword from the associated
 * document node via the {@link KeywordsService} and emits {@link removed} so
 * parent components can refresh their view.
 *
 * Key collaborators:
 * - {@link TranslocoService} to determine the active UI language for title resolution.
 * - {@link KeywordsService} (generated CIRCABC API client) to delete keywords.
 *
 * @example
 * ```html
 * <cbc-keyword-tag
 *   [keyword]="keyword"
 *   [documentNode]="node"
 *   [removable]="true"
 *   (removed)="reload()">
 * </cbc-keyword-tag>
 * ```
 */
@Component({
  selector: 'cbc-keyword-tag',
  templateUrl: './keyword-tag.component.html',
  styleUrl: './keyword-tag.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, CapitalizePipe],
})
export class KeywordTagComponent {
  /** Transloco service used to resolve the currently active UI language. */
  private readonly translateService = inject(TranslocoService);
  /** Generated CIRCABC API client used to delete keywords from a document node. */
  private readonly keywordsService = inject(KeywordsService);

  /**
   * Required input holding the keyword definition to render, including its
   * identifier and localized titles.
   */
  readonly keyword = input.required<KeywordDefinition>();
  /**
   * Optional preferred language code used to pick which localized title to
   * display. Falls back to the active language or the first available title.
   */
  readonly lang = input<string>();
  /**
   * Optional document node the keyword is attached to. Required for keyword
   * removal; without it {@link removeKeyword} is a no-op.
   */
  readonly documentNode = input<ModelNode>();
  /** When `true`, the tag renders a control allowing the keyword to be removed. */
  readonly removable = input(false);
  /** When `true`, the tag also displays the language of the shown title. */
  readonly showLang = input(true);
  /** Emitted after the keyword has been successfully removed from the document node. */
  readonly removed = output<void>();

  /** Indicates that a keyword deletion request is currently in progress. */
  public readonly deleting = signal(false);

  /**
   * Determines which title translation key(s) should be used to display the
   * keyword's title.
   *
   * Resolution order when a preferred {@link lang} is provided:
   * 1. the requested language, if a title exists for it;
   * 2. the active Transloco language, if a title exists for it;
   * 3. the first available title language.
   * When no preferred language is set, all available title keys are returned.
   *
   * @param keyword The keyword definition whose title keys are being resolved.
   * @returns An array of language code keys to render the title with.
   */
  public getTitleKeys(keyword: KeywordDefinition): string[] {
    const keywordValue = this.keyword();
    const lang = this.lang();
    if (keywordValue.title && lang) {
      if (Object.keys(keywordValue.title).includes(lang)) {
        return [lang];
      }
      if (
        Object.keys(keywordValue.title).includes(
          this.translateService.getActiveLang()
        )
      ) {
        return [this.translateService.getActiveLang()];
      }
      return [Object.keys(keywordValue.title)[0]];
    }
    return Object.keys(keyword.title);
  }

  /**
   * Removes the given keyword from the current {@link documentNode}.
   *
   * Does nothing when no document node is set, when the component is not
   * {@link removable}, or when either the keyword id or node id is missing.
   * While the deletion request is in flight, {@link deleting} is set to `true`.
   * On success, {@link removed} is emitted.
   *
   * @param keyword The keyword definition to delete.
   * @returns A promise that resolves once the removal attempt has completed.
   */
  public async removeKeyword(keyword: KeywordDefinition) {
    const documentNode = this.documentNode();
    if (documentNode === undefined) {
      return;
    }
    if (this.removable() && keyword.id && documentNode.id) {
      this.deleting.set(true);
      await this.keywordsService.deleteKeywordAsync({
        id: documentNode.id,
        keywordId: keyword.id,
      });
      this.removed.emit();
      this.deleting.set(false);
    }
  }

  /**
   * Resolves the human-readable display name for a given language code.
   *
   * @param langCode The ISO language code to look up.
   * @returns The matching language name, or an empty string if unknown.
   */
  getLanguageName(langCode: string): string {
    for (const lang of supportedLanguages) {
      if (lang.code === langCode) {
        return lang.name;
      }
    }

    return '';
  }
}
