import {
  Component,
  EventEmitter,
  Inject,
  Input,
  Optional,
  Output,
} from '@angular/core';

import { Router } from '@angular/router';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
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
import { assertDefined } from 'app/core/asserts';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.scss'],
  preserveWhitespaces: true,
})
export class PostComponent {
  @Input()
  public topic: ModelNode | undefined;
  @Input()
  public post!: ModelNode;
  @Input()
  public igId: string | undefined;
  @Output()
  public readonly replyClicked = new EventEmitter<Quote>();
  @Output()
  public readonly editClicked = new EventEmitter<ModelNode>();
  @Output()
  public readonly deleted = new EventEmitter<ActionEmitterResult>();
  @Output()
  public readonly verified = new EventEmitter<ActionEmitterResult>();

  public abuses!: AbuseReport[];

  public showRejectModal = false;

  public showSignalModal = false;
  public showSignaledAbuses = false;
  public showModifiedDate = false;

  public attachments: File[] = [];

  private basePath!: string;

  public constructor(
    private postService: PostService,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private loginService: LoginService,
    private saveAsService: SaveAsService,
    private router: Router,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  public fireReply() {
    if (this.post && this.post.properties) {
      const allQuotesExpression =
        /<p><br><\/p><p>(\w)+: (((\w)+\s)*)(\w)+<\/p>(<p><br><\/p>)?<blockquote>(.)+<\/blockquote>$/;
      const message =
        allQuotesExpression.test(this.post.properties.message) !== null
          ? this.post.properties.message.replace(allQuotesExpression, '')
          : this.post.properties.message;
      const quote: Quote = {
        author: this.post.properties.creator,
        text: message,
      };
      this.replyClicked.emit(quote);
    }
  }

  public fireEdit() {
    if (this.post && this.post.properties) {
      this.editClicked.emit(this.post);
    }
  }

  public async deletePost() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_POST;

    try {
      if (this.post.id) {
        await firstValueFrom(this.postService.deletePost(this.post.id));
        result.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      result.result = ActionResult.FAILED;
    }

    this.deleted.emit(result);
  }

  public canDeleteComment(): boolean {
    return (
      this.permEvalService.isLibAdmin(this.post) ||
      this.permEvalService.isNewsgroupAdmin(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post) ||
      this.isOwner()
    );
  }

  public canPostComment(): boolean {
    return (
      this.permEvalService.isLibAccess(this.post) ||
      this.permEvalService.isNewsgroupPost(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post)
    );
  }

  public canEditComment(): boolean {
    return (
      this.permEvalService.isLibAdminOrFullEdit(this.post) ||
      this.permEvalService.isNewsgroupModerate(this.post) ||
      this.isOwner()
    );
  }

  public isOwner(): boolean {
    if (this.post && this.post.properties) {
      return (
        this.loginService.getCurrentUsername() === this.post.properties.owner
      );
    } else {
      return false;
    }
  }

  public async approve() {
    if (this.post.properties) {
      await firstValueFrom(
        this.postService.putVerify(this.post.id as string, true)
      );
      this.post.properties.waitingForApproval = 'false';

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.APPROVE_POST;

      this.verified.emit(result);
    }
  }

  public reject() {
    this.showRejectModal = true;
  }

  public refreshPostAfterReject(result: ActionEmitterResult) {
    this.showRejectModal = false;
    this.verified.emit(result);
  }

  public refreshPostAfterAbuse(result: ActionEmitterResult) {
    this.showSignalModal = false;
    this.verified.emit(result);
  }

  public waitingForApproval(): boolean {
    if (this.post.properties) {
      return this.post.properties.waitingForApproval === 'true';
    }
    return false;
  }

  public isModerator(): boolean {
    assertDefined(this.topic);
    if (this.topic.permissions) {
      return (
        this.topic.permissions.NwsModerate === 'ALLOWED' ||
        this.topic.permissions.NwsAdmin === 'ALLOWED'
      );
    }
    return false;
  }

  public hasBeenEdited(): boolean {
    if (this.post.properties) {
      return this.post.properties.versionLabel !== '1.0';
    }
    return false;
  }

  public hasBeenRejected(): boolean {
    if (this.post.properties) {
      return this.post.properties.rejectedOn !== undefined;
    }
    return false;
  }

  public abuseSignaled(): boolean {
    if (!this.post.properties) {
      return false;
    }
    return (
      this.post.properties.messages !== undefined &&
      this.post.properties.messages.includes('Abuse Report:')
    );
  }

  public signalAbuse() {
    this.showSignalModal = true;
  }

  public async removeAbuses() {
    await firstValueFrom(this.postService.deleteAbuse(this.post.id as string));
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ABUSE;
    this.verified.emit(result);
  }

  public async toggleShowAbuses() {
    if (!this.showSignaledAbuses) {
      await this.getAbuses();
    }
    this.showSignaledAbuses = !this.showSignaledAbuses;
  }

  private async getAbuses() {
    this.abuses = await firstValueFrom(
      this.postService.getAbuses(this.post.id as string)
    );
  }

  public toggleShowModifiedDate() {
    this.showModifiedDate = !this.showModifiedDate;
  }

  public displayName(text: string | undefined): string {
    if (text === undefined) {
      return '';
    }
    return text.length > 7 ? `${text.substring(0, 7)}...` : text;
  }

  public async accessAttachment(attachment: Attachment) {
    if (attachment.isLink) {
      const attachmentNode = await firstValueFrom(
        this.nodesService.getNode(attachment.id as string)
      );
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate([
        `/group/${this.igId}/library/${attachment.id}${
          !(attachmentNode.type as string).endsWith('folder') ? '/details' : ''
        }`,
      ]);
    } else {
      this.downloadAttachment(attachment);
    }
  }

  public downloadAttachment(attachment: Attachment) {
    const url = `${this.basePath}/posts/${attachment.id}/attachment/download`;
    this.saveAsService.saveUrlAs(url, attachment.name as string);
  }

  get avatar(): string | null {
    if (this.post.properties) {
      return this.post.properties.avatar;
    } else {
      return null;
    }
  }

  get creator(): string | null {
    if (this.post.properties) {
      return this.post.properties.creator;
    } else {
      return null;
    }
  }

  get modifier(): string | null {
    if (this.post.properties) {
      return this.post.properties.modifier;
    } else {
      return null;
    }
  }

  get created(): string | null {
    if (this.post.properties) {
      return this.post.properties.created;
    } else {
      return null;
    }
  }

  get modified(): string | null {
    if (this.post.properties) {
      return this.post.properties.modified;
    } else {
      return null;
    }
  }

  get rejectedBy(): string | null {
    if (this.post.properties) {
      return this.post.properties.rejectedBy;
    } else {
      return null;
    }
  }

  get rejectedOn(): string | null {
    if (this.post.properties) {
      return this.post.properties.rejectedOn;
    } else {
      return null;
    }
  }

  get message(): string | null {
    if (this.post.properties) {
      return this.post.properties.message;
    } else {
      return null;
    }
  }
}
