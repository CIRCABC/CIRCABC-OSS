import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Data, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { assertDefined } from 'app/core/asserts';
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
  TopicService,
  UserService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { Quote } from 'app/core/ui-model/index';
import { getSuccessTranslation, getUserFullName } from 'app/core/util';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { PostComponent } from 'app/group/forum/post/post.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { FilePickerComponent } from 'app/shared/file-picker/file-picker.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { TaggedToPlainTextPipe } from 'app/shared/pipes/taggedtoplaintext.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Associates a browser {@link File} selected for upload with a locally
 * generated identifier so that individual pending files can be tracked and
 * removed from the upload queue before the post is submitted.
 */
interface FileWithId {
  /** Locally generated, monotonically increasing identifier for the pending file. */
  id: number;
  /** The underlying browser file to be uploaded as a post attachment. */
  file: File;
}
/**
 * Renders a single forum topic and the paginated list of replies (posts)
 * belonging to it.
 *
 * The component displays the topic node together with its posts and provides
 * the full authoring workflow for replies: creating a new post, quoting an
 * existing post, editing an existing post, and deleting posts. It also manages
 * file/link attachments (including the remaining attachment size budget),
 * pagination of replies, and per-user notification subscription for the topic.
 *
 * Key collaborators include {@link NodesService}, {@link TopicService} and
 * {@link PostService} for data access, {@link NotificationService} for
 * subscription management, {@link PermissionEvaluatorService} and
 * {@link LoginService} for permission checks, and {@link UiMessageService} /
 * {@link TranslocoService} for user feedback and localisation.
 */
@Component({
  selector: 'cbc-topic',
  templateUrl: './topic.component.html',
  styleUrl: './topic.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RichTextEditorComponent,
    HorizontalLoaderComponent,
    RouterLink,
    ReponsiveSubMenuComponent,
    BreadcrumbComponent,
    PagerComponent,
    PostComponent,
    IfRoleGEDirective,
    ReactiveFormsModule,
    NotificationMessageComponent,
    SpinnerComponent,
    InlineDeleteComponent,
    FilePickerComponent,
    MatSlideToggleModule,
    I18nPipe,
    TaggedToPlainTextPipe,
    TranslocoModule,
  ],
})
export class TopicComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly nodesService = inject(NodesService);
  private readonly route = inject(ActivatedRoute);
  private readonly topicService = inject(TopicService);
  private readonly loadingService = inject(LoadingService);
  private readonly postService = inject(PostService);
  private readonly loginService = inject(LoginService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly userService = inject(UserService);

  /** The forum topic node currently displayed, if loaded. */
  public readonly topicNode = signal<ModelNode | undefined>(undefined);
  /** The paginated collection of reply posts for the current topic. */
  public readonly posts = signal<PagedNodes | undefined>(undefined);
  /** The post currently being quoted, used to pre-fill the reply editor. */
  public futureQuote!: Quote;
  /** The post currently being edited, or `undefined` when creating a new post. */
  public readonly editPost = signal<ModelNode | undefined>(undefined);
  /** Whether a modal dialog is currently displayed. */
  public showModal = false;
  /** Whether an asynchronous topic/reply load is in progress. */
  public readonly loading = signal(false);
  /** The interest group that owns the current topic, resolved from route data. */
  public readonly group = signal<InterestGroup>(
    undefined as unknown as InterestGroup
  );

  /** Pagination and sorting options used when fetching the topic replies. */
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  /** Page numbers available to the pager component. */
  public pages: number[] = [];
  /** Total number of reply items, used to compute pagination. */
  public readonly totalItems = signal(10);

  /** Whether the reply authoring editor is currently open. */
  public readonly postOpen = signal(false);
  /** Reactive form backing the reply editor; holds the post text. */
  public addPostForm!: FormGroup;
  /** Whether a post create/update request is currently in flight. */
  public readonly posting = signal(false);
  /** Whether the current editor content is considered valid for submission. */
  public isValid = false;
  /** Snapshot of the editor's original text, used to detect real edits. */
  public originalTextValue: string | undefined;

  /** Pending files selected for upload, paired with their local identifiers. */
  public readonly filesToUpload = signal<FileWithId[]>([]);
  /** Counter used to assign unique identifiers to pending upload files. */
  public idCount = 0;
  /** Remaining attachment size budget (in bytes) allowed for the post. */
  public readonly attachmentRemainingSize = signal<AttachmentsRemainingSize>({
    remainingSize: 0,
  });

  /** Existing attachments and links shown while editing a post. */
  public readonly attachmentsAndLinksToEdit = signal<Attachment[]>([]);
  /** Existing attachments and links marked for deletion on save. */
  private attachmentsAndLinksToDelete: Attachment[] = [];

  /** Whether the link picker is currently open. */
  public readonly linkPickerOpen = signal(false);
  /** Whether the link picker is still loading. */
  public loadingPicker = false;
  /** Identifiers of nodes picked as links to attach to the post. */
  public pickedNodes: string[] = [];
  /** Form control toggling whether group members are notified about the post. */
  public notify = new FormControl(true, { nonNullable: true });

  /**
   * Angular lifecycle hook. Initialises the reply form, reads the owning group
   * from the route data, and (re)loads the topic and its replies whenever the
   * route parameters change.
   */
  public ngOnInit() {
    this.addPostForm = this.fb.group({
      text: ['', Validators.required],
    });

    this.route.data.subscribe((value: Data) => {
      this.group.set(value.group);
    });

    this.route.params.subscribe(async (params) => await this.loadTopic(params));
  }

  /**
   * Loads the topic node identified by the route parameters and fetches its
   * paginated replies according to the current {@link listingOptions}.
   *
   * On failure to load the replies, {@link posts} is reset to an empty page.
   *
   * @param params Route parameters; `nodeId` selects the topic to load.
   * @returns A promise that resolves once the topic and its replies are loaded.
   */
  public async loadTopic(params: { [key: string]: string }) {
    await this.loadingService.run(this.loading, async () => {
      if (params.nodeId !== undefined) {
        this.topicNode.set(
          await this.nodesService.getNodeAsync({ id: params.nodeId })
        );
      }

      const topicNode = this.topicNode();
      assertDefined(topicNode);
      try {
        if (topicNode.id) {
          const replies = await this.topicService.getRepliesAsync({
            id: topicNode.id,
            limit: this.listingOptions.limit,
            page: this.listingOptions.page,
            order: this.listingOptions.sort,
          });
          this.posts.set(replies);
          this.totalItems.set(
            replies.total > 0 ? replies.total : this.listingOptions.limit
          );
        }
      } catch (_error) {
        this.posts.set({ data: [], total: 0 });
      }
    });
  }

  /**
   * Reloads the topic and its replies for the given topic identifier.
   *
   * @param idTopic Identifier of the topic whose comments should be reloaded.
   * @returns A promise that resolves once the reload has completed.
   */
  public async loadComments(idTopic: string) {
    await this.loadTopic({ id: idTopic });
  }

  /**
   * Prepares the reply editor to quote an existing post.
   *
   * Fetches the remaining attachment size budget, pre-fills the editor with a
   * formatted blockquote containing the quoted author's full name and text,
   * resets any pending attachment/link state, and opens the editor.
   *
   * @param post The post to be quoted, including its author and text.
   * @returns A promise that resolves once the quote has been prepared.
   */
  public async prepareQuote(post: Quote) {
    this.futureQuote = post;
    this.attachmentRemainingSize.set(
      await this.postService.getAttachmentsRemainingSizeAsync({ id: 'null' })
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
    this.linkPickerOpen.set(false);
    this.editPost.set(undefined);
    this.openPost();
  }

  /**
   * Prepares the reply editor to edit an existing post.
   *
   * Fetches the remaining attachment size budget for the post, pre-fills the
   * editor with the post's current message, copies its existing attachments
   * for editing, resets pending upload/link state, and opens the editor.
   *
   * @param post The post node to edit.
   * @returns A promise that resolves once the edit has been prepared.
   */
  public async prepareEdit(post: ModelNode) {
    this.editPost.set(post);
    this.attachmentRemainingSize.set(
      await this.postService.getAttachmentsRemainingSizeAsync({
        id: post.id as string,
      })
    );
    if (post.properties) {
      const tmpText = post.properties.message;
      if (this.addPostForm) {
        this.addPostForm.patchValue({ text: tmpText });
      }
    }
    if (post.attachments !== undefined) {
      this.attachmentsAndLinksToEdit.set(post.attachments.slice());
    }
    this.filesToUpload.set([]);
    this.attachmentsAndLinksToDelete = [];
    this.pickedNodes = [];
    this.linkPickerOpen.set(false);
    this.openPost();
  }

  /**
   * Reacts to the result of a post create/edit/delete action by closing the
   * modal, clearing the edit state, and reloading the comments. On successful
   * deletion, a localised success message is shown to the user.
   *
   * @param res The action result describing the outcome and its type.
   * @returns A promise that resolves once the comments have been refreshed.
   */
  public async refreshComments(res: ActionEmitterResult) {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    this.showModal = false;
    this.editPost.set(undefined);
    if (topicNode.id === undefined) {
      return;
    }
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.CREATE_POST
    ) {
      await this.loadComments(topicNode.id);
    } else if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_POST
    ) {
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_POST)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
      await this.loadComments(topicNode.id);
    } else if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.EDIT_POST
    ) {
      await this.loadComments(topicNode.id);
    } else if (res.result === ActionResult.SUCCEED) {
      await this.loadComments(topicNode.id);
    }
  }

  /**
   * Navigates to the given page of replies and reloads the topic.
   *
   * @param p The one-based page number to load.
   * @returns A promise that resolves once the requested page is loaded.
   */
  public async changePage(p: number) {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    if (topicNode.id) {
      this.listingOptions.page = p;
      await this.loadTopic({ id: topicNode.id });
    }
  }

  /**
   * Indicates whether the current user is an anonymous (guest) user.
   *
   * @returns `true` if the current user is a guest, otherwise `false`.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Indicates whether the current user is subscribed to notifications for the
   * current topic.
   *
   * @returns `true` if topic notifications are set to `ALLOWED`, otherwise `false`.
   */
  isSubscribedToNotifications(): boolean {
    const topicNode = this.topicNode();
    if (topicNode?.notifications !== undefined) {
      return topicNode.notifications === 'ALLOWED';
    }
    return false;
  }

  /**
   * Updates the current user's notification subscription for the topic and
   * refreshes the topic node to reflect the new subscription state.
   *
   * @param value The desired notification authority value (e.g. `ALLOWED`);
   *   ignored when empty.
   * @returns A promise that resolves once the subscription has been updated.
   */
  public async changeNotificationSubscription(value: string) {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    if (value && value !== '' && topicNode.id) {
      await this.notificationService.putNotificationAuthorityAsync({
        id: topicNode.id,
        authority: this.loginService.getCurrentUsername(),
        body: value,
      });
      this.topicNode.set(
        await this.nodesService.getNodeAsync({ id: topicNode.id })
      );
    }
  }

  /**
   * Indicates whether the current user has library administrator rights on the
   * topic node.
   *
   * @returns `true` if the user is a library admin for the topic, otherwise `false`.
   */
  public isLibAdmin(): boolean {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    return this.permEvalService.isLibAdmin(topicNode);
  }

  /**
   * Indicates whether the current user can administer the newsgroup/forum,
   * either through group-level newsgroup admin permission, node-level
   * newsgroup admin rights, or ownership of the topic node.
   *
   * @returns `true` if the user has newsgroup admin privileges, otherwise `false`.
   */
  public isNewsgroupAdmin(): boolean {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    if (this.group().permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return (
      this.permEvalService.isNewsgroupAdmin(topicNode) ||
      this.permEvalService.isOwner(
        topicNode,
        this.loginService.getCurrentUsername()
      )
    );
  }

  /**
   * Determines whether the given node has a non-empty name.
   *
   * @param item The node to inspect.
   * @returns `true` if the node exists and has a non-empty name, otherwise `false`.
   */
  public nameExists(item: ModelNode): boolean {
    if (item === undefined) {
      return false;
    }
    return item.name !== undefined && item.name !== '';
  }

  // posting ...

  /**
   * Opens the editor for a brand-new post. Does nothing if the editor is
   * already open; otherwise fetches the remaining attachment size budget before
   * opening.
   *
   * @returns A promise that resolves once the editor is ready and open.
   */
  public async openNewPost() {
    if (this.postOpen()) {
      return;
    }
    this.attachmentRemainingSize.set(
      await this.postService.getAttachmentsRemainingSizeAsync({ id: 'null' })
    );
    this.openPost();
  }

  /** Marks the reply editor as open. */
  private openPost() {
    this.postOpen.set(true);
  }

  /**
   * Resets the reply editor to its initial state, clearing the text, edit
   * target, pending files, attachments/links and link picker, and closing the
   * editor.
   */
  public resetPost() {
    this.addPostForm.patchValue({ text: ' ' });
    this.editPost.set(undefined);
    this.filesToUpload.set([]);
    this.attachmentsAndLinksToEdit.set([]);
    this.attachmentsAndLinksToDelete = [];
    this.pickedNodes = [];
    this.linkPickerOpen.set(false);
    this.postOpen.set(false);
  }

  /**
   * Submits a new reply to the current topic, including any pending file
   * uploads and picked node links, and optionally notifying group members.
   *
   * Validity is checked via {@link isAllValid} before posting. On failure the
   * error is handled through {@link handleError}; regardless of outcome the
   * editor is reset and the comments are refreshed.
   *
   * @returns A promise that resolves once the post attempt has completed.
   */
  public async addPost() {
    const topicNode = this.topicNode();
    assertDefined(topicNode);
    if (this.isAllValid()) {
      this.posting.set(true);
      if (this.addPostForm.controls.text.value === '') {
        this.addPostForm.patchValue({ text: ' ' });
      }

      const body: Comment = {
        ...this.addPostForm.value,
      };

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_POST;

      try {
        if (topicNode.id) {
          await this.topicService.postReplyAsync({
            id: topicNode.id,
            notify: this.notify.value,
            comment: body,
            filesToAdd: this.filesToUpload().map(
              (fileWithId) => fileWithId.file
            ),
            linksToAdd: this.pickedNodes,
          });
          result.result = ActionResult.SUCCEED;
          this.addPostForm.reset();
        }
      } catch (exception) {
        await this.handleError(exception);
        result.result = ActionResult.FAILED;
      }
      this.editPost.set(undefined);
      this.posting.set(false);
      this.isValid = false;
      this.originalTextValue = undefined;
      this.resetPost();
      await this.refreshComments(result);
    }
  }

  /**
   * Persists edits to the post currently being edited, uploading any new files,
   * adding picked node links, and removing attachments marked for deletion.
   *
   * Validity is checked via {@link isAllValid} before saving. On failure the
   * error is handled through {@link handleError}; regardless of outcome the
   * editor is reset and the comments are refreshed.
   *
   * @returns A promise that resolves once the update attempt has completed.
   */
  public async updatePost() {
    if (this.isAllValid()) {
      this.posting.set(true);

      if (this.addPostForm.controls.text.value === '') {
        this.addPostForm.patchValue({ text: ' ' });
      }

      const result: ActionEmitterResult = {};
      result.type = ActionType.EDIT_POST;

      const editPost = this.editPost();
      if (editPost?.properties) {
        editPost.properties.message = this.addPostForm.value.text;
        try {
          if (editPost.id) {
            await this.postService.putPostAsync({
              id: editPost.id,
              notify: this.notify.value,
              post: editPost,
              filesToAdd: this.filesToUpload().map(
                (fileWithId) => fileWithId.file
              ),
              linksToAdd: this.pickedNodes,
              attachmentsToDelete: this.attachmentsAndLinksToDelete.map(
                (attachment) => attachment.id as string
              ),
            });
            result.result = ActionResult.SUCCEED;
            this.attachmentsAndLinksToDelete = [];
            this.addPostForm.reset();
          }
        } catch (exception) {
          await this.handleError(exception);
          result.result = ActionResult.FAILED;
        }
        this.editPost.set(undefined);
        this.posting.set(false);
        this.isValid = false;
        this.originalTextValue = undefined;
        this.resetPost();
        await this.refreshComments(result);
      }
    }
  }

  /**
   * Updates {@link isValid} based on the latest rich-text editor change,
   * treating the content as valid only when it differs from the original text
   * and contains actual edit operations.
   *
   * @param event The rich-text editor change event carrying the delta ops and
   *   the current HTML value.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public checkValidity(event: any) {
    const intermediaryResult =
      event.delta.ops.length > 0 && this.originalTextValue !== event.htmlValue;

    this.isValid =
      (this.addPostForm.value.text === undefined &&
        this.addPostForm.value.text === null &&
        this.addPostForm.value.text === '') ||
      intermediaryResult;
  }

  // file attachment

  /**
   * Handles the file input change event by queuing the selected files for
   * upload.
   *
   * @param event The DOM change event from the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.addFiles(filesList);
  }

  /**
   * Adds the given files to the pending upload queue, assigning each a unique
   * identifier and decrementing the remaining attachment size budget
   * accordingly.
   *
   * @param filesList The list of files selected by the user.
   */
  private addFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.attachmentRemainingSize.update((size) => ({
          ...size,
          remainingSize: size.remainingSize - fileItem.size,
        }));
        this.filesToUpload.update((files) => [
          ...files,
          { file: fileItem, id: this.idCount },
        ]);
        this.idCount += 1;
      }
    }
  }

  /**
   * Removes a previously queued file from the upload list and restores its size
   * to the remaining attachment size budget.
   *
   * @param file The pending file (with its identifier) to remove.
   */
  public deleteSelectedFile(file: FileWithId) {
    const files = this.filesToUpload();
    const remaining = files.filter(
      (fileToUpload) => fileToUpload.id !== file.id
    );
    if (remaining.length !== files.length) {
      this.attachmentRemainingSize.update((size) => ({
        ...size,
        remainingSize: size.remainingSize + file.file.size,
      }));
    }
    this.filesToUpload.set(remaining);
  }

  /**
   * Produces a display-friendly, truncated version of the given text.
   *
   * @param text The text to display, or `undefined`.
   * @returns The original text if 7 characters or fewer, an empty string if
   *   `undefined`, otherwise the first 7 characters followed by an ellipsis.
   */
  public displayName(text: string | undefined): string {
    if (text === undefined) {
      return '';
    }
    return text.length > 7 ? `${text.substring(0, 7)}...` : text;
  }

  /**
   * Removes an existing attachment/link from the edit list and marks it for
   * deletion on save. For non-link attachments, the freed size is added back to
   * the remaining attachment size budget.
   *
   * @param attachment The attachment or link to remove.
   */
  public removeAttachment(attachment: Attachment) {
    this.attachmentsAndLinksToEdit.update((attachments) =>
      attachments.filter((item) => item !== attachment)
    );
    this.attachmentsAndLinksToDelete.push(attachment);
    if (!attachment.isLink) {
      this.attachmentRemainingSize.update((size) => ({
        ...size,
        remainingSize: size.remainingSize + (attachment.size as number),
      }));
    }
  }

  // links

  /** Opens the node link picker and marks it as loading. */
  public openLinkPicker() {
    this.linkPickerOpen.set(true);
    this.loadingPicker = true;
  }

  /** Marks the link picker as finished loading once it has opened. */
  public linkPickerOpened() {
    this.loadingPicker = false;
  }

  /** Closes the node link picker and clears any picked nodes. */
  public closeLinkPicker() {
    this.linkPickerOpen.set(false);
    this.pickedNodes = [];
  }

  /**
   * Translates a caught post-submission error into a localised, user-facing
   * message and displays it. Recognises attachment size, invalid file type and
   * duplicate link errors, falling back to a generic upload error otherwise.
   *
   * @param error The error thrown during a post create/update request.
   * @returns A promise that resolves once the error message has been shown.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async handleError(error: any) {
    let res: string;
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

  /**
   * Determines whether the current post/edit can be submitted: there must be
   * remaining attachment budget and either valid text content, attachments
   * marked for deletion, files queued for upload, or picked node links.
   *
   * @returns `true` if the post is valid for submission, otherwise `false`.
   */
  public isAllValid(): boolean {
    return (
      this.attachmentRemainingSize().remainingSize > 0 &&
      (this.isValid ||
        this.attachmentsAndLinksToDelete.length > 0 ||
        this.filesToUpload().length > 0 ||
        this.pickedNodes.length > 0)
    );
  }

  /**
   * Returns the remaining attachment size budget formatted in megabytes.
   *
   * @returns The remaining size in MB as a string with two decimals, or `0`
   *   when no budget information is available.
   */
  public getRemainingSizeInMB() {
    const attachmentRemainingSize = this.attachmentRemainingSize();
    if (attachmentRemainingSize !== undefined) {
      return (attachmentRemainingSize.remainingSize / 1024 / 1024).toFixed(2);
    }
    return 0;
  }
}
