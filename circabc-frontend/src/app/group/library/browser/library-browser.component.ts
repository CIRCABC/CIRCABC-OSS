import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';

import { AbstractControl, FormBuilder, FormGroup } from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ContentService,
  ColumnOptions,
  ExternalRepositoryData,
  ListingOptions,
  Node as ModelNode,
  NotificationService,
  PreferenceConfiguration,
  User,
  UserService,
} from 'app/core/generated/circabc';

import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableNode } from 'app/core/ui-model/index';
import {
  changeSort,
  getErrorTranslation,
  getSuccessTranslation,
  isContentPreviewable,
} from 'app/core/util';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { environment } from 'environments/environment';
import { CookieService } from 'ngx-cookie-service';
import { debounceTime } from 'rxjs/operators';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'cbc-library-browser',
  templateUrl: './library-browser.component.html',
  styleUrls: ['./library-browser.component.scss'],
  preserveWhitespaces: true,
})
export class LibraryBrowserComponent implements OnInit {
  private listingOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: 'modified_DESC',
  };

  private columnOptions: ColumnOptions = {
    name: true,
    title: true,
    version: true,
    modification: true,
    creation: false,
    size: true,
    expiration: true,
    status: false,
    description: true,
    author: false,
    securityRanking: false,
  };

  @Input()
  public contents!: SelectableNode[];
  @Input()
  public parent!: ModelNode;
  @Input()
  public preferences: PreferenceConfiguration = {
    library: {
      column: this.columnOptions,
      listing: this.listingOptions,
    },
  };
  @Input()
  public totalItems = 25;
  @Input()
  treeView = false;
  @Input()
  restrictedMode = false;
  @Input()
  igId: string | undefined;
  @Input()
  resetPage!: boolean;

  @Output()
  public readonly needRefresh = new EventEmitter<ActionEmitterResult>();
  @Output()
  public readonly changedPage = new EventEmitter<number>();
  @Output()
  public readonly changedListing = new EventEmitter<ListingOptions>();
  @Output()
  public readonly treeViewChange = new EventEmitter();

  public columnForm!: FormGroup;
  public selection!: SelectableNode[];
  public showMultipleDeleteWizard = false;
  public localListingOptions!: ListingOptions;
  public itemToClipboard: string | undefined;
  public selecteditemsToClipboard = false;
  public showPreview = false;
  public contentURL!: string;
  public previewDocumentId!: string;
  public contentToPreview!: SelectableNode;
  private dummyPreviewUrlChange = false;
  private user!: User;
  private currentIgId!: string;
  public processing = false;
  public bulkDownloading = false;
  private allSelectedValue = false;
  public isSubscribedToNotifications!: boolean;
  private isAresBridgeEnabled = false;
  @Output()
  readonly allSelectedChange = new EventEmitter();
  @Input()
  get allSelected() {
    return this.allSelectedValue;
  }
  set allSelected(value: boolean) {
    this.allSelectedValue = value;
    this.allSelectedChange.emit(this.allSelectedValue);
  }

  public constructor(
    private router: Router,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private permEvalService: PermissionEvaluatorService,
    private clipboardService: ClipboardService,
    private contentService: ContentService,
    private notificationService: NotificationService,
    private loginService: LoginService,
    private bulkDownloadPipe: BulkDownloadPipe,
    private saveAsService: SaveAsService,
    private permissionEvaluator: PermissionEvaluator,
    private location: Location,
    private i18nPipe: I18nPipe,
    private cookieService: CookieService,
    private fb: FormBuilder,
    private userService: UserService,
    private aresBridgeHelperService: AresBridgeHelperService,
    private dialog: MatDialog
  ) {
    // analyses which condition should make the shared space message disappear...
    this.router.events.subscribe((value) => {
      if (value instanceof NavigationEnd) {
        const newIgId = value.url.substring(7, 7 + 36);
        if (
          this.currentIgId !== undefined &&
          this.currentIgId !== newIgId &&
          !value.url.includes('fromLink=true')
        ) {
          this.removeStoredLinkNavigationInfo();
        }
        this.currentIgId = newIgId;
      }
    });
    this.router.routeReuseStrategy.shouldReuseRoute = () => {
      return false;
    };
  }

  public async ngOnInit() {
    if (this.user === undefined || this.user === null) {
      this.user = this.loginService.getUser();
    }

    if (this.preferences.library && this.preferences.library.listing) {
      this.localListingOptions = this.preferences.library.listing;
    }

    if (this.preferences.library && this.preferences.library.column) {
      this.columnForm = this.fb.group(this.preferences.library.column);
    }

    this.columnForm.valueChanges.pipe(debounceTime(1000)).subscribe((value) => {
      this.saveColumnConfiguration(value);
    });

    this.isSubscribedToNotifications =
      await this.calculateIsSubscribedToNotifications();
    if (this.igId) {
      if (!this.isGuest()) {
        this.isAresBridgeEnabled =
          await this.aresBridgeHelperService.isAresBridgeEnabled(this.igId);
      }
    }
  }

  public async onDeletedElement(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_SPACE
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_SPACE)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    } else if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_CONTENT
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_CONTENT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }

    this.needRefresh.emit(result);
  }

  public changePage(p: number) {
    this.localListingOptions.page = p;
    this.changedPage.emit(this.localListingOptions.page);
  }

  public changeSort(sort: string) {
    this.localListingOptions.sort = changeSort(
      this.localListingOptions.sort,
      sort
    );
    this.changedListing.emit(this.localListingOptions);
  }

  public changeLimit(limit: number) {
    this.localListingOptions.limit = limit;
    this.localListingOptions.page = 1;
    this.changedListing.emit(this.localListingOptions);
  }

  public toggleSelect() {
    if (this.contents) {
      this.contents.forEach((content: SelectableNode) => {
        if (!this.locked(content) && !this.workingCopy(content)) {
          if (!this.allSelected) {
            content.selected = true;
          } else {
            content.selected = false;
          }
        }
      });
    }
    this.allSelected = !this.allSelected;
  }

  public toggleSelected(content: SelectableNode): boolean {
    if (!this.locked(content) && !this.workingCopy(content)) {
      this.contents.forEach((contentTmp) => {
        const idx = this.contents.indexOf(contentTmp);
        if (contentTmp.id === content.id) {
          contentTmp.selected = !contentTmp.selected;
          this.contents[idx] = contentTmp;
        }
      });

      return true;
    } else {
      return false;
    }
  }

  public showDeleteAllModal() {
    this.selection = [];

    this.contents.forEach((content: SelectableNode) => {
      if (content.selected) {
        this.selection.push(content);
      }
    });

    this.showMultipleDeleteWizard = true;
  }

  public async afterMultipleDeletion(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_ALL
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_ALL)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
      this.toggleSelect();
    } else if (
      result.result === ActionResult.FAILED &&
      result.type === ActionType.DELETE_ALL
    ) {
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.DELETE_ALL)
      );
      if (text) {
        this.uiMessageService.addErrorMessage(text, true);
      }
    }
    this.showMultipleDeleteWizard = false;
    this.needRefresh.emit(result);
  }
  public isLibAdmin(selectableNode: SelectableNode) {
    return this.permEvalService.isLibAdmin(selectableNode);
  }

  public isOwner(selectableNode: SelectableNode) {
    if (this.user && this.user.userId) {
      return this.permEvalService.isOwner(selectableNode, this.user.userId);
    } else {
      return false;
    }
  }

  isFile(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') === -1 && !this.isLibraryLink(node);
    } else {
      return false;
    }
  }

  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1 && !this.isLibraryLink(node);
    } else {
      return false;
    }
  }

  isLink(node: ModelNode): boolean {
    if (node.properties && node.properties.mimetype && node.properties.url) {
      return node.properties.isUrl === 'true';
    } else {
      return false;
    }
  }

  getDestinationId(destination: string): string {
    const destinationPart = destination.split('/');
    if (destinationPart.length === 4) {
      return destinationPart[3];
    } else {
      return '';
    }
  }

  private isLibraryItem(
    node: ModelNode,
    item: 'filelink' | 'folderlink'
  ): boolean {
    if (node.type) {
      return node.type.indexOf(item) !== -1;
    } else {
      return false;
    }
  }

  isLibraryLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'filelink');
  }

  isSharedSpaceLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'folderlink');
  }

  hasExpirationDate(node: ModelNode): boolean {
    return (
      node.properties !== undefined &&
      node.properties.expiration_date !== undefined
    );
  }

  removeStoredLinkNavigationInfo() {
    localStorage.removeItem(`sharedSpaceLink-${this.parent.id}`);
  }

  isStoredInSharedLinkNavigationInfo(): boolean {
    return (
      localStorage.getItem(`sharedSpaceLink-${this.parent.id}`) !== null &&
      this.igId !== this.getOriginIgId()
    );
  }

  getOriginIgId(): string {
    return JSON.parse(
      localStorage.getItem(`sharedSpaceLink-${this.parent.id}`) as string
    ).originIgId;
  }

  getOriginId(): string {
    return JSON.parse(
      localStorage.getItem(`sharedSpaceLink-${this.parent.id}`) as string
    ).originId;
  }

  openSharedLinkNavigation(node: ModelNode) {
    if (
      node.properties &&
      (node.properties.destinationIgId || node.properties.destinationId)
    ) {
      this.storeSharedLinkNavigationInfo(node);
      this.router.navigate(
        [
          `/group/${node.properties.destinationIgId}/library/${node.properties.destinationId}`,
        ],
        { queryParams: { fromLink: 'true' } }
      );
    } else {
      const text = this.translateService.translate('spaces.sharing.error');
      this.uiMessageService.addErrorMessage(text);
    }
  }

  storeSharedLinkNavigationInfo(node: ModelNode) {
    if (node.properties) {
      this.localListingOptions.page = 1;
      localStorage.setItem(
        `sharedSpaceLink-${node.properties.destinationId}`,
        JSON.stringify({
          originIgId: node.properties.originIgId,
          originId: node.parentId,
        })
      );
    }
  }

  public addToClipboard(content: SelectableNode) {
    this.clipboardService.addItem(content);
    this.itemToClipboard = content.name;
  }

  public addSelectedToClipboard() {
    this.contents.forEach((node: SelectableNode) => {
      if (
        node.selected &&
        node.properties !== undefined &&
        node.properties.locked !== 'true'
      ) {
        this.clipboardService.addItem(node);
      }
    });

    this.selecteditemsToClipboard = true;
  }

  public getSelectedNodes() {
    return this.contents
      .filter((node: SelectableNode) => node.selected)
      .map((node: SelectableNode) => node.id);
  }

  public isPublic(content: ModelNode) {
    return (
      content.properties !== undefined &&
      (content.properties.security_ranking === undefined ||
        content.properties.security_ranking === 'PUBLIC')
    );
  }

  public async changeNotificationSubscription(value: string) {
    if (value && value !== '' && this.parent.id) {
      try {
        await firstValueFrom(
          this.notificationService.putNotificationAuthority(
            this.parent.id,
            this.loginService.getCurrentUsername(),
            value
          )
        );
        this.isSubscribedToNotifications =
          await this.calculateIsSubscribedToNotifications();
      } catch (error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  public async bulkDownload() {
    const url = this.bulkDownloadPipe.transform(this.getSelectedNodes());
    const name = 'bulk.zip';
    this.bulkDownloading = true;
    try {
      await this.saveAsService.saveUrlAsync(url, name);
    } finally {
      this.bulkDownloading = false;
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  private async calculateIsSubscribedToNotifications() {
    let result = false;
    if (this.parent.id && !this.isGuest()) {
      const isSubscribed = await firstValueFrom(
        this.notificationService.isUserSubscribed(
          this.parent.id,
          this.loginService.getCurrentUsername()
        )
      );
      result = isSubscribed.subscribed;
    }
    return result;
  }

  public isPreviewable(content: SelectableNode): boolean {
    return isContentPreviewable(content);
  }

  public previewContent(content: SelectableNode) {
    this.contentToPreview = content;
    this.dummyPreviewUrlChange = !this.dummyPreviewUrlChange;
    this.contentURL = `${
      environment.serverURL
    }pdfRendition?documentId=workspace://SpacesStore/${
      content.id
    }&response=content&ticket=${this.loginService.getTicket()}&dummy=${
      this.dummyPreviewUrlChange
    }`;
    this.previewDocumentId = content.id as string;
    this.showPreview = true;
  }

  public closePreview() {
    this.showPreview = false;
  }

  public canEditInOffice(content: SelectableNode) {
    return (
      this.isOfficeIntegrationEnabled() &&
      this.isFile(content) &&
      content.name !== undefined &&
      (content.name.endsWith('.docx') ||
        content.name.endsWith('.xlsx') ||
        content.name.endsWith('.pptx')) &&
      content?.properties?.locked !== 'true' &&
      content?.properties?.editInline !== 'true'
    );
  }

  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }

  public openDocInOffice(content: SelectableNode) {
    const id = content?.properties?.workingCopyId ?? content.id;
    if (content?.properties?.workingCopy !== 'true') {
      setTimeout(() => location.reload(), 10000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=edit`;
      window.open(url, '_blank');
    } else {
      setTimeout(() => location.reload(), 2000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=update`;
      window.open(url, '_blank');
    }
  }

  public isEditOnline(content: SelectableNode): boolean {
    if (this.loginService.isGuest()) {
      return false;
    }
    return (
      content?.properties?.editInline === 'true' &&
      this.loginService.getUser().userId ===
        content?.properties?.workingCopyOwner
    );
  }

  public async zipAndEmailMe() {
    let text: string;
    this.processing = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.EMAIL_CONTENTS;
    try {
      if (this.parent.id !== undefined) {
        const selection: string[] = [];

        this.contents.forEach((content: SelectableNode) => {
          if (content.selected) {
            selection.push(content.id as string);
          }
        });

        const status = await firstValueFrom(
          this.contentService.postContentsEmail(this.parent.id, selection)
        );
        if (!status.result) {
          text = this.translateService.translate(
            getErrorTranslation(ActionType.EMAIL_CONTENTS),
            {
              name: `${this.user.firstname} ${this.user.lastname}`,
              email: this.user.email,
            }
          );
          this.uiMessageService.addErrorMessage(text);
          this.processing = false;
          this.needRefresh.emit(result);
          return;
        }
        text = this.translateService.translate(
          getSuccessTranslation(ActionType.EMAIL_CONTENTS),
          {
            name: `${this.user.firstname} ${this.user.lastname}`,
            email: this.user.email,
          }
        );
        this.uiMessageService.addSuccessMessage(text);
      } else {
        text = this.translateService.translate(
          getErrorTranslation(ActionType.EMAIL_CONTENTS),
          {
            name: `${this.user.firstname} ${this.user.lastname}`,
            email: this.user.email,
          }
        );
        this.uiMessageService.addErrorMessage(text);
      }
    } catch (error) {
      text = this.translateService.translate(
        getErrorTranslation(ActionType.EMAIL_CONTENTS),
        {
          name: `${this.user.firstname} ${this.user.lastname}`,
          email: this.user.email,
        }
      );
      const jsonError = JSON.parse(error._body);
      if (jsonError) {
        this.uiMessageService.addErrorMessage(
          `${text} -> ${jsonError.message as string}`
        );
      }
    }
    this.processing = false;
    this.needRefresh.emit(result);
  }

  public canSendToAresBridge() {
    if (this.user?.properties?.domain === 'external') {
      return false;
    }
    if (!environment.aresBridgeEnabled) {
      return false;
    }
    if (!this.isAresBridgeEnabled) {
      return false;
    }

    if (this.isFolderSelected()) {
      return false;
    }

    if (environment.circabcRelease !== 'oss') {
      return this.areNodesDeletable();
    } else {
      return false;
    }
  }

  private isFolderSelected() {
    return this.contents.some((node) => node.selected && this.isFolder(node));
  }

  public async sendToAresBridge() {
    const selectedNodes: SelectableNode[] = [];
    const nodeLogDuplicated: ExternalRepositoryData[] = [];

    this.contents.forEach((content: SelectableNode) => {
      if (content.selected && this.isFile(content)) {
        selectedNodes.push(content);
      }
    });

    if (selectedNodes.length === 0) {
      return;
    }
    if (this.processing) {
      return;
    }

    const sentNodes =
      await this.aresBridgeHelperService.getAlreadySentToAresBridge(
        selectedNodes
      );

    this.processing = true;

    //map all logs of selectedNodes by saveNumber
    const result = this.groupBy(sentNodes, (log) => log.saveNumber);

    selectedNodes.forEach((e) => {
      result.forEach((element, index) => {
        //remove index if node not include
        if (!element.some((elem) => elem.nodeId === e.id)) {
          result.delete(index);
        }
        //remove index if has different version
        if (
          !element.some(
            (elem) => elem.versionLabel === e.properties?.versionLabel
          )
        ) {
          result.delete(index);
        }
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    result.forEach((value: any) => {
      if (selectedNodes.length === 1) {
        if (
          selectedNodes[0].properties?.versionLabel === value[0].versionLabel
        ) {
          nodeLogDuplicated.push(value[0]);
        }
      } else {
        nodeLogDuplicated.push(value[0]);
      }
    });

    if (nodeLogDuplicated.length > 0) {
      await this.showDuplicatesAresBrigeDialog(nodeLogDuplicated);
      this.processing = false;
      return;
    }

    if (sentNodes.length > 0) {
      await this.showConfirmationDialog(selectedNodes, sentNodes);
      this.processing = false;
      return;
    }
    try {
      await this.aresBridgeHelperService.sendToAresBridge(selectedNodes);
    } finally {
      this.processing = false;
    }
  }

  groupBy<K, V>(arrayr: V[], grouper: (item: V) => K) {
    return arrayr.reduce((store, item) => {
      const key = grouper(item);
      if (!store.has(key)) {
        store.set(key, [item]);
      } else {
        store.get(key)?.push(item);
      }
      return store;
    }, new Map<K, V[]>());
  }

  private async showDuplicatesAresBrigeDialog(
    nodeLogDuplicated: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'label.ares.bridge.resend.title',
        layoutStyle: 'sendDuplicatesManyToAres',
        nodeLog: nodeLogDuplicated,
      },
    });

    // listen to response
    await firstValueFrom(dialogRef.afterClosed());
  }

  private async showConfirmationDialog(
    nodes: SelectableNode[],
    nodeLog: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'label.ares.bridge.resend.title',
        layoutStyle: 'sendManyToAres',
        nodeLog: nodeLog,
      },
    });

    // listen to response
    const dialogResult = await firstValueFrom(dialogRef.afterClosed());
    if (dialogResult) {
      await this.aresBridgeHelperService.sendToAresBridge(nodes);
    }
  }

  get isNotItemToClipboardUndefined(): boolean {
    return this.itemToClipboard !== undefined;
  }

  get isNotSelectedItemsToClipboardUndefined(): boolean {
    return this.selecteditemsToClipboard !== false;
  }

  public url(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.url;
    } else {
      return '';
    }
  }

  public locale(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.locale;
    } else {
      return '';
    }
  }

  public versionLabel(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.versionLabel;
    } else {
      return '';
    }
  }
  public locked(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.locked === 'true';
    } else {
      return false;
    }
  }
  public workingCopy(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.workingCopy === 'true';
    } else {
      return false;
    }
  }
  public multilingual(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.multilingual;
    } else {
      return 'false';
    }
  }

  public modified(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.modified;
    } else {
      return null;
    }
  }

  public created(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.created;
    } else {
      return null;
    }
  }

  public size(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.size;
    } else {
      return null;
    }
  }

  public modifier(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.modifier;
    } else {
      return '';
    }
  }

  public creatoOrOwner(content: SelectableNode): string {
    if (content.properties) {
      return !content.properties.owner
        ? content.properties.creator
        : content.properties.owner;
    } else {
      return '';
    }
  }

  public getSelectCheckboxTooltip(content: SelectableNode): string {
    if (this.locked(content) || this.workingCopy(content)) {
      return 'label.locked.no.deletion';
    }

    return '';
  }

  public areNodesDeletable(): boolean {
    let result = true;

    const selecteds: SelectableNode[] = [];
    this.contents.forEach((node: SelectableNode) => {
      if (node.selected) {
        selecteds.push(node);
      }
    });

    for (const node of selecteds) {
      if (
        !this.permissionEvaluator.hasAnyOfPermissions(
          node,
          ['LibManageOwn', 'LibAdmin', 'LibFullEdit'],
          ['LibFullEdit', 'LibManageOwn']
        )
      ) {
        result = false;
        break;
      }
    }

    return result;
  }

  public toggleTreeView() {
    this.treeView = !this.treeView;
    localStorage.setItem('showTreeView', `${this.treeView}`);
    this.treeViewChange.emit(this.treeView);
  }

  goBack() {
    this.location.back();
  }

  public getTitle(content: ModelNode): string {
    let result = '';

    if (content.title) {
      const title = this.i18nPipe.transform(content.title);

      if (title !== '') {
        result = title;
      }
    }

    return result;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private saveColumnConfiguration(column: any) {
    this.cookieService.set('columnsOptions', JSON.stringify(column));
    this.saveUserPreferences(column);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async saveUserPreferences(column: any) {
    const columnPref = column as ColumnOptions;
    if (
      this.preferences &&
      this.preferences.library &&
      this.preferences.library.column
    ) {
      this.preferences.library.column = columnPref;
    }
    try {
      if (this.user && this.user.userId) {
        await firstValueFrom(
          this.userService.saveUserPreferences(
            this.user.userId,
            this.preferences
          )
        );
      }
    } catch (error) {
      console.error(error);
    }
  }

  public getColumnCount() {
    let count = 3;
    if (this.columnForm) {
      Object.keys(this.columnForm.controls).forEach((key: string) => {
        const control = this.columnForm.get(key);
        if (control instanceof AbstractControl && control.value !== null) {
          if (control.value) {
            count++;
          }
        }
      });
    }
    return count;
  }
  public getTranslationsCount(content: SelectableNode): string {
    return content?.properties?.translations === undefined
      ? '0'
      : '' + (Number(content?.properties?.translations) - 1);
  }
  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
