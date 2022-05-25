import { I18nSelectPipe, Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Data, Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ContentService,
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  ExternalRepositoryData,
  GuardsService,
  InterestGroup,
  KeywordDefinition,
  KeywordsService,
  Node as ModelNode,
  NodesService,
  NotificationService,
  OfficeEditResult,
  PagedNodes,
  SpaceService,
  TopicService,
  Translations,
  User,
  Version,
} from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';

import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { Quote } from 'app/core/ui-model/index';
import {
  getErrorTranslation,
  getSuccessTranslation,
  isContentPreviewable,
} from 'app/core/util';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-node-properties',
  templateUrl: './details.component.html',
  styleUrls: ['./details-component.scss'],
  preserveWhitespaces: true,
})
export class DetailsComponent implements OnInit {
  public nodeLog: ExternalRepositoryData[] = [];
  public node!: ModelNode;
  public nodeId!: string;
  public versionLabel!: string;
  public versions!: Version[];
  public keywords!: KeywordDefinition[];
  public topics: ModelNode[] = [];
  public translationSet!: Translations;
  public showUpdateWizard!: boolean;
  public group!: InterestGroup | { id: string };
  public dynamicPropertiesModel: DynamicPropertyDefinition[] = [];
  public comments!: PagedNodes;
  public currentTopic!: ModelNode;
  public futureQuote!: Quote;
  public editPost!: ModelNode;
  public showCreateTopic = false;
  public currentDeletedTopic: ModelNode | undefined;
  public currentEditedTopic: ModelNode | undefined;
  public showDeleteTopic = false;
  public showEditTopic = false;
  public showEnableMultilingualModal = false;
  public restrictedMode = true;
  public destinationId = '';
  public step = 'metadata';
  public folderSize = 0;
  public hasAresLogVersion = false;
  public nodeLogVersionDetail!: ExternalRepositoryData;
  public formAresInfo!: FormGroup;

  public listingCommentOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: '',
  };

  public commentPages: number[] = [];
  public totalCommentItems = 10;

  // for document preview
  public showPreview = false;
  public contentURL!: string;
  public previewDocumentId!: string;
  private dummyPreviewUrlChange = false;

  public user!: User;

  // history modal
  public historyShowModal = false;
  public areslogShowModal = false;
  public processing = false;
  public checkinShowModal = false;
  public cancelCheckoutShowModal = false;
  public loading = false;
  public hasMoreVersions = false;
  public itemToClipboard: string | undefined;
  public officeEditResult: OfficeEditResult[] = [];
  private isAresBridgeEnabled = false;

  public constructor(
    private nodesService: NodesService,
    private route: ActivatedRoute,
    private router: Router,
    private translateService: TranslocoService,
    private contentService: ContentService,
    private loginService: LoginService,
    private permEvalService: PermissionEvaluatorService,
    private uiMessageService: UiMessageService,
    private keywordService: KeywordsService,
    private dynamicPropertiesService: DynamicPropertiesService,
    private topicService: TopicService,
    private clipboardService: ClipboardService,
    private location: Location,
    private notificationService: NotificationService,
    private saveAsService: SaveAsService,
    private i18nPipe: I18nPipe,
    private i18nSelectPipe: I18nSelectPipe,
    private guardsService: GuardsService,
    private aresBridgeHelperService: AresBridgeHelperService,
    private spaceService: SpaceService,
    private dialog: MatDialog,
    private libraryIdService: LibraryIdService
  ) {}

  public ngOnInit() {
    this.ngInit();
  }

  private isInterestGroup(
    group: InterestGroup | { id: string }
  ): group is InterestGroup {
    return (group as InterestGroup).name !== undefined;
  }
  private ngInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.group = value.group;
      if (this.group.id) {
        this.isAresBridgeEnabled =
          await this.aresBridgeHelperService.isAresBridgeEnabled(this.group.id);
      }
    });
    this.route.params.subscribe(async (params) => {
      await this.loadNode(params);
      if (!this.isInterestGroup(this.group)) {
        const interestGroup = await firstValueFrom(
          this.nodesService.getGroup(this.nodeId)
        );
        this.group = interestGroup;
        this.libraryIdService.updateLibraryId(this.group.libraryId as string);
      }

      this.route.queryParams.subscribe((queryParams) => {
        if (
          queryParams.download &&
          queryParams.download === 'true' &&
          this.node.name
        ) {
          this.saveAsService.saveAs(this.nodeId, this.node.name);
        }
      });
    });

    if (this.user === undefined || this.user === null) {
      this.user = this.loginService.getUser();
    }
  }

  public async loadNode(params: { [key: string]: string }) {
    this.loading = true;
    this.nodeId = params.nodeId;
    this.versionLabel = params.versionLabel;

    this.versions = await firstValueFrom(
      this.contentService.getFirstVersions(this.nodeId)
    );

    this.verifyMoreVersions();

    if (this.versionLabel !== undefined && !this.isLastVersionOfContent()) {
      for (const version of this.versions) {
        if (version.versionLabel === this.versionLabel) {
          if (version.node) {
            this.node = version.node;
          }
        }
      }
    } else {
      this.node = await firstValueFrom(this.nodesService.getNode(this.nodeId));
    }

    await this.verifyParentAccess();

    if (
      this.node.properties &&
      this.node.properties.multilingual === 'true' &&
      this.node.id
    ) {
      this.translationSet = await firstValueFrom(
        this.contentService.getTranslations(this.node.id)
      );
    }

    if (this.node.type && this.node.id && this.group.id) {
      await this.loadKeywords(this.node.id);
      await this.loadDynamicPropertiesModel(this.group.id);
      await this.loadTopics(this.node.id);
    }

    this.extractDestinationId();

    this.nodeLog = await this.aresBridgeHelperService.nodeLog(this.nodeId);
    this.loading = false;

    if (this.node.properties !== undefined) {
      this.aresBridgeVersionSelected(this.node.properties.versionLabel);
    }
  }

  private async verifyParentAccess() {
    if (this.node.parentId) {
      try {
        const authorization = await firstValueFrom(
          this.guardsService.getGuardAccess(this.node.parentId)
        );
        if (authorization === null || authorization === undefined) {
          this.restrictedMode = true;
        } else if (
          authorization !== undefined &&
          authorization.granted !== undefined
        ) {
          this.restrictedMode = !authorization.granted;
        }
      } catch (error) {
        if (error.status === 403) {
          this.restrictedMode = true;
        }
      }
    }
  }

  private verifyMoreVersions() {
    if (
      this.versions.length === 10 &&
      this.versions[9].versionLabel !== '1.0'
    ) {
      this.hasMoreVersions = true;
    }
  }

  public async loadAllVersions() {
    this.loading = true;
    this.versions = await firstValueFrom(
      this.contentService.getVersions(this.nodeId)
    );

    this.verifyMoreVersions();
    this.loading = false;
  }

  public extractDestinationId() {
    this.destinationId = '';

    if (
      this.node.type &&
      this.node.type.indexOf('filelink') !== -1 &&
      this.node.properties
    ) {
      this.destinationId = this.getDestinationId(
        this.node.properties.destination
      );
    }
  }

  public async loadKeywords(id: string) {
    this.keywords = await firstValueFrom(this.keywordService.getKeywords(id));
  }

  public async loadDynamicPropertiesModel(id: string) {
    this.dynamicPropertiesModel = await firstValueFrom(
      this.dynamicPropertiesService.getDynamicPropertyDefinitions(id)
    );
  }

  public async loadTopics(id: string) {
    const emptyNodesArray: ModelNode[] = [];
    this.comments = { data: emptyNodesArray, total: 0 };
    this.topics = await firstValueFrom(this.contentService.getTopics(id));
    if (this.topics.length > 0) {
      await this.loadTopic(this.topics[0].id as string);
    }
  }

  public async loadTopic(id: string | undefined) {
    if (id === undefined) {
      return;
    }
    for (const topic of this.topics) {
      if (topic.id === id) {
        this.currentTopic = topic;
      }
    }

    this.comments = await firstValueFrom(
      this.topicService.getReplies(
        id,
        this.listingCommentOptions.limit,
        this.listingCommentOptions.page,
        this.listingCommentOptions.sort
      )
    );
    this.totalCommentItems = this.comments.total;
  }

  public async onKeywordRemoved() {
    await this.loadKeywords(this.node.id as string);
  }

  public isLastVersionOfContent(): boolean {
    let result = false;

    if (this.versionLabel === undefined) {
      result = true;
    } else if (this.versionLabel !== undefined && this.versions !== undefined) {
      if (this.versionLabel === this.versions[0].versionLabel) {
        result = true;
      }
    }

    return result;
  }

  public async onDeletedElement(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      (result.type === ActionType.DELETE_SPACE ||
        result.type === ActionType.DELETE_CONTENT)
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(result.type)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
      if (result.node) {
        this.clipboardService.removeItem(result.node);
      }
      if (this.versionLabel === undefined) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../', this.node.parentId], {
          relativeTo: this.route,
        });
      } else {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../../', this.node.parentId], {
          relativeTo: this.route,
        });
      }
    } else if (
      result.result === ActionResult.FAILED &&
      (result.type === ActionType.DELETE_SPACE ||
        result.type === ActionType.DELETE_CONTENT)
    ) {
      const text = this.translateService.translate(
        getErrorTranslation(result.type)
      );
      if (text) {
        this.uiMessageService.addErrorMessage(text, false);
      }
    }
  }

  public isFile(): boolean {
    let result = false;
    if (this.node !== undefined && this.node.type !== undefined) {
      result = this.node.type.indexOf('folder') === -1;
    }
    return result;
  }

  public async onCloseUpdateWizard(result: ActionEmitterResult) {
    this.showUpdateWizard = false;
    this.ngInit();
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.UPDATE_FILE_CONTENT
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.UPDATE_FILE_CONTENT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  public async onKeywordAdded(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      await this.loadKeywords(this.node.id as string);
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.ADD_KEYWORD)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  public get currentLang(): string {
    return this.translateService.getActiveLang();
  }
  public isLibManageOwnOrHigher(): boolean {
    return this.permEvalService.isLibManageOwnOrHigher(this.node);
  }

  public getPageTitle() {
    if (this.isLink()) {
      return 'label.library.details.of.url';
    } else if (this.isFile()) {
      return 'label.library.details.of.document';
    } else {
      return 'label.library.details.of.folder';
    }
  }

  public isContent(): boolean {
    if (this.node !== undefined && this.node.type) {
      return !this.isLibraryLink(this.node);
    } else {
      return false;
    }
  }

  public isFolder(node: ModelNode): boolean {
    if (node !== undefined && node.type) {
      return node.type.indexOf('folder') !== -1 && !this.isLibraryLink(node);
    } else {
      return false;
    }
  }

  public isLink(): boolean {
    if (this.node !== undefined && this.node.type && this.node.properties) {
      return this.node.properties.isUrl === 'true';
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

  private isNodeTypeOf(
    node: ModelNode,
    item: 'filelink' | 'folderlink'
  ): boolean {
    if (node !== undefined && node.type) {
      return node.type.indexOf(item) !== -1;
    } else {
      return false;
    }
  }

  isLibraryLink(node: ModelNode): boolean {
    return this.isNodeTypeOf(node, 'filelink');
  }

  isSharedSpaceLink(node: ModelNode): boolean {
    return this.isNodeTypeOf(node, 'folderlink');
  }

  public async refreshComments(_result: ActionEmitterResult) {
    await this.loadTopic(this.currentTopic.id as string);
  }

  public async refreshCreateTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.CREATE_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
    }
    this.showCreateTopic = false;
  }

  public async refreshDeleteTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
      this.showDeleteTopic = false;
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.loadTopic(this.currentTopic.id as string);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic = false;
    }
  }

  public prepareQuote(post: Quote) {
    this.futureQuote = post;
  }

  public prepareEdit(post: ModelNode) {
    this.editPost = post;
  }

  public prepareDeletion(topic: ModelNode) {
    this.currentDeletedTopic = topic;
    this.showDeleteTopic = true;
  }

  public async previousCommentPage() {
    if (this.canPreviousCommentPage()) {
      if (this.listingCommentOptions.page > 1) {
        this.listingCommentOptions.page = this.listingCommentOptions.page - 1;
        await this.loadTopic(this.currentTopic.id as string);
      }
    }
  }

  public async nextCommentPage() {
    if (this.canNextCommentPage()) {
      if (
        this.listingCommentOptions.page <
        Math.floor(
          this.totalCommentItems / this.listingCommentOptions.limit + 1
        )
      ) {
        this.listingCommentOptions.page = this.listingCommentOptions.page + 1;
        await this.loadTopic(this.currentTopic.id as string);
      }
    }
  }

  public async changeCommentPage(p: number) {
    this.listingCommentOptions.page = p;
    await this.loadTopic(this.currentTopic.id as string);
  }

  public getCommentPages(): number[] {
    const result: number[] = [];

    for (
      let i = 1;
      i < this.totalCommentItems / this.listingCommentOptions.limit + 1;
      i += 1
    ) {
      result.push(i);
    }

    return result;
  }

  public hasComments(): boolean {
    let result = false;

    if (this.comments && this.comments.total) {
      if (this.comments.total > 0) {
        result = true;
      }
    }
    return result;
  }

  public hasTopics(): boolean {
    if (this.topics) {
      return this.topics.length > 0;
    }
    return false;
  }

  public isLibAdmin(): boolean {
    return this.permEvalService.isLibAdmin(this.node);
  }

  public isAccess(): boolean {
    return this.permEvalService.isLibAccess(this.node);
  }

  public isLibFullEdit(): boolean {
    return this.permEvalService.isLibFullEdit(this.node);
  }

  public async takeOwnership() {
    try {
      await firstValueFrom(this.nodesService.putOwnership(this.nodeId));
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.TAKE_OWNERSHIP)
      );
      this.uiMessageService.addSuccessMessage(text, true);
    } catch (error) {
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.TAKE_OWNERSHIP)
      );
      this.uiMessageService.addErrorMessage(text, true);
    }
  }

  public isLocked(): boolean {
    return this.node?.properties?.locked === 'true';
  }

  public isEditOnline(): boolean {
    if (this.loginService.isGuest()) {
      return false;
    }
    return (
      this.node?.properties?.editInline === 'true' &&
      this.loginService.getUser().userId ===
        this.node?.properties?.workingCopyOwner
    );
  }

  public isWorkingCopy(): boolean {
    return this.node?.properties?.workingCopy === 'true';
  }

  public currentUserHasAccess(): boolean {
    return (
      this.node !== undefined &&
      this.node.properties !== undefined &&
      this.node.properties.currentUserHasAccess === 'true'
    );
  }

  public async checkout() {
    this.processing = true;

    if (this.node.id !== undefined) {
      await firstValueFrom(this.contentService.postCheckout(this.node.id));
      this.route.params.subscribe(
        async (params) => await this.loadNode(params)
      );
    }

    this.processing = false;
  }

  public async cancelCheckout() {
    this.processing = true;

    if (this.node.id !== undefined && this.node.properties !== undefined) {
      await firstValueFrom(
        this.contentService.deleteCheckout(this.node.properties.originalNodeId)
      );
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(
        [`../../${this.node.properties.originalNodeId}/details`],
        { relativeTo: this.route }
      );
    }

    this.processing = false;
  }

  public async checkinDone() {
    if (this.node.id !== undefined && this.node.properties !== undefined) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(
        [`../../${this.node.properties.originalNodeId}/details`],
        { relativeTo: this.route }
      );
    }
  }

  public addToClipboard() {
    this.clipboardService.addItem(this.node, true, this.group.id);
    this.itemToClipboard = this.node.name;
  }

  public async emailMe() {
    let text: string;
    try {
      if (this.node.id !== undefined) {
        const status = await firstValueFrom(
          this.contentService.postContentEmail(this.node.id)
        );
        if (!status.result) {
          text = this.translateService.translate(
            getErrorTranslation(ActionType.EMAIL_CONTENT),
            {
              content: this.node.name,
              name: `${this.user.firstname} ${this.user.lastname}`,
              email: this.user.email,
            }
          );
          this.uiMessageService.addErrorMessage(text);
          return;
        }
        text = this.translateService.translate(
          getSuccessTranslation(ActionType.EMAIL_CONTENT),
          {
            content: this.node.name,
            name: `${this.user.firstname} ${this.user.lastname}`,
            email: this.user.email,
          }
        );
        this.uiMessageService.addSuccessMessage(text);
      } else {
        text = this.translateService.translate(
          getErrorTranslation(ActionType.EMAIL_CONTENT),
          {
            content: this.node.name,
            name: `${this.user.firstname} ${this.user.lastname}`,
            email: this.user.email,
          }
        );
        this.uiMessageService.addErrorMessage(text);
      }
    } catch (error) {
      text = this.translateService.translate(
        getErrorTranslation(ActionType.EMAIL_CONTENT),
        {
          content: this.node.name,
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
  }

  public isPublic() {
    return (
      this.node.properties !== undefined &&
      (this.node.properties.security_ranking === undefined ||
        this.node.properties.security_ranking === 'PUBLIC')
    );
  }

  public isMultiLingual(): boolean {
    if (this.node && this.node.properties) {
      return (
        this.node.properties.multilingual === 'true' &&
        this.translationSet !== undefined
      );
    }

    return false;
  }

  public async onCloseMakeMultiModal(result: ActionEmitterResult) {
    this.showEnableMultilingualModal = false;
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.ENABLE_MULTILINGUAL
    ) {
      this.ngInit();
    }
  }

  public canEditInOffice() {
    if (
      this.isOfficeIntegrationEnabled() &&
      !this.isWorkingCopy() &&
      !this.isLocked()
    ) {
      return (
        this.isFile() &&
        this.node.name !== undefined &&
        (this.node.name.endsWith('.docx') ||
          this.node.name.endsWith('.xlsx') ||
          this.node.name.endsWith('.pptx'))
      );
    } else {
      return false;
    }
  }

  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }
  public openDocInOffice() {
    const id = this.node?.properties?.workingCopyId ?? this.node.id;
    if (!this.isWorkingCopy()) {
      setTimeout(() => location.reload(), 10000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=edit`;
      window.open(url, '_blank');
    } else {
      setTimeout(
        () =>
          this.router.navigate([
            '/group',
            this.group.id,
            'library',
            this.node.properties?.originalNodeId ?? this.node.id,
            'details',
          ]),
        2000
      );
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=update`;
      window.open(url, '_blank');
    }
  }

  get securityRanking(): string {
    if (this.node.properties) {
      return this.node.properties.security_ranking;
    } else {
      return '';
    }
  }

  get size(): string | null {
    if (this.node.properties) {
      return this.node.properties.size;
    } else {
      return null;
    }
  }

  get locale(): string {
    if (this.node.properties) {
      return this.node.properties.locale;
    } else {
      return '';
    }
  }
  get mimetypeName(): string {
    if (this.node.properties) {
      return this.node.properties.mimetypeName;
    } else {
      return '';
    }
  }
  get created(): string | null {
    if (this.node.properties) {
      return this.node.properties.created;
    } else {
      return null;
    }
  }

  get modified(): string | null {
    if (this.node.properties) {
      return this.node.properties.modified;
    } else {
      return null;
    }
  }
  get modifier(): string {
    if (this.node.properties) {
      return this.node.properties.modifier;
    } else {
      return '';
    }
  }
  get creator(): string {
    if (this.node.properties) {
      return this.node.properties.creator;
    } else {
      return '';
    }
  }
  get author(): string {
    if (this.node.properties) {
      return this.node.properties.author;
    } else {
      return '';
    }
  }

  get url(): string {
    if (this.node.properties) {
      return this.node.properties.url;
    } else {
      return '';
    }
  }

  get encoding(): string {
    if (this.node.properties) {
      return this.node.properties.encoding;
    } else {
      return '';
    }
  }

  get status(): string {
    if (this.node.properties) {
      return this.node.properties.status;
    } else {
      return '';
    }
  }

  get reference(): string {
    if (this.node.properties) {
      return this.node.properties.reference;
    } else {
      return '';
    }
  }
  get nodeVersionLabel(): string {
    if (this.node.properties) {
      return this.node.properties.versionLabel;
    } else {
      return '';
    }
  }
  get issueDate(): string | null {
    if (this.node.properties) {
      return this.node.properties.issue_date;
    } else {
      return null;
    }
  }
  get expirationDate(): string | null {
    if (this.node.properties) {
      return this.node.properties.expiration_date;
    } else {
      return null;
    }
  }

  aresBridgeVersionSelected(version: string) {
    this.formAresInfo = new FormGroup(
      {
        saveNumber: new FormControl(),
        registrationNumber: new FormControl(),
        documentId: new FormControl(),
      },
      {
        updateOn: 'change',
      }
    );

    // do not mutate this.nodeLog
    const nodeLogReversed = this.nodeLog.slice().reverse();
    nodeLogReversed.forEach((response) => {
      if (
        response.versionLabel === version &&
        response.documentId !== 'null' &&
        response.documentId !== ''
      ) {
        this.hasAresLogVersion = true;
        this.formAresInfo.controls.saveNumber.setValue(response.saveNumber);
        this.formAresInfo.controls.registrationNumber.setValue(
          response.registrationNumber
        );
        this.formAresInfo.controls.documentId.setValue(response.documentId);
      }
    });
  }

  get aresDocumentLink(): string | null {
    const documentId = this.formAresInfo.value.documentId;
    return `${environment.aresBridgeServer}/Ares/document/show.do?documentId=${documentId}`;
  }

  private getRepositoriesInfoProperty(
    propertyName:
      | 'property_registration_number='
      | 'property_save_number='
      | 'property_document_id='
  ): string | null {
    let result: string | null = null;
    if (this.node.properties && this.node.properties.repositoriesInfo) {
      const repositoriesInfo: string = this.node.properties.repositoriesInfo;
      let startIndex = repositoriesInfo.indexOf(propertyName);
      if (startIndex > -1) {
        startIndex += propertyName.length;
        const endIndex = repositoriesInfo.indexOf(',', startIndex);
        if (endIndex > -1) {
          result = repositoriesInfo.substring(startIndex, endIndex);
        }
      }
    }
    return result;
  }

  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  public isSelectionField(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  public endsWithDetails(): boolean {
    return window.location.href.endsWith('details');
  }
  get workingCopyId(): string {
    if (this.node.properties) {
      return this.node.properties.workingCopyId;
    } else {
      return '';
    }
  }
  get isNotItemToClipboardUndefined(): boolean {
    return this.itemToClipboard !== undefined;
  }

  public isVersion() {
    return (
      this.node.properties !== undefined &&
      this.node.properties.isVersion === 'true'
    );
  }

  public getVersionModifier(version: Version): string {
    if (version.node && version.node.properties) {
      return version.node.properties.modifier;
    } else {
      return '';
    }
  }

  public getVersionModifed(version: Version): string | null {
    if (version.node && version.node.properties) {
      return version.node.properties.modified;
    } else {
      return null;
    }
  }

  get isNotFolder(): boolean {
    if (this.node.type) {
      return this.node.type.indexOf('folder') === -1;
    } else {
      return true;
    }
  }
  get pivotId(): string | null {
    if (this.translationSet.pivot && this.translationSet.pivot.id) {
      return this.translationSet.pivot.id;
    } else {
      return null;
    }
  }

  get title(): string {
    if (this.node && this.node.title) {
      const title = this.i18nPipe.transform(this.node.title);
      if (title !== '') {
        return title;
      } else {
        return this.i18nSelectPipe.transform('en', this.node.title);
      }
    }
    return '';
  }

  get description(): string {
    if (this.node && this.node.description) {
      const description = this.i18nPipe.transform(this.node.description);
      if (description !== '') {
        return description;
      } else {
        return this.i18nSelectPipe.transform('en', this.node.description);
      }
    }

    return '';
  }

  public prepareEdition(topic: ModelNode) {
    this.currentEditedTopic = topic;
    this.showEditTopic = true;
  }

  public async refreshEditTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
      this.showEditTopic = false;
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      await this.loadTopic(this.currentTopic.id as string);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      this.showEditTopic = false;
    }
  }

  public canPreviousCommentPage() {
    return this.listingCommentOptions.page > 1;
  }

  public canNextCommentPage() {
    return (
      this.listingCommentOptions.page <
      Math.ceil(this.totalCommentItems / this.listingCommentOptions.limit)
    );
  }

  public humanReadableSelectionDynProp(value: string): string {
    // only if [..., ...] - in case of an array
    if (value && value.indexOf('[') !== -1 && value.indexOf(']') !== -1) {
      let result: string = value.substring(1);
      result = result.substring(0, result.length - 1);
      const parts = result.split(',');
      result = '';
      let i = 0;
      for (const part of parts) {
        const itemString = part.substring(1, part.length - 1);
        result = `${itemString}${i === 0 ? '' : ', '}${result}`;
        i = 1;
      }
      return result;
    } else if (value) {
      return value;
    } else {
      return '';
    }
  }

  public urlLength(): number {
    return (this.router.url.match(/\//g) || []).length;
  }

  public goBack() {
    this.location.back();
  }

  public async backToContainer() {
    if (!this.isInterestGroup(this.group)) {
      return;
    }
    if (
      this.node &&
      this.node.properties &&
      this.node.properties.originalContainerId
    ) {
      if (this.urlLength() === 5 && !this.restrictedMode) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises

        this.router.navigate(
          [
            '../../',
            this.isFolder(this.node)
              ? this.node.id
              : this.node.properties.originalContainerId,
          ],
          {
            relativeTo: this.route,
            queryParams: {
              p: sessionStorage.getItem('libraryPage'),
              n: sessionStorage.getItem('libraryLimit'),
              s: sessionStorage.getItem('librarySort'),
            },
          }
        );
      } else if (this.urlLength() === 5 && this.restrictedMode) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../', this.group.libraryId], {
          relativeTo: this.route,
          queryParams: {
            p: sessionStorage.getItem('libraryPage'),
            n: sessionStorage.getItem('libraryLimit'),
            s: sessionStorage.getItem('librarySort'),
          },
        });
      } else if (this.urlLength() === 6 && !this.restrictedMode) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(
          ['../../../', this.node.properties.originalContainerId],
          {
            relativeTo: this.route,
            queryParams: {
              p: sessionStorage.getItem('libraryPage'),
              n: sessionStorage.getItem('libraryLimit'),
              s: sessionStorage.getItem('librarySort'),
            },
          }
        );
      } else if (this.urlLength() === 6 && this.restrictedMode) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['../../../', this.group.libraryId], {
          relativeTo: this.route,
          queryParams: {
            p: sessionStorage.getItem('libraryPage'),
            n: sessionStorage.getItem('libraryLimit'),
            s: sessionStorage.getItem('librarySort'),
          },
        });
      }
    }
  }

  public isPreviewable(): boolean {
    return isContentPreviewable(this.node);
  }

  public previewContent() {
    this.dummyPreviewUrlChange = !this.dummyPreviewUrlChange;
    // eslint-disable-next-line max-len
    this.contentURL = `${
      environment.serverURL
    }pdfRendition?documentId=workspace://SpacesStore/${
      this.node.id
    }&response=content&ticket=${this.loginService.getTicket()}&dummy=${
      this.dummyPreviewUrlChange
    }`;
    this.previewDocumentId = this.node.id as string;
    this.showPreview = true;
  }

  public closePreview() {
    this.showPreview = false;
  }

  public async changeNotificationSubscription(value: string) {
    if (value && value !== '' && this.node.id) {
      try {
        await firstValueFrom(
          this.notificationService.putNotificationAuthority(
            this.node.id,
            this.loginService.getCurrentUsername(),
            value
          )
        );
        this.node = await firstValueFrom(
          this.nodesService.getNode(this.node.id)
        );
      } catch (error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  isSubscribedToNotifications(): boolean {
    return this.node.notifications === 'ALLOWED';
  }

  public isGuest() {
    return this.loginService.isGuest();
  }

  public isCurrentOwner() {
    if (this.node && this.node.properties) {
      return this.node.properties.owner === this.user.userId;
    } else {
      return false;
    }
  }

  public isFolderLink(node: ModelNode) {
    if (node !== undefined && node.type) {
      return node.type.indexOf('folderlink') !== -1;
    } else {
      return false;
    }
  }

  public canSendToAresBridge() {
    if (this.user?.properties?.domain === 'external') {
      return false;
    }
    if (!this.isFile()) {
      return false;
    }
    if (!this.isAresBridgeEnabled) {
      return false;
    }
    if (!environment.aresBridgeEnabled) {
      return false;
    }

    if (
      this.nodeLog.some(
        (log) =>
          log.versionLabel === this.node.properties?.versionLabel &&
          log.saveNumber
      )
    ) {
      return false;
    }

    if (environment.circabcRelease !== 'oss') {
      return this.isLibManageOwnOrHigher();
    } else {
      return false;
    }
  }

  public async sendToAresBridge() {
    if (this.processing) {
      return;
    }
    this.processing = true;
    const lastVersionRegistered = this.nodeLog.filter(
      (register) => register.registrationNumber
    );
    if (lastVersionRegistered.length > 0) {
      await this.showConfirmationDialog(lastVersionRegistered);
      this.processing = false;
      return;
    }
    try {
      await this.aresBridgeHelperService.sendToAresBridge(this.node);
    } finally {
      this.processing = false;
    }
  }
  private async showConfirmationDialog(
    lastVersionRegistred: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'label.ares.bridge.resend.title',
        message: 'label.ares.bridge.resend.message',
        layoutStyle: 'sendOneToAres',
        nodeLog: lastVersionRegistred,
      },
    });

    // listen to response
    const dialogResult = await firstValueFrom(dialogRef.afterClosed());
    if (dialogResult) {
      await this.aresBridgeHelperService.sendToAresBridge(this.node);
    }
  }

  public async getFolderSize(folderId: string | undefined) {
    if (folderId === undefined) {
      this.folderSize = 0;
    } else {
      const result = await firstValueFrom(
        this.spaceService.getFolderSize(folderId)
      );
      if (result.code !== undefined) {
        this.folderSize = result.code;
      }
    }
  }

  isEmptyRegistrationNumber() {
    return (
      this.formAresInfo.value.registrationNumber === 'null' ||
      this.formAresInfo.value.registrationNumber === ''
    );
  }
}
