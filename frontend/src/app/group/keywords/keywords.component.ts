import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroup,
  InterestGroupService,
  KeywordsService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { getSuccessTranslation } from 'app/core/util';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { CreateKeywordComponent } from './create/create-keyword.component';
import { DeleteKeywordComponent } from './delete/delete-keyword.component';
import { DeleteMultiKeywordsComponent } from './delete/delete-multi-keywords.component';
import { ImportKeywordComponent } from './import-keyword/import-keyword.component';
import { KeywordTagComponent } from './tag/keyword-tag.component';

/**
 * Component that renders the keyword management view for an interest group's
 * document library.
 *
 * It displays the list of keyword definitions configured for a group and
 * provides the tooling to manage them: single- and multi-selection, creation,
 * update, single/bulk deletion, import from a file and export (bulk download)
 * of the keywords as an Excel file. Administrative actions are only exposed
 * when the current user is a library administrator.
 *
 * The component reads the interest group id from the active route, then loads
 * the group, its library node and the keyword definitions from the backend.
 *
 * It composes several child components (create, delete, delete-multi, import,
 * keyword tag) and relays their action-result events to refresh its own state
 * and surface success/error messages.
 *
 * Key collaborators:
 * - {@link KeywordsService} to fetch keyword definitions.
 * - {@link InterestGroupService} / {@link NodesService} to resolve the group
 *   and its library node.
 * - {@link PermissionEvaluatorService} to check library-admin permissions.
 * - {@link UiMessageService} to display success/error notifications.
 * - {@link TranslocoService} to translate user-facing messages.
 * - {@link SaveAsService} to trigger the bulk keyword download.
 */
@Component({
  selector: 'cbc-keywords',
  templateUrl: './keywords.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    NumberBadgeComponent,
    KeywordTagComponent,
    DeleteKeywordComponent,
    DeleteMultiKeywordsComponent,
    CreateKeywordComponent,
    ImportKeywordComponent,
    TranslocoModule,
  ],
})
export class KeywordsComponent implements OnInit {
  private readonly keywordsService = inject(KeywordsService);
  private readonly route = inject(ActivatedRoute);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly groupService = inject(InterestGroupService);
  private readonly nodesService = inject(NodesService);
  private readonly saveAsService = inject(SaveAsService);
  private readonly loadingService = inject(LoadingService);

  /** All keyword definitions loaded for the current group, each flagged as selectable. */
  public readonly keywords = signal<SelectableKeyword[]>([]);
  /** Subset of {@link keywords} currently marked as selected. */
  public readonly selection = signal<SelectableKeyword[]>([]);
  /** Identifier of the interest group whose keywords are being managed (from the route). */
  public readonly nodeId = signal<string>('');
  /** The interest group resolved from {@link nodeId}. */
  public currentIg!: InterestGroup;
  /** The library node of {@link currentIg}, used for permission checks. */
  public readonly currentLibrary = signal<ModelNode | undefined>(undefined);
  /** Whether the multi-keyword delete wizard/modal is displayed. */
  public readonly showMultipleDeleteWizard = signal<boolean>(false);
  /** Whether the create/update keyword modal is displayed. */
  public showCreateModal = false;
  /** Whether the import keyword modal is displayed. */
  public showImportModal = false;
  /** Keyword passed to the create modal when editing an existing keyword. */
  public selectedKeyword!: SelectableKeyword;
  /** Whether an asynchronous load operation is in progress. */
  public readonly loading = signal<boolean>(false);
  /** Base API path used to build the bulk-download URL. */
  private readonly basePath!: string;
  /** Whether the "add keyword" dropdown menu is expanded. */
  public showAddDropdown = false;

  /** Signal tracking whether every keyword in the list is currently selected. */
  allSelected = signal<boolean>(false);

  /**
   * Initializes the component and captures the injected {@link BASE_PATH}
   * token into {@link basePath} when it is provided.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }
  }

  /**
   * Angular lifecycle hook. Resets the keyword list and subscribes to route
   * parameter changes, reloading the component whenever the group id changes.
   */
  ngOnInit() {
    this.keywords.set([]);
    this.route.params.subscribe(
      async (params) => await this.loadComponent(params)
    );
  }

  /**
   * Loads the interest group, its library node and keyword definitions for the
   * group id contained in the route parameters. Displays an error message when
   * no group id is present.
   *
   * @param params - The route parameters; the `id` entry holds the group id.
   * @returns A promise that resolves once loading has completed.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async loadComponent(params: { [key: string]: any }) {
    this.nodeId.set(params.id);
    if (this.nodeId() === undefined) {
      const res = this.translateService.translate('error.keywords.read');
      this.uiMessageService.addErrorMessage(res);
      return;
    }
    await this.loadingService.run(
      this.loading,
      async () => {
        this.currentIg = await this.groupService.getInterestGroupAsync({
          id: this.nodeId(),
        });

        if (this.currentIg.libraryId) {
          this.currentLibrary.set(
            await this.nodesService.getNodeAsync({
              id: this.currentIg.libraryId,
            })
          );
        }

        this.keywords.set(
          await this.keywordsService.getKeywordDefinitionsAsync({
            id: this.nodeId(),
          })
        );
      },
      'error.keywords.read'
    );
  }

  /**
   * Toggles the selection state of all keywords at once, flipping between
   * "all selected" and "none selected", then refreshes {@link selection}.
   */
  public toggleSelect() {
    const keywords = this.keywords();
    if (keywords) {
      keywords.forEach((keyword: SelectableKeyword) => {
        if (this.allSelected()) {
          keyword.selected = false;
        } else {
          keyword.selected = true;
        }
      });
    }
    this.allSelected.set(!this.allSelected());
    this.remapSelection();
  }

  /**
   * Toggles the selection state of a single keyword and refreshes
   * {@link selection}.
   *
   * @param keyword - The keyword whose selection state should be toggled.
   */
  public toggleSelected(keyword: SelectableKeyword) {
    this.keywords().forEach((keywordTmp) => {
      if (keywordTmp.id === keyword.id) {
        keywordTmp.selected = !keywordTmp.selected;
      }
    });

    this.remapSelection();
  }

  /**
   * Rebuilds {@link selection} from the keywords currently flagged as selected.
   */
  private remapSelection() {
    this.selection.set(
      this.keywords().filter((keywordTmp) => keywordTmp.selected)
    );
  }

  /**
   * Handles the result emitted after a single keyword deletion. On success it
   * reloads the component and shows a success message.
   *
   * @param result - The action-result emitted by the delete keyword child component.
   * @returns A promise that resolves once any reload has completed.
   */
  public async afterKeywordDeletion(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_KEYWORD
    ) {
      await this.loadComponent({ id: this.nodeId() });
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_KEYWORD)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  /**
   * Opens the wizard used to delete all selected keywords.
   */
  public showDeleteAllModal() {
    this.showMultipleDeleteWizard.set(true);
  }

  /**
   * Handles the result emitted after a bulk ("delete all") keyword deletion.
   * On success it reloads the component, clears the current {@link selection}
   * and shows a success message. Always closes the multi-delete wizard.
   *
   * @param result - The action-result emitted by the multi-delete child component.
   * @returns A promise that resolves once any reload has completed.
   */
  public async refreshAfterAllDeletion(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_ALL
    ) {
      await this.loadComponent({ id: this.nodeId() });
      this.selection.set([]);
      // success message not managed by the action-url.ts patterns,
      // because in that case it appears during creation and not afterwards
      // to make it appear afterwards:
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_KEYWORD)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }

    this.showMultipleDeleteWizard.set(false);
  }

  /**
   * Handles the result emitted after creating, updating or importing keywords.
   * Closes the create/import modals, reloads the component and, on a successful
   * create or update, shows the corresponding success message.
   *
   * @param result - The action-result emitted by the create/import child component.
   * @returns A promise that resolves once the reload has completed.
   */
  public async refreshAfterCreation(result: ActionEmitterResult) {
    this.showCreateModal = false;
    this.showImportModal = false;
    await this.loadComponent({ id: this.nodeId() });
    // success message not managed by the action-url.ts patterns,
    // because in that case it appears during creation and not afterwards
    // to make it appear afterwards:
    if (
      result.result === ActionResult.SUCCEED &&
      (result.type === ActionType.CREATE_KEYWORD ||
        result.type === ActionType.UPDATE_KEYWORD)
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(
          result.type === ActionType.CREATE_KEYWORD
            ? ActionType.ADD_KEYWORD
            : ActionType.UPDATE_KEYWORD
        )
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  /**
   * Opens the create modal in edit mode for the given keyword.
   *
   * @param keyword - The keyword to be edited.
   */
  public showUpdateKeyword(keyword: SelectableKeyword) {
    this.showCreateModal = true;
    this.selectedKeyword = keyword;
  }

  /**
   * Determines whether the current user is an administrator of the group's
   * library.
   *
   * @returns `true` when the library node is loaded and the user has
   *   library-admin permissions, otherwise `false`.
   */
  public isLibAdmin(): boolean {
    const currentLibrary = this.currentLibrary();
    if (currentLibrary) {
      return this.permEvalService.isLibAdmin(currentLibrary);
    }
    return false;
  }

  /**
   * Triggers a browser download of all keywords of the current group as an
   * Excel (`.xls`) file, using the group's bulk keyword endpoint.
   */
  public bulkDownload() {
    const url = `${this.basePath}/groups/${this.currentIg.id}/keywords/bulk`;
    const name = `Keywords.${this.currentIg.name}.xls`;
    this.saveAsService.saveUrlAs(url, name);
  }
}
