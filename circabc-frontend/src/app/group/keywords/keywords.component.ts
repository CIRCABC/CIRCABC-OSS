import { Component, Inject, OnInit, Optional, signal } from '@angular/core';
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
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { getSuccessTranslation } from 'app/core/util';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { firstValueFrom } from 'rxjs';
import { CreateKeywordComponent } from './create/create-keyword.component';
import { DeleteKeywordComponent } from './delete/delete-keyword.component';
import { DeleteMultiKeywordsComponent } from './delete/delete-multi-keywords.component';
import { ImportKeywordComponent } from './import-keyword/import-keyword.component';
import { KeywordTagComponent } from './tag/keyword-tag.component';

@Component({
  selector: 'cbc-keywords',
  templateUrl: './keywords.component.html',
  preserveWhitespaces: true,
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
  public keywords: SelectableKeyword[] = [];
  public selection: SelectableKeyword[] = [];
  public nodeId!: string;
  public currentIg!: InterestGroup;
  public currentLibrary!: ModelNode;
  public showMultipleDeleteWizard = false;
  public showCreateModal = false;
  public showImportModal = false;
  public selectedKeyword!: SelectableKeyword;
  public loading = false;
  private basePath!: string;
  public showAddDropdown = false;

  allSelected = signal<boolean>(false);

  constructor(
    private keywordsService: KeywordsService,
    private route: ActivatedRoute,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private permEvalService: PermissionEvaluatorService,
    private groupService: InterestGroupService,
    private nodesService: NodesService,
    private saveAsService: SaveAsService,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  ngOnInit() {
    this.keywords = [];
    this.route.params.subscribe(
      async (params) => await this.loadComponent(params)
    );
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async loadComponent(params: { [key: string]: any }) {
    this.loading = true;
    this.nodeId = params.id;
    if (this.nodeId !== undefined) {
      this.currentIg = await firstValueFrom(
        this.groupService.getInterestGroup(this.nodeId)
      );

      if (this.currentIg.libraryId) {
        this.currentLibrary = await firstValueFrom(
          this.nodesService.getNode(this.currentIg.libraryId)
        );
      }

      this.keywords = await firstValueFrom(
        this.keywordsService.getKeywordDefinitions(this.nodeId)
      );
    } else {
      const res = this.translateService.translate('error.keywords.read');
      this.uiMessageService.addErrorMessage(res);
    }
    this.loading = false;
  }

  public toggleSelect() {
    if (this.keywords) {
      this.keywords.forEach((keyword: SelectableKeyword) => {
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

  public toggleSelected(keyword: SelectableKeyword) {
    this.keywords.forEach((keywordTmp) => {
      if (keywordTmp.id === keyword.id) {
        keywordTmp.selected = !keywordTmp.selected;
      }
    });

    this.remapSelection();
  }

  private remapSelection() {
    this.selection = [];

    this.keywords.forEach((keywordTmp) => {
      if (keywordTmp.selected) {
        this.selection.push(keywordTmp);
      }
    });
  }

  public async afterKeywordDeletion(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_KEYWORD
    ) {
      await this.loadComponent({ id: this.nodeId });
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_KEYWORD)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  public showDeleteAllModal() {
    this.showMultipleDeleteWizard = true;
  }

  public async refreshAfterAllDeletion(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_ALL
    ) {
      await this.loadComponent({ id: this.nodeId });
      this.selection = [];
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

    this.showMultipleDeleteWizard = false;
  }

  public async refreshAfterCreation(result: ActionEmitterResult) {
    this.showCreateModal = false;
    this.showImportModal = false;
    await this.loadComponent({ id: this.nodeId });
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

  public showUpdateKeyword(keyword: SelectableKeyword) {
    this.showCreateModal = true;
    this.selectedKeyword = keyword;
  }

  public isLibAdmin(): boolean {
    if (this.currentLibrary) {
      return this.permEvalService.isLibAdmin(this.currentLibrary);
    }
    return false;
  }

  public bulkDownload() {
    const url = `${this.basePath}/groups/${this.currentIg.id}/keywords/bulk`;
    const name = `Keywords.${this.currentIg.name}.xls`;
    this.saveAsService.saveUrlAs(url, name);
  }
}
