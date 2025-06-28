import { DatePipe, Location } from '@angular/common';
import { Component, Input, OnInit, output, input, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ColumnOptions,
  ExternalRepositoryData,
  ListingOptions,
  Node as ModelNode,
  NotificationService,
  type PreferenceConfiguration,
  User,
  UserService,
} from 'app/core/generated/circabc';

import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableNode } from 'app/core/ui-model/index';
import {
  changeSort,
  getErrorTranslation,
  getSuccessTranslation,
  isContentPreviewable,
  isContentPreviewableFull,
} from 'app/core/util';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { ContentPreviewExtendedComponent } from 'app/group/library/content-preview-ext/content-preview-ext.component';
import { DeleteActionComponent } from 'app/group/library/delete/delete-action.component';
import { DeleteMultipleComponent } from 'app/group/library/delete/delete-multiple.component';
import { FavouriteSwitchComponent } from 'app/group/library/favourite-switch/favourite-switch.component';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { SnackbarComponent } from 'app/group/library/snackbar/snackbar.component';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HintComponent } from 'app/shared/hint/hint.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { IfRolesPipe } from 'app/shared/pipes/if-roles.pipe';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SaveAsComponent } from 'app/shared/save-as/save-as.component';
import { ShareComponent } from 'app/shared/share/share.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { environment } from 'environments/environment';
import { CookieService } from 'ngx-cookie-service';
import { firstValueFrom } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { AlfrescoService } from 'app/core/alfresco.service';

@Component({
  selector: 'cbc-library-browser',
  templateUrl: './library-browser.component.html',
  styleUrl: './library-browser.component.scss',
  preserveWhitespaces: true,
  imports: [
    RouterLink,
    PagerComponent,
    ShareComponent,
    ReactiveFormsModule,
    DataCyDirective,
    NumberBadgeComponent,
    SpinnerComponent,
    HintComponent,
    FavouriteSwitchComponent,
    UserCardComponent,
    SaveAsComponent,
    DeleteActionComponent,
    SnackbarComponent,
    DeleteMultipleComponent,
    ContentPreviewExtendedComponent,
    ModalComponent,
    DatePipe,
    I18nPipe,
    IfRolesPipe,
    SizePipe,
    TranslocoModule,
    MatDialogModule,
  ],
})
export class LibraryBrowserComponent implements OnInit {
  public readonly MAX_NODES = 160;
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

  public readonly contents = input.required<SelectableNode[]>();
  public readonly parent = input.required<ModelNode>();
  public readonly preferences = input<PreferenceConfiguration>({
    library: {
      column: this.columnOptions,
      listing: this.listingOptions,
    },
    search: [],
  });
  public readonly totalItems = input(25);
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  treeView = false;
  readonly restrictedMode = input(false);
  readonly igId = input<string>();
  readonly resetPage = input.required<boolean>();

  public readonly needRefresh = output<ActionEmitterResult>();
  public readonly changedPage = output<number>();
  public readonly changedListing = output<ListingOptions>();
  public readonly treeViewChange = output<boolean>();

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
  public allSelected = signal(false);
  public isSubscribedToNotifications!: boolean;
  private isAresBridgeEnabled = false;
  public acceptSncShowModal = false;

  public constructor(
    private router: Router,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private permEvalService: PermissionEvaluatorService,
    private clipboardService: ClipboardService,
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
    private dialog: MatDialog,
    private alfrescoService: AlfrescoService
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

    const preferences = this.preferences();
    if (preferences.library?.listing) {
      this.localListingOptions = preferences.library.listing;
    }

    if (preferences.library?.column) {
      this.columnForm = this.fb.group(preferences.library.column);
    }

    this.columnForm.valueChanges.pipe(debounceTime(1000)).subscribe((value) => {
      this.saveColumnConfiguration(value);
    });

    this.isSubscribedToNotifications =
      await this.calculateIsSubscribedToNotifications();
    const igId = this.igId();
    if (igId) {
      if (!this.isGuest()) {
        this.isAresBridgeEnabled =
          await this.aresBridgeHelperService.isAresBridgeEnabled(igId);
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
    const contents = this.contents();
    if (contents) {
      contents.forEach((content: SelectableNode) => {
        if (!(this.locked(content) || this.workingCopy(content))) {
          if (this.allSelected()) {
            content.selected = false;
          } else {
            content.selected = true;
          }
        }
      });
    }
    this.allSelected.set(!this.allSelected());
  }

  public toggleSelected(content: SelectableNode): boolean {
    if (this.locked(content) || this.workingCopy(content)) {
      return false;
    }
    this.contents().forEach((contentTmp) => {
      const idx = this.contents().indexOf(contentTmp);
      if (contentTmp.id === content.id) {
        contentTmp.selected = !contentTmp.selected;
        this.contents()[idx] = contentTmp;
      }
    });

    return true;
  }

  public showDeleteAllModal() {
    this.selection = [];

    this.contents().forEach((content: SelectableNode) => {
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
    if (this.user?.userId) {
      return this.permEvalService.isOwner(selectableNode, this.user.userId);
    }
    return false;
  }

  isFile(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') === -1 && !this.isLibraryLink(node);
    }
    return false;
  }

  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1 && !this.isLibraryLink(node);
    }
    return false;
  }

  isLink(node: ModelNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return node.properties.isUrl === 'true';
    }
    return false;
  }

  getDestinationId(destination: string): string {
    const destinationPart = destination.split('/');
    if (destinationPart.length === 4) {
      return destinationPart[3];
    }
    return '';
  }

  private isLibraryItem(
    node: ModelNode,
    item: 'filelink' | 'folderlink'
  ): boolean {
    if (node.type) {
      return node.type.indexOf(item) !== -1;
    }
    return false;
  }

  isLibraryLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'filelink');
  }

  isSharedSpaceLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'folderlink');
  }

  hasExpirationDate(node: ModelNode): boolean {
    const expirationDate = node.properties?.expiration_date;
    const hasDate =
      expirationDate !== undefined &&
      expirationDate !== null &&
      expirationDate !== 'null';
    return hasDate;
  }

  removeStoredLinkNavigationInfo() {
    localStorage.removeItem(`sharedSpaceLink-${this.parent().id}`);
  }

  isStoredInSharedLinkNavigationInfo(): boolean {
    return (
      localStorage.getItem(`sharedSpaceLink-${this.parent().id}`) !== null &&
      this.igId() !== this.getOriginIgId()
    );
  }

  getOriginIgId(): string {
    const item = localStorage.getItem(
      `sharedSpaceLink-${this.parent().id}`
    ) as string;
    return (JSON.parse(item) as { originIgId: string }).originIgId;
  }

  getOriginId(): string {
    const item = localStorage.getItem(
      `sharedSpaceLink-${this.parent().id}`
    ) as string;
    return (JSON.parse(item) as { originId: string }).originId;
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
    this.contents().forEach((node: SelectableNode) => {
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
    return this.contents()
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
    const parent = this.parent();
    if (value && value !== '' && parent.id) {
      try {
        await firstValueFrom(
          this.notificationService.putNotificationAuthority(
            parent.id,
            this.loginService.getCurrentUsername(),
            value
          )
        );
        this.isSubscribedToNotifications =
          await this.calculateIsSubscribedToNotifications();
      } catch (_error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  public async bulkDownload() {
    this.acceptSncShowModal = false;

    if (this.getSelectedNodes().length > this.MAX_NODES) {
      const text = this.translateService.translate(
        'label.bulk.download.limit',
        { limit: this.MAX_NODES }
      );
      this.uiMessageService.addErrorMessage(text);
    }
    const url = this.bulkDownloadPipe.transform(this.getSelectedNodes());
    const name = 'bulk.zip';
    this.bulkDownloading = true;
    try {
      await this.saveAsService.saveUrlAsync(url, name);
    } catch (error) {
      if (error.name === 'HttpErrorResponse' && error.status === 507) {
        const text = this.translateService.translate(
          'label.bulk.download.too.big',
          { fileSize: error?.error?.size ?? 2147479168 }
        );
        this.uiMessageService.addErrorMessage(text);
      } else {
        this.uiMessageService.addErrorMessage(error.message);
      }
      console.error(error);
    } finally {
      this.bulkDownloading = false;
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  private async calculateIsSubscribedToNotifications() {
    let result = false;
    const parent = this.parent();
    if (parent.id && !this.isGuest()) {
      const isSubscribed = await firstValueFrom(
        this.notificationService.isUserSubscribed(
          parent.id,
          this.loginService.getCurrentUsername()
        )
      );
      result = isSubscribed.subscribed;
    }
    return result;
  }

  public isPreviewable(content: SelectableNode): boolean {
    if (environment.useAlfrescoAPI) {
      return isContentPreviewableFull(content);
    }
    return isContentPreviewable(content);
  }

  public async previewContent(content: SelectableNode) {
    this.contentToPreview = content;
    this.dummyPreviewUrlChange = !this.dummyPreviewUrlChange;
    if (environment.useAlfrescoAPI) {
      if (!isContentPreviewable(content) && isContentPreviewableFull(content)) {
        try {
          const rendition = await this.alfrescoService.getRendition(
            content.id as string,
            'pdf'
          );
          if (rendition.entry.status !== 'CREATED') {
            await this.alfrescoService.createRendition(
              content.id as string,
              'pdf'
            );
          }
        } catch (error) {
          this.uiMessageService.addErrorMessage(
            this.translateService.translate('preview.messages.not.available'),
            true
          );
          console.error(error);
        }

        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${content.id}/renditions/pdf/content?attachment=true&alf_ticket=${this.loginService.getTicket()}&dummy=${
          this.dummyPreviewUrlChange
        }`;
      } else {
        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${content.id}/content?attachment=false&alf_ticket=${this.loginService.getTicket()}&dummy=${
          this.dummyPreviewUrlChange
        }`;
      }
    } else {
      this.contentURL = `${
        environment.serverURL
      }pdfRendition?documentId=workspace://SpacesStore/${
        content.id
      }&response=content&ticket=${this.loginService.getTicket()}&dummy=${
        this.dummyPreviewUrlChange
      }`;
    }

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
    }
    return false;
  }

  private isFolderSelected() {
    return this.contents().some((node) => node.selected && this.isFolder(node));
  }

  public async sendToAresBridge() {
    const selectedNodes: SelectableNode[] = [];
    const nodeLogDuplicated: ExternalRepositoryData[] = [];

    this.contents().forEach((content: SelectableNode) => {
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
      if (store.has(key)) {
        store.get(key)?.push(item);
      } else {
        store.set(key, [item]);
      }
      return store;
    }, new Map<K, V[]>());
  }

  private async showDuplicatesAresBrigeDialog(
    nodeLogDuplicated: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'label.send.to.ares',
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
        title: 'label.send.to.ares',
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
    }
    return '';
  }

  public locale(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.locale;
    }
    return '';
  }

  public versionLabel(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.versionLabel;
    }
    return '';
  }
  public locked(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.locked === 'true';
    }
    return false;
  }
  public workingCopy(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.workingCopy === 'true';
    }
    return false;
  }
  public multilingual(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.multilingual;
    }
    return 'false';
  }

  public modified(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.modified;
    }
    return null;
  }

  public created(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.created;
    }
    return null;
  }

  public size(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.size;
    }
    return null;
  }

  public modifier(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.modifier;
    }
    return '';
  }

  public creatoOrOwner(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.owner
        ? content.properties.owner
        : content.properties.creator;
    }
    return '';
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
    this.contents().forEach((node: SelectableNode) => {
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
    const preferences = this.preferences();
    if (preferences?.library?.column) {
      preferences.library.column = columnPref;
    }
    try {
      if (this.user?.userId) {
        await firstValueFrom(
          this.userService.saveUserPreferences(this.user.userId, preferences)
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
      : `${Number(content?.properties?.translations) - 1}`;
  }

  public isSensitive(content: SelectableNode) {
    return (
      content?.properties?.security_ranking === 'SENSITIVE' ||
      content?.properties?.security_ranking === 'SPECIAL_HANDLING'
    );
  }

  public getSncFiles() {
    return this.contents()
      .filter(
        (node: SelectableNode) =>
          node.selected &&
          (node?.properties?.security_ranking === 'SENSITIVE' ||
            node?.properties?.security_ranking === 'SPECIAL_HANDLING')
      )
      .map((node: SelectableNode) => node.name);
  }

  public bulkDownloadCheck() {
    if (
      environment.circabcRelease === 'echa' &&
      this.getSncFiles().length > 0
    ) {
      this.acceptSncShowModal = true;
    } else {
      this.bulkDownload();
    }
  }

  get message() {
    return this.translateService.translate('label.dialog.alert.snc.download', {
      link: `<a href=\"https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en\" target=\"_blank\">C(2019)1904</a>`,
    });
  }
}
