import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Data } from '@angular/router';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslocoService } from '@ngneat/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Attachment,
  AttachmentsRemainingSize,
  Comment,
  InterestGroup,
  Node as ModelNode,
  NodesService,
  NotificationService,
  PagedNodes,
  PostService,
  UserService,
  TopicService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { Quote } from 'app/core/ui-model/index';
import { getSuccessTranslation, getUserFullName } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { assertDefined } from 'app/core/asserts';
import { firstValueFrom } from 'rxjs';

interface FileWithId {
  id: number;
  file: File;
}
@Component({
  selector: 'cbc-topic',
  templateUrl: './topic.component.html',
  styleUrls: ['./topic.component.scss'],
  preserveWhitespaces: true,
})
export class TopicComponent implements OnInit {
  public topicNode: ModelNode | undefined;
  public posts!: PagedNodes;
  public futureQuote!: Quote;
  public editPost: ModelNode | undefined;
  public showModal = false;
  public loading = false;
  public group!: InterestGroup;

  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public pages: number[] = [];
  public totalItems = 10;

  public postOpen = false;
  public addPostForm!: FormGroup;
  public posting = false;
  public isValid = false;
  public originalTextValue: string | undefined;

  public filesToUpload: FileWithId[] = [];
  public idCount = 0;
  public attachmentRemainingSize: AttachmentsRemainingSize = {
    remainingSize: 0,
  };

  public attachmentsAndLinksToEdit: Attachment[] = [];
  private attachmentsAndLinksToDelete: Attachment[] = [];

  public linkPickerOpen = false;
  public loadingPicker = false;
  public pickedNodes: string[] = [];

  public constructor(
    private fb: FormBuilder,
    private nodesService: NodesService,
    private route: ActivatedRoute,
    private topicService: TopicService,
    private postService: PostService,
    private loginService: LoginService,
    private notificationService: NotificationService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private permEvalService: PermissionEvaluatorService,
    private userService: UserService
  ) {}

  public ngOnInit() {
    this.addPostForm = this.fb.group({
      text: ['', Validators.required],
    });

    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
    });

    this.route.params.subscribe(async (params) => await this.loadTopic(params));
  }

  public async loadTopic(params: { [key: string]: string }) {
    this.loading = true;
    if (params.nodeId !== undefined) {
      this.topicNode = await firstValueFrom(
        this.nodesService.getNode(params.nodeId)
      );
    }

    assertDefined(this.topicNode);
    try {
      if (this.topicNode.id) {
        this.posts = await firstValueFrom(
          this.topicService.getReplies(
            this.topicNode.id,
            this.listingOptions.limit,
            this.listingOptions.page,
            this.listingOptions.sort
          )
        );
        this.totalItems =
          this.posts.total > 0 ? this.posts.total : this.listingOptions.limit;
      }
    } catch (error) {
      this.posts = { data: [], total: 0 };
    }

    this.loading = false;
  }

  public async loadComments(idTopic: string) {
    await this.loadTopic({ id: idTopic });
  }

  public async prepareQuote(post: Quote) {
    this.futureQuote = post;
    this.attachmentRemainingSize = await firstValueFrom(
      this.postService.getAttachmentsRemainingSize('')
    );
    const quoting = this.translateService.translate('label.quoting');
    const authorFullName = await getUserFullName(
      post.author as string,
      this.userService
    );
    const tmpText = `<br/><br/><p>${quoting}: ${authorFullName}
    </p><blockquote>${post.text as string}</blockquote>`;
    if (this.addPostForm) {
      this.addPostForm.patchValue({ text: tmpText });
    }
    this.attachmentsAndLinksToDelete = [];
    this.pickedNodes = [];
    this.linkPickerOpen = false;
    this.editPost = undefined;
    this.openPost();
  }

  public async prepareEdit(post: ModelNode) {
    this.editPost = post;
    this.attachmentRemainingSize = await firstValueFrom(
      this.postService.getAttachmentsRemainingSize(this.editPost.id as string)
    );
    if (post.properties) {
      const tmpText = post.properties.message;
      if (this.addPostForm) {
        this.addPostForm.patchValue({ text: tmpText });
      }
    }
    if (this.editPost.attachments !== undefined) {
      this.attachmentsAndLinksToEdit = this.editPost.attachments.slice();
    }
    this.filesToUpload = [];
    this.attachmentsAndLinksToDelete = [];
    this.pickedNodes = [];
    this.linkPickerOpen = false;
    this.openPost();
  }

  public async refreshComments(res: ActionEmitterResult) {
    assertDefined(this.topicNode);
    this.showModal = false;
    this.editPost = undefined;
    if (this.topicNode.id === undefined) {
      return;
    }
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.CREATE_POST
    ) {
      await this.loadComments(this.topicNode.id);
    } else if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_POST
    ) {
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_POST)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      await this.loadComments(this.topicNode.id);
    } else if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.EDIT_POST
    ) {
      await this.loadComments(this.topicNode.id);
    } else if (res.result === ActionResult.SUCCEED) {
      await this.loadComments(this.topicNode.id);
    }
  }

  public async changePage(p: number) {
    assertDefined(this.topicNode);
    if (this.topicNode.id) {
      this.listingOptions.page = p;
      await this.loadTopic({ id: this.topicNode.id });
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  isSubscribedToNotifications(): boolean {
    if (this.topicNode && this.topicNode.notifications !== undefined) {
      return this.topicNode.notifications === 'ALLOWED';
    } else {
      return false;
    }
  }

  public async changeNotificationSubscription(value: string) {
    assertDefined(this.topicNode);
    if (value && value !== '' && this.topicNode.id) {
      await firstValueFrom(
        this.notificationService.putNotificationAuthority(
          this.topicNode.id,
          this.loginService.getCurrentUsername(),
          value
        )
      );
      this.topicNode = await firstValueFrom(
        this.nodesService.getNode(this.topicNode.id)
      );
    }
  }

  public isLibAdmin(): boolean {
    assertDefined(this.topicNode);
    return this.permEvalService.isLibAdmin(this.topicNode);
  }

  public isNewsgroupAdmin(): boolean {
    assertDefined(this.topicNode);
    if (this.group.permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return (
      this.permEvalService.isNewsgroupAdmin(this.topicNode) ||
      this.permEvalService.isOwner(
        this.topicNode,
        this.loginService.getCurrentUsername()
      )
    );
  }

  public nameExists(item: ModelNode): boolean {
    if (item === undefined) {
      return false;
    }
    return item.name !== undefined && item.name !== '';
  }

  // posting ...

  public async openNewPost() {
    if (this.postOpen) {
      return;
    }
    this.attachmentRemainingSize = await firstValueFrom(
      this.postService.getAttachmentsRemainingSize('')
    );
    this.openPost();
  }

  private openPost() {
    this.postOpen = true;
  }

  public resetPost() {
    this.addPostForm.patchValue({ text: ' ' });
    this.editPost = undefined;
    this.filesToUpload = [];
    this.attachmentsAndLinksToEdit = [];
    this.attachmentsAndLinksToDelete = [];
    this.pickedNodes = [];
    this.linkPickerOpen = false;
    this.postOpen = false;
  }

  public async addPost() {
    assertDefined(this.topicNode);
    if (this.isAllValid()) {
      this.posting = true;
      if (this.addPostForm.controls.text.value === '') {
        this.addPostForm.patchValue({ text: ' ' });
      }

      const body: Comment = {
        ...this.addPostForm.value,
      };

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_POST;

      try {
        if (this.topicNode.id) {
          await firstValueFrom(
            this.topicService.postReply(
              this.topicNode.id,
              body,
              this.filesToUpload.map((fileWithId) => fileWithId.file),
              this.pickedNodes
            )
          );
          result.result = ActionResult.SUCCEED;
          this.addPostForm.reset();
        }
      } catch (exception) {
        await this.handleError(exception);
        result.result = ActionResult.FAILED;
      }
      this.editPost = undefined;
      this.posting = false;
      this.isValid = false;
      this.originalTextValue = undefined;
      this.resetPost();
      await this.refreshComments(result);
    }
  }

  public async updatePost() {
    if (this.isAllValid()) {
      this.posting = true;

      if (this.addPostForm.controls.text.value === '') {
        this.addPostForm.patchValue({ text: ' ' });
      }

      const result: ActionEmitterResult = {};
      result.type = ActionType.EDIT_POST;

      if (this.editPost && this.editPost.properties) {
        this.editPost.properties.message = this.addPostForm.value.text;
        try {
          if (this.editPost.id) {
            await firstValueFrom(
              this.postService.putPost(
                this.editPost.id,
                this.editPost,
                this.filesToUpload.map((fileWithId) => fileWithId.file),
                this.pickedNodes,
                this.attachmentsAndLinksToDelete.map(
                  (attachment) => attachment.id as string
                )
              )
            );
            result.result = ActionResult.SUCCEED;
            this.attachmentsAndLinksToDelete = [];
            this.addPostForm.reset();
          }
        } catch (exception) {
          await this.handleError(exception);
          result.result = ActionResult.FAILED;
        }
        this.editPost = undefined;
        this.posting = false;
        this.isValid = false;
        this.originalTextValue = undefined;
        this.resetPost();
        await this.refreshComments(result);
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public checkValidity(event: any) {
    let intermediaryResult = false;

    intermediaryResult =
      event.delta.ops.length > 0 && this.originalTextValue !== event.htmlValue;

    this.isValid =
      (this.addPostForm.value.text === undefined &&
        this.addPostForm.value.text === null &&
        this.addPostForm.value.text === '') ||
      intermediaryResult;
  }

  // file attachment

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
    this.addFiles(filesList);
  }

  private addFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.attachmentRemainingSize.remainingSize -= fileItem.size;
        this.filesToUpload.push({ file: fileItem, id: this.idCount });
        this.idCount += 1;
      }
    }
  }

  public deleteSelectedFile(file: FileWithId) {
    let index = 0;
    for (const fileToUpload of this.filesToUpload) {
      if (fileToUpload.id === file.id) {
        this.filesToUpload.splice(index, 1);
        this.attachmentRemainingSize.remainingSize += file.file.size;
      }
      index += 1;
    }
  }

  /* eslint-disable @typescript-eslint/no-unused-vars */
  private async deleteAttachments() {
    if (this.editPost && this.editPost.id) {
      for (const attachment of this.attachmentsAndLinksToDelete) {
        await firstValueFrom(
          this.postService.deleteAttachment(
            this.editPost.id,
            attachment.id as string
          )
        );
      }
    }
    this.attachmentsAndLinksToDelete = [];
  }

  public displayName(text: string | undefined): string {
    if (text === undefined) {
      return '';
    }
    return text.length > 7 ? `${text.substring(0, 7)}...` : text;
  }

  public removeAttachment(attachment: Attachment) {
    const index: number = this.attachmentsAndLinksToEdit.indexOf(attachment, 0);
    this.attachmentsAndLinksToEdit.splice(index, 1);
    this.attachmentsAndLinksToDelete.push(attachment);
    if (!attachment.isLink) {
      this.attachmentRemainingSize.remainingSize += attachment.size as number;
    }
  }

  // links

  public openLinkPicker() {
    this.linkPickerOpen = true;
    this.loadingPicker = true;
  }

  public linkPickerOpened() {
    this.loadingPicker = false;
  }

  public closeLinkPicker() {
    this.linkPickerOpen = false;
    this.pickedNodes = [];
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async handleError(error: any) {
    let res = '';
    if (
      error.error.message.includes(
        'Size of attachments exceeds the allowed limit for this post'
      )
    ) {
      res = this.translateService.translate('error.attachment.exceeds');
    } else if (error.error.message.includes('Invalid file type: ')) {
      res = this.translateService.translate('error.attachment.invalid', {
        name: error.error.message.substring(
          'Invalid file type: '.length + 1,
          error.error.message.length
        ),
      });
    } else if (error.error.message.includes('Link already exists')) {
      res = this.translateService.translate('error.link.exists');
    } else {
      res = this.translateService.translate('error.upload');
    }
    this.uiMessageService.addErrorMessage(res);
  }

  public isAllValid(): boolean {
    return (
      this.attachmentRemainingSize.remainingSize > 0 &&
      (this.isValid ||
        this.attachmentsAndLinksToDelete.length > 0 ||
        this.filesToUpload.length > 0 ||
        this.pickedNodes.length > 0)
    );
  }

  public getRemainingSizeInMB() {
    if (this.attachmentRemainingSize !== undefined) {
      return (this.attachmentRemainingSize.remainingSize / 1024 / 1024).toFixed(
        2
      );
    }
    return 0;
  }

  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
