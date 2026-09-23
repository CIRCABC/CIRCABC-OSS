import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  AbuseReport,
  Attachment,
  BASE_PATH,
  Node as ModelNode,
  NodesService,
  PostService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { Quote } from 'app/core/ui-model/index';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { IfRoleGePipe } from 'app/shared/pipes/if-role-ge.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { RejectPostComponent } from './reject-post/reject-post.component';
import { SignalAbuseComponent } from './signal-abuse/signal-abuse.component';

/**
 * Renders a single forum post (a message/comment inside a discussion topic).
 *
 * The component displays the post's author (via `cbc-user-card`), its message
 * body, creation/modification dates and any attachments. It also exposes the
 * moderation and interaction actions available for the post, such as replying,
 * editing, deleting, approving, rejecting and signalling abuse — each gated by
 * the current user's permissions as computed through
 * {@link PermissionEvaluatorService}.
 *
 * User-triggered actions are surfaced to the parent component through the
 * `replyClicked`, `editClicked`, `deleted` and `verified` outputs, while
 * backend operations (delete, verify/approve, abuse retrieval/removal) are
 * delegated to {@link PostService} and {@link NodesService}.
 */
@Component({
  selector: 'cbc-post',
  templateUrl: './post.component.html',
  styleUrl: './post.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HintComponent,
    UserCardComponent,
    InlineDeleteComponent,
    RejectPostComponent,
    SignalAbuseComponent,
    DatePipe,
    DownloadPipe,
    IfRoleGePipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class PostComponent {
  /** Client used for post-related backend operations (delete, verify, abuse handling). */
  private readonly postService = inject(PostService);
  /** Client used to resolve nodes (e.g. attachment links) from the backend. */
  private readonly nodesService = inject(NodesService);
  /** Evaluates the current user's permissions against a given node. */
  private readonly permEvalService = inject(PermissionEvaluatorService);
  /** Provides information about the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** Handles saving/downloading remote resources to the user's device. */
  private readonly saveAsService = inject(SaveAsService);
  /** Angular router used to navigate to linked attachment nodes. */
  private readonly router = inject(Router);

  /**
   * The parent discussion topic node this post belongs to. Used to derive
   * moderation permissions for the post.
   */
  public readonly topic = input<ModelNode>();

  /** The post node to render. Required input. */
  public readonly post = input.required<ModelNode>();
  /** Identifier of the interest group the post belongs to; used for attachment navigation. */
  public readonly igId = input<string>();
  /** Emitted when the user chooses to reply, carrying the quoted author and message. */
  public readonly replyClicked = output<Quote>();
  /** Emitted when the user chooses to edit the post, carrying the post node. */
  public readonly editClicked = output<ModelNode>();
  /** Emitted after a delete attempt, carrying the outcome of the operation. */
  public readonly deleted = output<ActionEmitterResult>();
  /**
   * Emitted after a moderation action (approve, reject, abuse removal/report)
   * so the parent can refresh, carrying the outcome of the operation.
   */
  public readonly verified = output<ActionEmitterResult>();

  /** Abuse reports fetched for this post; populated lazily when shown. */
  public readonly abuses = signal<AbuseReport[]>([]);

  /** Whether the reject-post modal is currently displayed. */
  public showRejectModal = false;

  /** Whether the signal-abuse modal is currently displayed. */
  public showSignalModal = false;
  /** Whether the list of signalled abuses is currently expanded. */
  public readonly showSignaledAbuses = signal(false);
  /** Whether the modified date is currently shown instead of the created date. */
  public showModifiedDate = false;

  /** Files attached to the post. */
  public attachments: File[] = [];

  /** Base URL of the CIRCABC API, used to build attachment download URLs. */
  private readonly basePath!: string;

  /**
   * Resolves the API base path from the {@link BASE_PATH} injection token and
   * stores it for later attachment URL construction.
   */
  public constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }
  }

  /**
   * Builds a {@link Quote} from the current post and emits it through
   * `replyClicked`. If the message ends with a previously appended quote block,
   * that trailing quote is stripped so replies do not nest quotes indefinitely.
   */
  public fireReply() {
    const post = this.post();
    if (post?.properties) {
      const allQuotesExpression =
        /<p><br><\/p><p>(\w)+: (((\w)+\s)*)(\w)+<\/p>(<p><br><\/p>)?<blockquote>(.)+<\/blockquote>$/;
      const message = allQuotesExpression.test(post.properties.message)
        ? post.properties.message.replace(allQuotesExpression, '')
        : post.properties.message;
      const quote: Quote = {
        author: post.properties.creator,
        text: message,
      };
      this.replyClicked.emit(quote);
    }
  }

  /** Emits the current post through `editClicked` so the parent can edit it. */
  public fireEdit() {
    const post = this.post();
    if (post?.properties) {
      this.editClicked.emit(post);
    }
  }

  /**
   * Deletes the current post via {@link PostService} and emits the outcome
   * through `deleted`.
   *
   * @returns A promise that resolves once the delete attempt completes and the
   * result has been emitted.
   */
  public async deletePost() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_POST;

    try {
      const post = this.post();
      if (post.id) {
        await this.postService.deletePostAsync({ id: post.id });
        result.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    this.deleted.emit(result);
  }

  /**
   * Determines whether the current user may delete this comment.
   *
   * @returns `true` if the user is a library admin, newsgroup admin,
   * newsgroup moderator or the owner of the post.
   */
  public canDeleteComment(): boolean {
    const post = this.post();
    return (
      this.permEvalService.isLibAdmin(post) ||
      this.permEvalService.isNewsgroupAdmin(post) ||
      this.permEvalService.isNewsgroupModerate(post) ||
      this.isOwner()
    );
  }

  /**
   * Determines whether the current user may post a comment/reply.
   *
   * @returns `true` if the user has library access, newsgroup post or
   * newsgroup moderate permission.
   */
  public canPostComment(): boolean {
    const post = this.post();
    return (
      this.permEvalService.isLibAccess(post) ||
      this.permEvalService.isNewsgroupPost(post) ||
      this.permEvalService.isNewsgroupModerate(post)
    );
  }

  /**
   * Determines whether the current user may edit this comment.
   *
   * @returns `true` if the user is a library admin/full-editor, newsgroup
   * moderator or the owner of the post.
   */
  public canEditComment(): boolean {
    const post = this.post();
    return (
      this.permEvalService.isLibAdminOrFullEdit(post) ||
      this.permEvalService.isNewsgroupModerate(post) ||
      this.isOwner()
    );
  }

  /**
   * Checks whether the current user is the owner of the post.
   *
   * @returns `true` if the authenticated username matches the post's owner.
   */
  public isOwner(): boolean {
    const post = this.post();
    if (post?.properties) {
      return this.loginService.getCurrentUsername() === post.properties.owner;
    }
    return false;
  }

  /**
   * Approves (verifies) the post via {@link PostService}, marks it as no longer
   * awaiting approval and emits the outcome through `verified`.
   *
   * @returns A promise that resolves once the approval has been processed and
   * emitted.
   */
  public async approve() {
    const post = this.post();
    if (post.properties) {
      await this.postService.putVerifyAsync({
        id: post.id as string,
        approve: true,
      });
      post.properties.waitingForApproval = 'false';

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.APPROVE_POST;

      this.verified.emit(result);
    }
  }

  /** Opens the reject-post modal. */
  public reject() {
    this.showRejectModal = true;
  }

  /**
   * Closes the reject-post modal and forwards the reject outcome through
   * `verified` so the parent can refresh.
   *
   * @param result The outcome of the reject operation.
   */
  public refreshPostAfterReject(result: ActionEmitterResult) {
    this.showRejectModal = false;
    this.verified.emit(result);
  }

  /**
   * Closes the signal-abuse modal and forwards the abuse-report outcome through
   * `verified` so the parent can refresh.
   *
   * @param result The outcome of the abuse-report operation.
   */
  public refreshPostAfterAbuse(result: ActionEmitterResult) {
    this.showSignalModal = false;
    this.verified.emit(result);
  }

  /**
   * Indicates whether the post is pending moderator approval.
   *
   * @returns `true` if the post's `waitingForApproval` property is `'true'`.
   */
  public waitingForApproval(): boolean {
    const post = this.post();
    if (post.properties) {
      return post.properties.waitingForApproval === 'true';
    }
    return false;
  }

  /**
   * Indicates whether the current user can moderate the parent topic.
   *
   * @returns `true` if the topic grants `NwsModerate` or `NwsAdmin` permission.
   * @throws Error (via {@link assertDefined}) if the `topic` input is undefined.
   */
  public isModerator(): boolean {
    const topic = this.topic();
    assertDefined(topic);
    if (topic.permissions) {
      return (
        topic.permissions.NwsModerate === 'ALLOWED' ||
        topic.permissions.NwsAdmin === 'ALLOWED'
      );
    }
    return false;
  }

  /**
   * Indicates whether the post has been edited since creation.
   *
   * @returns `true` if the post's version label differs from the initial
   * `'1.0'`.
   */
  public hasBeenEdited(): boolean {
    const post = this.post();
    if (post.properties) {
      return post.properties.versionLabel !== '1.0';
    }
    return false;
  }

  /**
   * Indicates whether the post has been rejected by a moderator.
   *
   * @returns `true` if the post has a `rejectedOn` timestamp.
   */
  public hasBeenRejected(): boolean {
    const post = this.post();
    if (post.properties) {
      return post.properties.rejectedOn !== undefined;
    }
    return false;
  }

  /**
   * Indicates whether an abuse report has been raised against the post.
   *
   * @returns `true` if the post's messages contain an `'Abuse Report:'` marker.
   */
  public abuseSignaled(): boolean {
    const post = this.post();
    if (!post.properties) {
      return false;
    }
    return post.properties.messages?.includes('Abuse Report:');
  }

  /** Opens the signal-abuse modal. */
  public signalAbuse() {
    this.showSignalModal = true;
  }

  /**
   * Removes all abuse reports for the post via {@link PostService} and emits the
   * outcome through `verified`.
   *
   * @returns A promise that resolves once the abuses have been removed and the
   * result emitted.
   */
  public async removeAbuses() {
    await this.postService.deleteAbuseAsync({
      id: this.post().id as string,
    });
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ABUSE;
    this.verified.emit(result);
  }

  /**
   * Toggles the display of signalled abuses, fetching them on first expansion.
   *
   * @returns A promise that resolves once the abuses have been loaded (when
   * expanding) and the visibility state toggled.
   */
  public async toggleShowAbuses() {
    if (!this.showSignaledAbuses()) {
      await this.getAbuses();
    }
    this.showSignaledAbuses.set(!this.showSignaledAbuses());
  }

  /**
   * Loads the abuse reports for the post from {@link PostService} into
   * {@link abuses}.
   *
   * @returns A promise that resolves once the abuses have been fetched.
   */
  private async getAbuses() {
    this.abuses.set(
      await this.postService.getAbusesAsync({ id: this.post().id as string })
    );
  }

  /** Toggles between showing the modified date and the created date. */
  public toggleShowModifiedDate() {
    this.showModifiedDate = !this.showModifiedDate;
  }

  /**
   * Truncates a display name for compact rendering.
   *
   * @param text The name to display, or `undefined`.
   * @returns An empty string if `text` is undefined, the text itself if 7
   * characters or shorter, otherwise the first 7 characters followed by an
   * ellipsis.
   */
  public displayName(text: string | undefined): string {
    if (text === undefined) {
      return '';
    }
    return text.length > 7 ? `${text.substring(0, 7)}...` : text;
  }

  /**
   * Opens an attachment. Link attachments navigate to the corresponding library
   * node (folder or details view); file attachments are downloaded.
   *
   * @param attachment The attachment to access.
   * @returns A promise that resolves once navigation has been initiated or the
   * download has been triggered.
   */
  public async accessAttachment(attachment: Attachment) {
    if (attachment.isLink) {
      const attachmentNode = await this.nodesService.getNodeAsync({
        id: attachment.id as string,
      });

      this.router.navigate([
        `/group/${this.igId()}/library/${attachment.id}${
          (attachmentNode.type as string).endsWith('folder') ? '' : '/details'
        }`,
      ]);
    } else {
      this.downloadAttachment(attachment);
    }
  }

  /**
   * Downloads a file attachment by building its download URL and delegating to
   * {@link SaveAsService}.
   *
   * @param attachment The attachment to download.
   */
  public downloadAttachment(attachment: Attachment) {
    const url = `${this.basePath}/posts/${attachment.id}/attachment/download`;
    this.saveAsService.saveUrlAs(url, attachment.name as string);
  }

  /** The post author's avatar, or `null` if unavailable. */
  get avatar(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.avatar;
    }
    return null;
  }

  /** The username of the post's creator, or `null` if unavailable. */
  get creator(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.creator;
    }
    return null;
  }

  /** The username of the post's last modifier, or `null` if unavailable. */
  get modifier(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.modifier;
    }
    return null;
  }

  /** The post's creation date, or `null` if unavailable. */
  get created(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.created;
    }
    return null;
  }

  /** The post's last modification date, or `null` if unavailable. */
  get modified(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.modified;
    }
    return null;
  }

  /** The username of the moderator who rejected the post, or `null` if not rejected. */
  get rejectedBy(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.rejectedBy;
    }
    return null;
  }

  /** The date on which the post was rejected, or `null` if not rejected. */
  get rejectedOn(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.rejectedOn;
    }
    return null;
  }

  /** The post's message body, or `null` if unavailable. */
  get message(): string | null {
    const post = this.post();
    if (post.properties) {
      return post.properties.message;
    }
    return null;
  }
}
